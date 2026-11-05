
import { z } from 'zod';

export const MAX_RECURRING_PERIODS = 500;

export const WEEKDAYS = [
    { value: 1, label: 'Mon' },
    { value: 2, label: 'Tue' },
    { value: 3, label: 'Wed' },
    { value: 4, label: 'Thu' },
    { value: 5, label: 'Fri' },
    { value: 6, label: 'Sat' },
    { value: 0, label: 'Sun' },
] as const;

const datePattern = /^\d{4}-\d{2}-\d{2}$/;
const timePattern = /^([01]\d|2[0-3]):[0-5]\d$/;

export function isValidDate(value: string): boolean {
    if (!datePattern.test(value)) return false;

    const [year, month, day] = value.split('-').map(Number);

    if (year < 1 || month < 1 || month > 12) {
        return false;
    }

    const date = new Date(0);
    date.setUTCFullYear(year, month - 1, day);
    date.setUTCHours(12, 0, 0, 0);

    return (
        date.getUTCFullYear() === year &&
        date.getUTCMonth() === month - 1 &&
        date.getUTCDate() === day
    );
}

export const recurringPeriodsSchema = z
    .object({
        startDate: z.string().refine(
            isValidDate,
            'Enter a valid start date.',
        ),
        endDate: z.string().refine(
            isValidDate,
            'Enter a valid end date.',
        ),
        weekdays: z.array(z.number().int().min(0).max(6))
            .min(1, 'Select at least one weekday.'),
        timeRanges: z.array(
            z.object({
                start: z.string().regex(
                    timePattern,
                    'Enter a valid start time.',
                ),
                end: z.string().regex(
                    timePattern,
                    'Enter a valid end time.',
                ),
            }),
        ).min(1, 'Add at least one time range.'),
    })
    .superRefine((values, context) => {
        if (
            isValidDate(values.startDate) &&
            isValidDate(values.endDate) &&
            values.startDate > values.endDate
        ) {
            context.addIssue({
                code: 'custom',
                path: ['endDate'],
                message: 'End date must be on or after start date.',
            });
        }

        values.timeRanges.forEach((range, index) => {
            if (
                timePattern.test(range.start) &&
                timePattern.test(range.end) &&
                range.start >= range.end
            ) {
                context.addIssue({
                    code: 'custom',
                    path: ['timeRanges', index, 'end'],
                    message: 'End time must be after start time.',
                });
            }
        });
    });

export type RecurringPeriodsValues =
    z.infer<typeof recurringPeriodsSchema>;
