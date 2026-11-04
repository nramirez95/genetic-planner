
export const WIZARD_STEPS = [
    { id: 'basics', label: 'Basics' },
    { id: 'resources', label: 'Resources' },
    { id: 'activities', label: 'Activities' },
    { id: 'timeSlotsLocations', label: 'Time Slots & Locations' },
    { id: 'constraints', label: 'Constraints' },
    { id: 'optimization', label: 'Optimization' },
    { id: 'review', label: 'Review' },
] as const;

export type WizardStepId =
    (typeof WIZARD_STEPS)[number]['id'];

export interface AcademicWizardValues {
    id: string;
    name: string;
    horizonStart: string;
    horizonEnd: string;

    teachers: Array<{ id: string; name: string }>;
    studentGroups: Array<{
        id: string;
        name: string;
        size: number | null;
    }>;
    lessons: Array<{
        id: string;
        name: string;
        teacherIds: string[];
        studentGroupIds: string[];
        allowedTeachingPeriodIds: string[] | null;
        allowedClassroomIds: string[] | null;
        requiredCapacity: number | null;
    }>;
    classrooms: Array<{
        id: string;
        name: string;
        capacity: number | null;
    }>;
    teachingPeriods: Array<{
        id: string;
        start: string;
        end: string;
        label: string | null;
    }>;
}
