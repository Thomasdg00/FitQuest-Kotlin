package com.univpm.fitquest.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class DarkColorContrastTest {
    @Test
    fun darkColorRolePairsMeetNormalTextContrast() {
        val rolePairs = mapOf(
            "primary" to (DarkPrimary to DarkOnPrimary),
            "primaryContainer" to (DarkPrimaryContainer to DarkOnPrimaryContainer),
            "secondary" to (DarkSecondary to DarkOnSecondary),
            "secondaryContainer" to (DarkSecondaryContainer to DarkOnSecondaryContainer),
            "background" to (DarkBackground to DarkOnBackground),
            "surface" to (DarkSurface to DarkOnSurface),
            "error" to (DarkError to DarkOnError),
        )

        rolePairs.forEach { (role, colors) ->
            val ratio = contrastRatio(colors.first, colors.second)
            assertTrue("$role contrast was $ratio", ratio >= 4.5)
        }
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = max(relativeLuminance(first), relativeLuminance(second))
        val darker = min(relativeLuminance(first), relativeLuminance(second))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        return 0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)
    }

    private fun linearize(channel: Float): Double {
        val value = channel.toDouble()
        return if (value <= 0.04045) {
            value / 12.92
        } else {
            ((value + 0.055) / 1.055).pow(2.4)
        }
    }
}
