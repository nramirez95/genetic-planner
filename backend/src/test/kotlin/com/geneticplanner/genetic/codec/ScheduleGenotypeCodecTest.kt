package com.geneticplanner.genetic.codec

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.candidate.ActivityCandidateOptions
import com.geneticplanner.domain.candidate.AssignmentCandidateGenerationResult
import com.geneticplanner.domain.candidate.AssignmentOption
import io.jenetics.Genotype
import io.jenetics.IntegerChromosome
import io.jenetics.IntegerGene
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScheduleGenotypeCodecTest {

    @Test
    fun `one activity corresponds to one chromosome`() {
        val codec = codec()

        val genotype = codec.createGenotype()

        assertEquals(3, genotype.length())
    }

    @Test
    fun `each chromosome contains exactly one gene`() {
        val codec = codec()

        val genotype = codec.createGenotype()

        assertTrue(
            (0 until genotype.length()).all { index ->
                genotype.get(index).length() == 1
            }
        )
    }

    @Test
    fun `generated chromosomes use candidate option index ranges`() {
        val codec = codec()

        repeat(100) {
            val genotype = codec.createGenotype()

            assertTrue(
                genotype.get(0).gene().allele() in 0..2
            )

            assertTrue(
                genotype.get(1).gene().allele() in 0..4
            )

            assertTrue(
                genotype.get(2).gene().allele() in 0..1
            )
        }
    }

    @Test
    fun `genotype decodes selected options into schedule`() {
        val codec = codec()

        val genotype = genotype(
            alleleA = 2,
            alleleB = 4,
            alleleC = 1
        )

        val schedule = codec.decode(genotype)

        assertEquals(
            "problem-1",
            schedule.planningProblemId
        )

        assertEquals(
            3,
            schedule.assignments.size
        )

        assertEquals(
            assignmentFrom(optionA(2)),
            schedule.assignments[0]
        )

        assertEquals(
            assignmentFrom(optionB(4)),
            schedule.assignments[1]
        )

        assertEquals(
            assignmentFrom(optionC(1)),
            schedule.assignments[2]
        )
    }

    @Test
    fun `schedule encodes to expected allele indexes`() {
        val codec = codec()

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignmentFrom(optionA(2)),
                assignmentFrom(optionB(4)),
                assignmentFrom(optionC(1))
            )
        )

        val genotype = codec.encode(schedule)

        assertEquals(3, genotype.length())

        assertEquals(
            2,
            genotype.get(0).gene().allele()
        )

        assertEquals(
            4,
            genotype.get(1).gene().allele()
        )

        assertEquals(
            1,
            genotype.get(2).gene().allele()
        )
    }

    @Test
    fun `encoded genotype preserves candidate option domains`() {
        val codec = codec()

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignmentFrom(optionA(2)),
                assignmentFrom(optionB(4)),
                assignmentFrom(optionC(1))
            )
        )

        val genotype = codec.encode(schedule)

        assertEquals(2, genotype.get(0).gene().allele())
        assertEquals(4, genotype.get(1).gene().allele())
        assertEquals(1, genotype.get(2).gene().allele())
    }

    @Test
    fun `encode followed by decode preserves schedule`() {
        val codec = codec()

        val originalSchedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignmentFrom(optionA(1)),
                assignmentFrom(optionB(3)),
                assignmentFrom(optionC(0))
            )
        )

        val genotype =
            codec.encode(originalSchedule)

        val decodedSchedule =
            codec.decode(genotype)

        assertEquals(
            originalSchedule,
            decodedSchedule
        )
    }

    @Test
    fun `decode followed by encode preserves allele values`() {
        val codec = codec()

        val originalGenotype = genotype(
            alleleA = 2,
            alleleB = 4,
            alleleC = 1
        )

        val schedule =
            codec.decode(originalGenotype)

        val encodedGenotype =
            codec.encode(schedule)

        assertEquals(
            2,
            encodedGenotype.get(0).gene().allele()
        )

        assertEquals(
            4,
            encodedGenotype.get(1).gene().allele()
        )

        assertEquals(
            1,
            encodedGenotype.get(2).gene().allele()
        )
    }

    @Test
    fun `genotype supports activity with exactly one assignment option`() {
        val candidates =
            AssignmentCandidateGenerationResult(
                candidatesByActivity = listOf(
                    ActivityCandidateOptions(
                        activityId = "activity-a",
                        options = listOf(
                            optionA(0)
                        )
                    )
                )
            )

        val codec =
            ScheduleGenotypeCodec(
                planningProblemId = "problem-1",
                candidateGenerationResult = candidates
            )

        val genotype =
            codec.createGenotype()

        assertEquals(
            1,
            genotype.length()
        )

        assertEquals(
            1,
            genotype.get(0).length()
        )

        // One option -> only index 0 is selectable.
        assertEquals(
            0,
            genotype.get(0).gene().allele()
        )

        // Jenetics represents it as [0, 1).
        assertEquals(
            0,
            genotype.get(0).gene().min()
        )

        assertEquals(
            1,
            genotype.get(0).gene().max()
        )
    }

    @Test
    fun `single option genotype decodes its only assignment`() {
        val option = optionA(0)

        val candidates =
            AssignmentCandidateGenerationResult(
                candidatesByActivity = listOf(
                    ActivityCandidateOptions(
                        activityId = "activity-a",
                        options = listOf(option)
                    )
                )
            )

        val codec =
            ScheduleGenotypeCodec(
                planningProblemId = "problem-1",
                candidateGenerationResult = candidates
            )

        val genotype =
            codec.createGenotype()

        val schedule =
            codec.decode(genotype)

        assertEquals(
            "problem-1",
            schedule.planningProblemId
        )

        assertEquals(
            listOf(assignmentFrom(option)),
            schedule.assignments
        )
    }

    @Test
    fun `codec rejects activity without assignment options`() {
        val candidates =
            AssignmentCandidateGenerationResult(
                candidatesByActivity = listOf(
                    ActivityCandidateOptions(
                        activityId = "activity-a",
                        options = listOf(
                            optionA(0)
                        )
                    ),
                    ActivityCandidateOptions(
                        activityId = "activity-b",
                        options = emptyList()
                    )
                )
            )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            ScheduleGenotypeCodec(
                planningProblemId = "problem-1",
                candidateGenerationResult = candidates
            )
        }
    }

    @Test
    fun `decode rejects genotype with wrong chromosome count`() {
        val codec = codec()

        val genotype =
            Genotype.of(
                IntegerChromosome.of(
                    IntegerGene.of(
                        0,
                        0,
                        3
                    )
                ),
                IntegerChromosome.of(
                    IntegerGene.of(
                        0,
                        0,
                        5
                    )
                )
            )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            codec.decode(genotype)
        }
    }

    @Test
    fun `encode rejects schedule from different planning problem`() {
        val codec = codec()

        val schedule = Schedule(
            planningProblemId = "other-problem",
            assignments = listOf(
                assignmentFrom(optionA(0)),
                assignmentFrom(optionB(0)),
                assignmentFrom(optionC(0))
            )
        )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            codec.encode(schedule)
        }
    }

    @Test
    fun `encode rejects incomplete schedule`() {
        val codec = codec()

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignmentFrom(optionA(0)),
                assignmentFrom(optionB(0))
            )
        )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            codec.encode(schedule)
        }
    }

    @Test
    fun `encode rejects duplicate assignment for same activity`() {
        val codec = codec()

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignmentFrom(optionA(0)),
                assignmentFrom(optionA(1)),
                assignmentFrom(optionB(0)),
                assignmentFrom(optionC(0))
            )
        )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            codec.encode(schedule)
        }
    }

    @Test
    fun `encode rejects assignment not present in candidate options`() {
        val codec = codec()

        val unknownAssignment = Assignment(
            activityId = "activity-a",
            timeSlotId = "unknown-slot",
            resourceAssignments = mapOf(
                "requirement-a" to listOf(
                    "resource-a"
                )
            ),
            locationId = "location-1"
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                unknownAssignment,
                assignmentFrom(optionB(0)),
                assignmentFrom(optionC(0))
            )
        )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            codec.encode(schedule)
        }
    }

    /*
     * Test fixtures
     */

    private fun codec(): ScheduleGenotypeCodec =
        ScheduleGenotypeCodec(
            planningProblemId = "problem-1",
            candidateGenerationResult = candidateResult()
        )

    private fun candidateResult() =
        AssignmentCandidateGenerationResult(
            candidatesByActivity = listOf(
                ActivityCandidateOptions(
                    activityId = "activity-a",
                    options = (0..2).map {
                        optionA(it)
                    }
                ),
                ActivityCandidateOptions(
                    activityId = "activity-b",
                    options = (0..4).map {
                        optionB(it)
                    }
                ),
                ActivityCandidateOptions(
                    activityId = "activity-c",
                    options = (0..1).map {
                        optionC(it)
                    }
                )
            )
        )

    private fun genotype(
        alleleA: Int,
        alleleB: Int,
        alleleC: Int
    ): Genotype<IntegerGene> =
        Genotype.of(
            IntegerChromosome.of(
                IntegerGene.of(
                    alleleA,
                    0,
                    3
                )
            ),
            IntegerChromosome.of(
                IntegerGene.of(
                    alleleB,
                    0,
                    5
                )
            ),
            IntegerChromosome.of(
                IntegerGene.of(
                    alleleC,
                    0,
                    2
                )
            )
        )

    private fun optionA(
        index: Int
    ) = AssignmentOption(
        activityId = "activity-a",
        timeSlotId = "slot-a-$index",
        resourceAssignments = mapOf(
            "requirement-a" to listOf(
                "resource-a-$index"
            )
        ),
        locationId = "location-1"
    )

    private fun optionB(
        index: Int
    ) = AssignmentOption(
        activityId = "activity-b",
        timeSlotId = "slot-b-$index",
        resourceAssignments = mapOf(
            "requirement-b" to listOf(
                "resource-b-$index"
            )
        ),
        locationId = "location-2"
    )

    private fun optionC(
        index: Int
    ) = AssignmentOption(
        activityId = "activity-c",
        timeSlotId = "slot-c-$index",
        resourceAssignments = mapOf(
            "requirement-c" to listOf(
                "resource-c-$index"
            )
        ),
        locationId = null
    )

    private fun assignmentFrom(
        option: AssignmentOption
    ) = Assignment(
        activityId = option.activityId,
        timeSlotId = option.timeSlotId,
        resourceAssignments = option.resourceAssignments,
        locationId = option.locationId
    )
}