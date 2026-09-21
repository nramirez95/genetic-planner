package com.geneticplanner.genetic.codec

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.candidate.ActivityCandidateOptions
import com.geneticplanner.domain.candidate.AssignmentCandidateGenerationResult
import com.geneticplanner.domain.candidate.AssignmentOption
import io.jenetics.Genotype
import io.jenetics.IntegerChromosome
import io.jenetics.IntegerGene

class ScheduleGenotypeCodec(
    private val planningProblemId: String,
    candidateGenerationResult: AssignmentCandidateGenerationResult
) {

    private val candidatesByActivity: List<ActivityCandidateOptions> =
        candidateGenerationResult.candidatesByActivity

    init {
        require(candidatesByActivity.isNotEmpty()) {
            "Cannot create genotype codec without activities."
        }

        require(candidatesByActivity.all { it.options.isNotEmpty() }) {
            "Cannot create genotype codec when an activity has no assignment options."
        }
    }

    fun createGenotype(): Genotype<IntegerGene> {
        val chromosomes =
            candidatesByActivity.map { activityCandidates ->

                val optionCount =
                    activityCandidates.options.size

                if (optionCount == 1) {
                    IntegerChromosome.of(
                        IntegerGene.of(
                            0,
                            0,
                            1
                        )
                    )
                } else {
                    IntegerChromosome.of(
                        0,
                        optionCount - 1,
                        1
                    )
                }
            }

        return Genotype.of(chromosomes)
    }

    fun decode(
        genotype: Genotype<IntegerGene>
    ): Schedule {
        require(genotype.length() == candidatesByActivity.size) {
            "Genotype chromosome count does not match activity count."
        }

        val assignments =
            candidatesByActivity.mapIndexed { index, activityCandidates ->
                val chromosome = genotype.get(index)

                require(chromosome.length() == 1) {
                    "Each chromosome must contain exactly one genetic decision."
                }

                val optionIndex =
                    chromosome.gene().allele()

                require(optionIndex in activityCandidates.options.indices) {
                    "Allele $optionIndex is outside the candidate range " +
                            "for activity '${activityCandidates.activityId}'."
                }

                activityCandidates.options[optionIndex]
                    .toAssignment()
            }

        return Schedule(
            planningProblemId = planningProblemId,
            assignments = assignments
        )
    }

    fun encode(
        schedule: Schedule
    ): Genotype<IntegerGene> {
        require(schedule.planningProblemId == planningProblemId) {
            "Schedule belongs to a different planning problem."
        }

        val assignmentsByActivity =
            schedule.assignments.groupBy { it.activityId }

        val chromosomes =
            candidatesByActivity.map { activityCandidates ->
                val assignments =
                    assignmentsByActivity[
                        activityCandidates.activityId
                    ].orEmpty()

                require(assignments.size == 1) {
                    "Schedule must contain exactly one assignment for " +
                            "activity '${activityCandidates.activityId}'."
                }

                val assignment = assignments.single()

                val optionIndex =
                    activityCandidates.options.indexOfFirst { option ->
                        option.matches(assignment)
                    }

                require(optionIndex >= 0) {
                    "Schedule assignment for activity " +
                            "'${activityCandidates.activityId}' does not " +
                            "match any generated assignment option."
                }

                val gene = IntegerGene.of(
                    optionIndex,
                    0,
                    activityCandidates.options.size
                )

                IntegerChromosome.of(gene)
            }

        return Genotype.of(chromosomes)
    }

    private fun AssignmentOption.toAssignment() =
        Assignment(
            activityId = activityId,
            timeSlotId = timeSlotId,
            resourceAssignments = resourceAssignments,
            locationId = locationId
        )

    private fun AssignmentOption.matches(
        assignment: Assignment
    ): Boolean =
        activityId == assignment.activityId &&
                timeSlotId == assignment.timeSlotId &&
                resourceAssignments == assignment.resourceAssignments &&
                locationId == assignment.locationId
}