
import { z } from 'zod';

import type { AcademicWizardValues } from '../../wizard/types/wizard';

const lessonSchema = z.object({
    id: z.string().trim().min(1, 'Lesson ID is required.'),
    name: z.string().trim().min(1, 'Lesson name is required.'),
    teacherIds: z.array(z.string()),
    studentGroupIds: z.array(z.string()),
    allowedTeachingPeriodIds: z.array(z.string()).nullable(),
    allowedClassroomIds: z.array(z.string()).nullable(),
    requiredCapacity: z
        .number()
        .int('Capacity must be a whole number.')
        .min(0, 'Capacity cannot be negative.')
        .nullable(),
});

export const activitiesSchema = z.array(lessonSchema);

export function validateActivities(values: AcademicWizardValues) {
    const result = activitiesSchema.safeParse(values.lessons);

    if (!result.success) {
        return result;
    }

    const issues: Array<{
        index: number;
        field: keyof AcademicWizardValues['lessons'][number];
        message: string;
    }> = [];

    const lessonIds = new Set<string>();
    const teacherIds = new Set(values.teachers.map((item) => item.id));
    const groupIds = new Set(values.studentGroups.map((item) => item.id));
    const periodIds = new Set(values.teachingPeriods.map((item) => item.id));
    const classroomIds = new Set(values.classrooms.map((item) => item.id));

    values.lessons.forEach((lesson, index) => {
        const id = lesson.id.trim();

        if (id && lessonIds.has(id)) {
            issues.push({
                index,
                field: 'id',
                message: 'Another lesson already uses this ID.',
            });
        }
        lessonIds.add(id);

        const references = [
            {
                field: 'teacherIds' as const,
                selected: lesson.teacherIds,
                available: teacherIds,
                label: 'teacher',
            },
            {
                field: 'studentGroupIds' as const,
                selected: lesson.studentGroupIds,
                available: groupIds,
                label: 'student group',
            },
            {
                field: 'allowedTeachingPeriodIds' as const,
                selected: lesson.allowedTeachingPeriodIds ?? [],
                available: periodIds,
                label: 'teaching period',
            },
            {
                field: 'allowedClassroomIds' as const,
                selected: lesson.allowedClassroomIds ?? [],
                available: classroomIds,
                label: 'classroom',
            },
        ];

        references.forEach(({ field, selected, available, label }) => {
            if (selected.some((selectedId) => !available.has(selectedId))) {
                issues.push({
                    index,
                    field,
                    message: `This lesson references a ${label} that no longer exists. Remove the invalid selection using the X icon.`,
                });
            }
        });
    });

    return { success: true as const, issues };
}
