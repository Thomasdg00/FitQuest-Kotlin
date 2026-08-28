package com.univpm.fitquest.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.data.local.database.FitQuestDatabase
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryRoomTest {
    private lateinit var database: FitQuestDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            FitQuestDatabase::class.java,
        ).build()
        repository = WorkoutRepository(
            workoutDao = database.workoutDao(),
            routePointDao = database.routePointDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun routePointFailureRollsBackWorkoutAndRoutePoints() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_route_point_insert
            BEFORE INSERT ON route_points
            BEGIN
                SELECT RAISE(ABORT, 'forced_route_point_failure');
            END
            """.trimIndent(),
        )

        val failure = try {
            repository.saveWorkout(workout(), routePoints())
            null
        } catch (error: Exception) {
            error
        }

        assertNotNull(failure)
        assertEquals(0L, rowCount("workouts"))
        assertEquals(0L, rowCount("route_points"))
    }

    @Test
    fun successfulSaveLinksEveryPointAndPreservesSequence() = runBlocking {
        val workoutId = repository.saveWorkout(workout(), routePoints())

        val workouts = repository.observeWorkouts().first()
        val savedPoints = repository.observeRoutePoints(workoutId).first()

        assertEquals(1, workouts.size)
        assertEquals(workoutId, workouts.single().id)
        assertEquals(2, savedPoints.size)
        assertTrue(savedPoints.all { it.workoutId == workoutId })
        assertEquals(listOf(0, 1), savedPoints.map { it.sequenceIndex })
        assertEquals(listOf(43.60, 43.61), savedPoints.map { it.latitude })
    }

    @Test
    fun deletingWorkoutCascadesToRoutePoints() = runBlocking {
        val workoutId = repository.saveWorkout(workout(), routePoints())
        assertEquals(2L, rowCount("route_points"))

        assertTrue(repository.deleteWorkout(workoutId))

        assertEquals(0L, rowCount("workouts"))
        assertEquals(0L, rowCount("route_points"))
    }

    private fun workout(): WorkoutEntity = WorkoutEntity(
        sport = "running",
        startedAtMillis = 1_000L,
        endedAtMillis = 61_000L,
        durationMillis = 60_000L,
        distanceMeters = 100.0,
        isCompleted = true,
    )

    private fun routePoints(): List<RoutePointEntity> = listOf(
        RoutePointEntity(
            workoutId = 0L,
            sequenceIndex = 0,
            latitude = 43.60,
            longitude = 13.50,
            recordedAtMillis = 2_000L,
        ),
        RoutePointEntity(
            workoutId = 0L,
            sequenceIndex = 1,
            latitude = 43.61,
            longitude = 13.51,
            recordedAtMillis = 3_000L,
        ),
    )

    private fun rowCount(tableName: String): Long {
        database.openHelper.readableDatabase
            .query("SELECT COUNT(*) FROM $tableName")
            .use { cursor ->
                check(cursor.moveToFirst())
                return cursor.getLong(0)
            }
    }
}
