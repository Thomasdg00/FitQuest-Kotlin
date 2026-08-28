package com.univpm.fitquest.tracking.permissions

const val ANDROID_13_API = 33

fun isNotificationPermissionRequired(sdkInt: Int): Boolean = sdkInt >= ANDROID_13_API
