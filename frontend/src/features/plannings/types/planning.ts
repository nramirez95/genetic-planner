
/**
 * Minimal representation of a planning configuration
 * used by the Dashboard.
 *
 * The backend returns additional configuration fields.
 * They will be modeled when the corresponding features
 * require them.
 */
export interface PlanningSummary {
    id: string;
    name: string;
    templateType: string | null;
    horizonStart: string;
    horizonEnd: string;
    resources: { id: string }[];
    activities: { id: string }[];
    timeSlots: { id: string }[];
    constraints: { id: string }[];
}
