package com.geneticplanner.domain.constraint

enum class ConstraintType {
    HARD, //Should not be violated in a feasible solution
    SOFT // Could be disregarded, but reduces the solution quality
}