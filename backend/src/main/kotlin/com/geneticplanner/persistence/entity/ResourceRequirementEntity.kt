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

@Entity
@Table(name = "resource_requirement")
class ResourceRequirementEntity(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    var activity: ActivityEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_type_id", nullable = false)
    var resourceType: ResourceTypeEntity,

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 1,

    @Column(name = "candidate_resources_restricted", nullable = false)
    var candidateResourcesRestricted: Boolean = false,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "requirement_candidate_resource",
        joinColumns = [
            JoinColumn(name = "requirement_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "resource_id")
        ]
    )
    var candidateResources: MutableSet<ResourceEntity> = mutableSetOf()
)