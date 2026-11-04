
import { Button } from '@mui/material';
import {
    ArrowBackOutlined,
    ArrowForwardOutlined,
} from '@mui/icons-material';

import styles from './PlanningWizard.module.css';

interface WizardNavigationProps {
    activeStep: number;
    lastStep: number;
    isValidating: boolean;
    onBack: () => void;
    onNext: () => void;
}

export default function WizardNavigation({
                                             activeStep,
                                             lastStep,
                                             isValidating,
                                             onBack,
                                             onNext,
                                         }: WizardNavigationProps) {
    return (
        <div className={styles.navigation}>
            <Button
                variant="outlined"
                startIcon={<ArrowBackOutlined />}
                disabled={activeStep === 0 || isValidating}
                onClick={onBack}
            >
                Back
            </Button>

            {activeStep < lastStep && (
                <Button
                    variant="contained"
                    endIcon={<ArrowForwardOutlined />}
                    disabled={isValidating}
                    onClick={onNext}
                >
                    Next
                </Button>
            )}
        </div>
    );
}
