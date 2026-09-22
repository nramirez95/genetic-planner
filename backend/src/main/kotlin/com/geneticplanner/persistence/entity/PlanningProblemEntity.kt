package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "planning_problem")
class PlanningProblemEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "template_type", length = 100)
    var templateType: String? = null,

    @Column(name = "planning_horizon_start", nullable = false)
    var planningHorizonStart: LocalDateTime,

    @Column(name = "planning_horizon_end", nullable = false)
    var planningHorizonEnd: LocalDateTime
)