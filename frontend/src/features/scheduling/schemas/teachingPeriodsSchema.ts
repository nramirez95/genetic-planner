
import { z } from 'zod';

import type { AcademicWizardValues } from '../../wizard/types/wizard';

type TeachingPeriod = AcademicWizardValues['teachingPeriods'][number];

const localDateTimePattern =
    /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/;

function isValidLocalDateTime(value: string): boolean {
    if (!localDateTimePattern.test(value)) {
        return false;
    }

    const [datePart, timePart] = value.split('T');
    const [year, month, day] = datePart.split('-').map(Number);
    const [hour, minute] = timePart.split(':').map(Number);

    if (
        year < 1 ||
        month < 1 ||
        month > 12 ||
        hour > 23 ||
        minute > 59
    ) {
        return false;
    }

    const date = new Date(0);
    date.setFullYear(year, month - 1, day);
    date.setHours(12, 0, 0, 0);

    return (
        date.getFullYear() === year &&
        date.getMonth() === month - 1 &&
        date.getDate() === day
    );
}

const requiredDateTime = z
    .string()
    .min(1, 'Date and time are required.')
    .refine(
        isValidLocalDateTime,
        'Enter a valid date and time.',
    );

export const teachingPeriodSchema = z.object({
    id: z.string().trim().min(1, 'Period ID is required.'),
    start: requiredDateTime,
    end: requiredDateTime,
    label: z.string().nullable(),
});

export const teachingPeriodsSchema = z.array(teachingPeriodSchema);

export function validateTeachingPeriods(
    periods: TeachingPeriod[],
    horizonStart: string,
    horizonEnd: string,
) {
    const result = teachingPeriodsSchema.safeParse(periods);

    if (!result.success) {
        return result;
    }

    const issues: Array<{
        index: number;
        field: 'id' | 'start' | 'end';
        message: string;
    }> = [];

    const seenIds = new Set<string>();
    const seenIntervals = new Set<string>();

    const validHorizon =
        isValidLocalDateTime(horizonStart) &&
        isValidLocalDateTime(horizonEnd) &&
        horizonStart < horizonEnd;

    periods.forEach((period, index) => {
        const id = period.id.trim();

        if (id && seenIds.has(id)) {
            issues.push({
                index,
                field: 'id',
                message: 'Another teaching period already uses this ID.',
            });
        }

        seenIds.add(id);

        if (period.start >= period.end) {
            issues.push({
                index,
                field: 'end',
                message: 'End date must be after start date.',
            });
        }

        if (
            validHorizon &&
            (
                period.start < horizonStart ||
                period.end > horizonEnd
            )
        ) {
            issues.push({
                index,
                field: 'start',
                message:
                    'Teaching period must be within the planning horizon.',
            });
        }

        const intervalKey = `${period.start}|${period.end}`;

        if (seenIntervals.has(intervalKey)) {
            issues.push({
                index,
                field: 'start',
                message:
                    'Another teaching period already has the same start and end.',
            });
        }

        seenIntervals.add(intervalKey);
    });

    return {
        success: true as const,
        issues,
    };
}
