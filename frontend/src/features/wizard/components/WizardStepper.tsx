
import {
    Step,
    StepLabel,
    Stepper,
} from '@mui/material';

import { WIZARD_STEPS } from '../types/wizard';

interface WizardStepperProps {
    activeStep: number;
}

export default function WizardStepper({
                                          activeStep,
                                      }: WizardStepperProps) {
    return (
        <Stepper activeStep={activeStep} orientation="vertical">
            {WIZARD_STEPS.map((step, index) => (
                <Step
                    key={step.id}
                    completed={index < activeStep}
                >
                    <StepLabel>{step.label}</StepLabel>
                </Step>
            ))}
        </Stepper>
    );
}
