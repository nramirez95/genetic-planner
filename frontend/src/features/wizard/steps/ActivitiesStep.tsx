
import { Box, Typography } from '@mui/material';

import ActivityEditor from '../../activities/components/ActivityEditor';
import styles from '../components/PlanningWizard.module.css';

export default function ActivitiesStep() {
    return (
        <Box className={styles.stepContent}>
            <Box>
                <Typography variant="h5" component="h2">
                    Academic activities
                </Typography>

                <Typography color="text.secondary">
                    Create lessons, assign teachers and student groups,
                    and configure allowed periods and classrooms.
                </Typography>
            </Box>

            <ActivityEditor />
        </Box>
    );
}
