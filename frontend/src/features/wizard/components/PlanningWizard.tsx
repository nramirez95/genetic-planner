
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

import styles from './PlanningWizard.module.css';

export default function PlanningWizard() {
    const [activeStep, setActiveStep] = useState(0);

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
        if (currentStep.id !== 'basics') {
            // Validation for these steps will be added
            // when their forms are implemented.
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
            case 'activities':
            case 'timeSlotsLocations':
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
