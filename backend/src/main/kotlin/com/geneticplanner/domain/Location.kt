package com.geneticplanner.domain

data class Location (
    val id: String,
    val name: String,
    val type: String? = null,
    val capacity: Int? = null,
    val attributes: Map<String, String> = emptyMap()
)