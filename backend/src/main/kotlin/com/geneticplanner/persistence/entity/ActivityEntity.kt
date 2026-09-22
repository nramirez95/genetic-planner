package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "activity")
class ActivityEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_problem_id", nullable = false)
    var planningProblem: PlanningProblemEntity,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "type", length = 100)
    var type: String? = null,

    @Column(name = "time_slots_restricted", nullable = false)
    var timeSlotsRestricted: Boolean = false,

    @Column(name = "locations_restricted", nullable = false)
    var locationsRestricted: Boolean = false,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "activity_allowed_time_slot",
        joinColumns = [
            JoinColumn(name = "activity_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "time_slot_id")
        ]
    )
    var allowedTimeSlots: MutableSet<TimeSlotEntity> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "activity_allowed_location",
        joinColumns = [
            JoinColumn(name = "activity_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "location_id")
        ]
    )
    var allowedLocations: MutableSet<LocationEntity> = mutableSetOf(),

    @Column(name = "required_location_capacity")
    var requiredLocationCapacity: Int? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    var attributes: Map<String, String> = emptyMap()
)