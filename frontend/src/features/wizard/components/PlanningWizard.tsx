
import { useState } from 'react';
import { Box, Paper, Typography } from '@mui/material';
import { FormProvider, useForm } from 'react-hook-form';

import { basicsSchema } from '../schemas/basicsSchema';
import {
    WIZARD_STEPS,
    type AcademicWizardValues,
} from '../types/wizard';

import BasicsStep from '../steps/BasicsStep';
import PendingStep from '../steps/PendingStep';
import WizardStepper from './WizardStepper';
import WizardNavigation from './WizardNavigation';
import { resourcesSchema } from '../../resources/schemas/resourcesSchema';
import ResourcesStep from '../steps/ResourcesStep';
import ActivitiesStep from '../steps/ActivitiesStep';
import { validateActivities } from '../../activities/schemas/activitiesSchema';
import TimeSlotsLocationsStep, {
    type SchedulingTab,
} from '../steps/TimeSlotsLocationsStep';
import { validateTeachingPeriods } from '../../scheduling/schemas/teachingPeriodsSchema';

import { classroomsSchema } from '../../scheduling/schemas/classroomsSchema';

import styles from './PlanningWizard.module.css';

export default function PlanningWizard() {
    const [activeStep, setActiveStep] = useState(0);

    const [schedulingTab, setSchedulingTab] =
        useState<SchedulingTab>('periods');

    const [invalidPeriodIndex, setInvalidPeriodIndex] =
        useState<number | null>(null);

    const [validationRequestId, setValidationRequestId] =
        useState(0);

    const methods = useForm<AcademicWizardValues>({
        mode: 'onTouched',
        shouldUnregister: false,
        defaultValues: {
            id: crypto.randomUUID(),
            name: '',
            horizonStart: '',
            horizonEnd: '',
            teachers: [],
            studentGroups: [],
            lessons: [],
            classrooms: [],
            teachingPeriods: [],
        },
    });

    const {
        getValues,
        setError,
        clearErrors,
    } = methods;

    const currentStep = WIZARD_STEPS[activeStep];
    const lastStep = WIZARD_STEPS.length - 1;

    function validateCurrentStep(): boolean {
        if (currentStep.id === 'resources') {
            clearErrors(['teachers', 'studentGroups']);

            const values = getValues();

            const result = resourcesSchema.safeParse({
                teachers: values.teachers,
                studentGroups: values.studentGroups,
            });

            if (result.success) {
                return true;
            }

            for (const issue of result.error.issues) {
                const [collection, index, property] = issue.path;

                if (
                    collection !== 'teachers' &&
                    collection !== 'studentGroups'
                ) {
                    continue;
                }

                if (
                    typeof index === 'number' &&
                    (property === 'id' ||
                        property === 'name' ||
                        property === 'size')
                ) {
                    const path =
                        `${collection}.${index}.${property}` as
                            | `teachers.${number}.id`
                            | `teachers.${number}.name`
                            | `studentGroups.${number}.id`
                            | `studentGroups.${number}.name`
                            | `studentGroups.${number}.size`;

                    setError(path, {
                        type: 'validate',
                        message: issue.message,
                    });
                } else {
                    setError(collection, {
                        type: 'validate',
                        message: issue.message,
                    });
                }
            }

            return false;
        }

        if (currentStep.id === 'activities') {
            clearErrors('lessons');

            const values = getValues();
            const result = validateActivities(values);

            if (!result.success) {
                for (const issue of result.error.issues) {
                    const [index, property] = issue.path;

                    if (
                        typeof index === 'number' &&
                        typeof property === 'string'
                    ) {
                        setError(
                            `lessons.${index}.${property}` as
                                `lessons.${number}.${keyof AcademicWizardValues['lessons'][number]}`,
                            {
                                type: 'validate',
                                message: issue.message,
                            },
                        );
                    }
                }

                return false;
            }

            for (const issue of result.issues) {
                const path =
                    `lessons.${issue.index}.${issue.field}` as
                        `lessons.${number}.${keyof AcademicWizardValues['lessons'][number]}`;

                setError(path, {
                    type: 'validate',
                    message: issue.message,
                });
            }

            return result.issues.length === 0;
        }

        if (currentStep.id === 'timeSlotsLocations') {
            clearErrors(['classrooms', 'teachingPeriods']);

            const values = getValues();

            let isValid = true;
            let firstInvalidPeriodIndex: number | null = null;
            let hasClassroomErrors = false;

            const classroomsResult = classroomsSchema.safeParse(
                values.classrooms,
            );

            if (!classroomsResult.success) {
                isValid = false;
                hasClassroomErrors = true;

                for (const issue of classroomsResult.error.issues) {
                    const [index, property] = issue.path;

                    if (
                        typeof index === 'number' &&
                        (
                            property === 'id' ||
                            property === 'name' ||
                            property === 'capacity'
                        )
                    ) {
                        setError(
                            `classrooms.${index}.${property}` as
                                | `classrooms.${number}.id`
                                | `classrooms.${number}.name`
                                | `classrooms.${number}.capacity`,
                            {
                                type: 'validate',
                                message: issue.message,
                            },
                        );
                    }
                }
            }

            const periodsResult = validateTeachingPeriods(
                values.teachingPeriods,
                values.horizonStart,
                values.horizonEnd,
            );

            if (!periodsResult.success) {
                isValid = false;

                for (const issue of periodsResult.error.issues) {
                    const [index, property] = issue.path;

                    if (typeof index === 'number') {
                        firstInvalidPeriodIndex =
                            firstInvalidPeriodIndex === null
                                ? index
                                : Math.min(firstInvalidPeriodIndex, index);
                    }

                    if (
                        typeof index === 'number' &&
                        (
                            property === 'id' ||
                            property === 'start' ||
                            property === 'end' ||
                            property === 'label'
                        )
                    ) {
                        setError(
                            `teachingPeriods.${index}.${property}` as
                                | `teachingPeriods.${number}.id`
                                | `teachingPeriods.${number}.start`
                                | `teachingPeriods.${number}.end`
                                | `teachingPeriods.${number}.label`,
                            {
                                type: 'validate',
                                message: issue.message,
                            },
                        );
                    }
                }
            } else {
                for (const issue of periodsResult.issues) {
                    isValid = false;

                    firstInvalidPeriodIndex =
                        firstInvalidPeriodIndex === null
                            ? issue.index
                            : Math.min(firstInvalidPeriodIndex, issue.index);

                    setError(
                        `teachingPeriods.${issue.index}.${issue.field}` as
                            | `teachingPeriods.${number}.id`
                            | `teachingPeriods.${number}.start`
                            | `teachingPeriods.${number}.end`,
                        {
                            type: 'validate',
                            message: issue.message,
                        },
                    );
                }
            }

            if (!isValid) {
                // Prioritize teaching period errors.
                if (firstInvalidPeriodIndex !== null) {
                    setSchedulingTab('periods');
                    setInvalidPeriodIndex(firstInvalidPeriodIndex);
                } else if (hasClassroomErrors) {
                    setSchedulingTab('classrooms');
                    setInvalidPeriodIndex(null);
                }

                setValidationRequestId((current) => current + 1);
            } else {
                setInvalidPeriodIndex(null);
            }

            return isValid;
        }


        if (currentStep.id !== 'basics') {
            return true;
        }

        const values = getValues();

        clearErrors([
            'name',
            'horizonStart',
            'horizonEnd',
        ]);

        const result = basicsSchema.safeParse({
            name: values.name,
            horizonStart: values.horizonStart,
            horizonEnd: values.horizonEnd,
        });

        if (result.success) {
            return true;
        }

        for (const issue of result.error.issues) {
            const field = issue.path[0];

            if (
                field === 'name' ||
                field === 'horizonStart' ||
                field === 'horizonEnd'
            ) {
                setError(field, {
                    type: 'validate',
                    message: issue.message,
                });
            }
        }

        return false;
    }

    function handleNext() {
        if (!validateCurrentStep()) {
            return;
        }

        setActiveStep((previous) =>
            Math.min(previous + 1, lastStep),
        );
    }

    function handleBack() {
        setActiveStep((previous) =>
            Math.max(previous - 1, 0),
        );
    }

    function renderStep() {
        switch (currentStep.id) {
            case 'basics':
                return <BasicsStep />;
            case 'resources':
                return <ResourcesStep />;
            case 'activities':
                return <ActivitiesStep />;
            case 'timeSlotsLocations':
                return (
                    <TimeSlotsLocationsStep
                        activeTab={schedulingTab}
                        onTabChange={setSchedulingTab}
                        invalidPeriodIndex={invalidPeriodIndex}
                        validationRequestId={validationRequestId}
                    />
                );
            case 'constraints':
            case 'optimization':
            case 'review':
                return <PendingStep title={currentStep.label} />;
        }
    }

    return (
        <FormProvider {...methods}>
            <Box className={styles.wizard}>
                <Box className={styles.header}>
                    <Typography variant="h2" component="h1">
                        Academic Planning Wizard
                    </Typography>

                    <Typography color="text.secondary">
                        Configure your academic schedule step by step.
                    </Typography>
                </Box>

                <Box className={styles.layout}>
                    <Paper
                        variant="outlined"
                        className={styles.stepperPanel}
                    >
                        <WizardStepper activeStep={activeStep} />
                    </Paper>

                    <Paper
                        variant="outlined"
                        className={styles.contentPanel}
                    >
                        <Box className={styles.content}>
                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                Step {activeStep + 1} of {WIZARD_STEPS.length}
                            </Typography>

                            {renderStep()}
                        </Box>

                        <WizardNavigation
                            activeStep={activeStep}
                            lastStep={lastStep}
                            isValidating={false}
                            onBack={handleBack}
                            onNext={handleNext}
                        />
                    </Paper>
                </Box>
            </Box>
        </FormProvider>
    );
}
