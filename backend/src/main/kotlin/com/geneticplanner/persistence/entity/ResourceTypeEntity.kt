package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.FetchType
import jakarta.persistence.Table

@Entity
@Table(name = "resource_type")
class ResourceTypeEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_problem_id", nullable = false)
    var planningProblem: PlanningProblemEntity
)