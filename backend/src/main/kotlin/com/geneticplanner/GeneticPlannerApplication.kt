package com.geneticplanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GeneticPlannerApplication

fun main(args: Array<String>) {
    runApplication<GeneticPlannerApplication>(*args)
}