package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "time_slot")
class TimeSlotEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_problem_id", nullable = false)
    var planningProblem: PlanningProblemEntity,

    @Column(name = "start_time", nullable = false)
    var start: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    var end: LocalDateTime,

    @Column(name = "label", length = 255)
    var label: String? = null
)