
import { z } from 'zod';

const localDateTime = z
    .string()
    .min(1, 'Date and time are required.')
    .refine(
        (value) =>
            /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value) &&
            !Number.isNaN(new Date(value).getTime()) &&
            new Date(value).getFullYear() === Number(value.slice(0, 4)) &&
            new Date(value).getMonth() + 1 === Number(value.slice(5, 7)) &&
            new Date(value).getDate() === Number(value.slice(8, 10)),
        'Enter a valid date and time.',
    );

export const basicsSchema = z
    .object({
        name: z.string().trim().min(1, 'Planning name is required.'),
        horizonStart: localDateTime,
        horizonEnd: localDateTime,
    })
    .refine(
        (values) =>
            !values.horizonStart ||
            !values.horizonEnd ||
            values.horizonStart >= values.horizonEnd
                ? false
                : true,
        {
            path: ['horizonEnd'],
            message: 'End date must be after start date.',
        },
    );
