package com.univpm.fitquest.domain.model

enum class Sport(val routeValue: String) {
    Walking("walking"),
    Running("running"),
    Cycling("cycling");

    companion object {
        fun fromRouteValue(value: String?): Sport =
            entries.firstOrNull { it.routeValue == value } ?: Walking
    }
}
