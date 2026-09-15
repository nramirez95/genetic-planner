package com.geneticplanner.domain

data class Resource (
    val id: String,
    val name: String,
    val typeId: String,
    val attributes: Map<String, String> = emptyMap()
)