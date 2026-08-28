package com.univpm.fitquest.domain.model

enum class ThemeMode(val storageValue: String) {
    Light("light"),
    Dark("dark");

    companion object {
        fun fromStorageValue(value: String?): ThemeMode = when (value) {
            Dark.storageValue -> Dark
            else -> Light
        }
    }
}
