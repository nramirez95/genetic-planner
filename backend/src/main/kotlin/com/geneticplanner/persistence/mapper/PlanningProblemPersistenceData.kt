package com.geneticplanner.persistence.mapper

import com.geneticplanner.persistence.entity.ActivityEntity
import com.geneticplanner.persistence.entity.LocationEntity
import com.geneticplanner.persistence.entity.PlanningConstraintEntity
import com.geneticplanner.persistence.entity.PlanningProblemEntity
import com.geneticplanner.persistence.entity.ResourceEntity
import com.geneticplanner.persistence.entity.ResourceRequirementEntity
import com.geneticplanner.persistence.entity.ResourceTypeEntity
import com.geneticplanner.persistence.entity.TimeSlotEntity

data class PlanningProblemPersistenceData(
    val problem: PlanningProblemEntity,
    val resourceTypes: List<ResourceTypeEntity>,
    val resources: List<ResourceEntity>,
    val timeSlots: List<TimeSlotEntity>,
    val locations: List<LocationEntity>,
    val activities: List<ActivityEntity>,
    val requirements: List<ResourceRequirementEntity>,
    val constraints: List<PlanningConstraintEntity>
)