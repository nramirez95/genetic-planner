
import type { AcademicWizardValues } from '../../wizard/types/wizard';

import {
    MAX_RECURRING_PERIODS,
    recurringPeriodsSchema,
    type RecurringPeriodsValues,
} from '../schemas/recurringPeriodsSchema';

type TeachingPeriod =
    AcademicWizardValues['teachingPeriods'][number];

export interface GenerationResult {
    total: number;
    duplicates: number;
    periods: TeachingPeriod[];
}

function toUtcDate(value: string): Date {
    const [year, month, day] = value.split('-').map(Number);

    const date = new Date(0);
    date.setUTCFullYear(year, month - 1, day);
    date.setUTCHours(12, 0, 0, 0);

    return date;
}

function formatDate(date: Date): string {
    const year = String(date.getUTCFullYear()).padStart(4, '0');
    const month = String(date.getUTCMonth() + 1).padStart(2, '0');
    const day = String(date.getUTCDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

function intervalKey(
    start: string,
    end: string,
): string {
    return `${start}|${end}`;
}

function buildPeriodId(
    start: string,
    end: string,
): string {
    return `TP-${start.replace(/\D/g, '')}-${end
        .slice(11)
        .replace(/\D/g, '')}`;
}

export function generateRecurringPeriods(
    values: RecurringPeriodsValues,
    existingPeriods: TeachingPeriod[],
    horizonStart: string,
    horizonEnd: string,
): GenerationResult {
    const validation = recurringPeriodsSchema.safeParse(values);

    if (!validation.success) {
        throw new Error('Invalid recurring period configuration.');
    }

    if (
        !horizonStart ||
        !horizonEnd ||
        horizonStart >= horizonEnd
    ) {
        throw new Error('Configure a valid planning horizon first.');
    }

    const existingIntervals = new Set(
        existingPeriods.map((period) =>
            intervalKey(period.start, period.end),
        ),
    );

    const usedIds = new Set(
        existingPeriods.map((period) => period.id),
    );

    const selectedDays = new Set(values.weekdays);

    const periods: TeachingPeriod[] = [];

    let total = 0;
    let duplicates = 0;

    const currentDate = toUtcDate(values.startDate);
    const endDate = toUtcDate(values.endDate);

    while (currentDate <= endDate) {
        if (selectedDays.has(currentDate.getUTCDay())) {
            const date = formatDate(currentDate);

            for (const range of values.timeRanges) {
                const start = `${date}T${range.start}`;
                const end = `${date}T${range.end}`;

                // The limit applies to all generated candidates,
                // including duplicates and out-of-horizon periods.
                total += 1;

                if (total > MAX_RECURRING_PERIODS) {
                    throw new Error(
                        `A maximum of ${MAX_RECURRING_PERIODS} periods ` +
                        'can be generated at once.',
                    );
                }

                if (start < horizonStart || end > horizonEnd) {
                    throw new Error(
                        'Generated periods must be within the planning horizon.',
                    );
                }

                const key = intervalKey(start, end);

                if (existingIntervals.has(key)) {
                    duplicates += 1;
                    continue;
                }

                existingIntervals.add(key);

                const baseId = buildPeriodId(start, end);
                let id = baseId;
                let suffix = 2;

                while (usedIds.has(id)) {
                    id = `${baseId}-${suffix}`;
                    suffix += 1;
                }

                usedIds.add(id);

                periods.push({
                    id,
                    start,
                    end,
                    label: null,
                });
            }
        }

        currentDate.setUTCDate(currentDate.getUTCDate() + 1);
    }

    return {
        total,
        duplicates,
        periods,
    };
}
