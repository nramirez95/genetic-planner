package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "resource")
class ResourceEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_problem_id", nullable = false)
    var planningProblem: PlanningProblemEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    var type: ResourceTypeEntity,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    var attributes: Map<String, String> = emptyMap()
)