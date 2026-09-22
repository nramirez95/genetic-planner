package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "planning_constraint")
class PlanningConstraintEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_problem_id", nullable = false)
    var planningProblem: PlanningProblemEntity,

    @Column(name = "constraint_key", nullable = false, length = 100)
    var constraintKey: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_type", nullable = false, length = 20)
    var constraintType: PersistenceConstraintType,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "weight", nullable = false)
    var weight: Double,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", nullable = false, columnDefinition = "jsonb")
    var configuration: Map<String, Any?> = emptyMap()
)

enum class PersistenceConstraintType {
    HARD,
    SOFT
}