
import { Alert, Box, Typography } from '@mui/material';

import styles from '../components/PlanningWizard.module.css';

interface PendingStepProps {
    title: string;
}

export default function PendingStep({ title }: PendingStepProps) {
    return (
        <Box className={styles.stepContent}>
            <Typography variant="h5" component="h2">
                {title}
            </Typography>

            <Alert severity="info">
                This step will be implemented in a subsequent task.
                You can continue to explore the wizard navigation.
            </Alert>
        </Box>
    );
}
