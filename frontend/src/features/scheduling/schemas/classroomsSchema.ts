
import { z } from 'zod';

export const classroomSchema = z.object({
    id: z.string().trim().min(1, 'Classroom ID is required.'),
    name: z.string().trim().min(1, 'Classroom name is required.'),
    capacity: z
        .number()
        .int('Capacity must be a whole number.')
        .min(0, 'Capacity cannot be negative.')
        .nullable(),
});

export const classroomsSchema = z
    .array(classroomSchema)
    .superRefine((classrooms, context) => {
        const seenIds = new Set<string>();

        classrooms.forEach((classroom, index) => {
            const id = classroom.id.trim();

            if (!id) {
                return;
            }

            if (seenIds.has(id)) {
                context.addIssue({
                    code: 'custom',
                    path: [index, 'id'],
                    message: 'Another classroom already uses this ID.',
                });
            }

            seenIds.add(id);
        });
    });
