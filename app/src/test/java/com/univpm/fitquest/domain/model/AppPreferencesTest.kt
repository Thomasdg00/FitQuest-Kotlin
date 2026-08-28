package com.univpm.fitquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreferencesTest {
    @Test
    fun themeModeSupportsLightAndDarkWhileUnknownOrLegacyValuesFallBackToLight() {
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorageValue("dark"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorageValue("light"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorageValue(null))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorageValue("unknown"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorageValue("system"))
    }
}
