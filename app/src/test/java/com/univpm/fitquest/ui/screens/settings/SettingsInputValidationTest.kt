package com.univpm.fitquest.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsInputValidationTest {
    @Test
    fun bodyWeightInitializationUsesItalianDecimalSeparator() {
        assertEquals("70,0", formatBodyWeightInput(70.0))
    }

    @Test
    fun bodyWeightAcceptsDotAndCommaDecimals() {
        assertEquals(70.5, parseBodyWeightKgInput("70.5").value ?: -1.0, 0.0)
        assertEquals(70.5, parseBodyWeightKgInput("70,5").value ?: -1.0, 0.0)
    }

    @Test
    fun bodyWeightRejectsInvalidAndNonFiniteValues() {
        assertEquals(SettingsInputError.Required, parseBodyWeightKgInput(" ").error)
        listOf("abc", "-1", "0", "19.9", "300.1", "NaN", "Infinity").forEach {
            assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput(it).error)
        }
    }

    @Test
    fun weeklyGoalKeepsCurrentForBlankAndAcceptsZeroAndCommaDecimal() {
        assertEquals(5.0, parseWeeklyGoalKmInput(" ", 5.0).value ?: -1.0, 0.0)
        assertEquals(0.0, parseWeeklyGoalKmInput("0", 5.0).value ?: -1.0, 0.0)
        assertEquals(2.5, parseWeeklyGoalKmInput("2,5", 5.0).value ?: -1.0, 0.0)
    }

    @Test
    fun weeklyGoalRejectsInvalidValues() {
        listOf("abc", "-1", "NaN", "Infinity").forEach {
            assertEquals(SettingsInputError.WeeklyGoalKm, parseWeeklyGoalKmInput(it, 5.0).error)
        }
    }
}
