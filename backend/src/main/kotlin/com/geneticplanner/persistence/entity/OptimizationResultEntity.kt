package com.geneticplanner.persistence.entity

import com.geneticplanner.application.model.StoredAssignment
import com.geneticplanner.application.model.StoredConstraintResult
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "optimization_result")
class OptimizationResultEntity(

    @Id
    @Column(
        name = "planning_problem_id",
        nullable = false,
        length = 255
    )
    var planningProblemId: String,

    @Column(
        name = "fitness",
        nullable = false
    )
    var fitness: Double,

    @Column(
        name = "feasible",
        nullable = false
    )
    var feasible: Boolean,

    @Column(
        name = "hard_penalty",
        nullable = false
    )
    var hardPenalty: Double,

    @Column(
        name = "soft_penalty",
        nullable = false
    )
    var softPenalty: Double,

    @Column(
        name = "generations_executed",
        nullable = false
    )
    var generationsExecuted: Long,

    @Column(
        name = "execution_time_millis",
        nullable = false
    )
    var executionTimeMillis: Long,

    @Column(
        name = "random_seed",
        nullable = false
    )
    var randomSeed: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "assignments",
        nullable = false,
        columnDefinition = "jsonb"
    )
    var assignments: List<StoredAssignment>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "constraint_results",
        nullable = false,
        columnDefinition = "jsonb"
    )
    var constraintResults: List<StoredConstraintResult>
)