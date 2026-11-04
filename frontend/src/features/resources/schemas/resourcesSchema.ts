
import { z } from 'zod';

const requiredText = z.string().trim().min(1, 'This field is required.');

const teacherSchema = z.object({
    id: requiredText,
    name: requiredText,
});

const studentGroupSchema = z.object({
    id: requiredText,
    name: requiredText,
    size: z
        .number()
        .int('Size must be a whole number.')
        .min(0, 'Size cannot be negative.')
        .nullable(),
});

function hasUniqueIds(items: Array<{ id: string }>): boolean {
    const ids = items.map((item) => item.id.trim());
    return new Set(ids).size === ids.length;
}

export const resourcesSchema = z
    .object({
        teachers: z.array(teacherSchema),
        studentGroups: z.array(studentGroupSchema),
    })
    .superRefine((values, context) => {
        if (!hasUniqueIds(values.teachers)) {
            context.addIssue({
                code: 'custom',
                path: ['teachers'],
                message: 'Teacher IDs must be unique.',
            });
        }

        if (!hasUniqueIds(values.studentGroups)) {
            context.addIssue({
                code: 'custom',
                path: ['studentGroups'],
                message: 'Student group IDs must be unique.',
            });
        }
    });
