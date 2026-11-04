
import { Box, Typography } from '@mui/material';

import ResourceEditor from '../../resources/components/ResourceEditor';
import styles from '../components/PlanningWizard.module.css';

export default function ResourcesStep() {
    return (
        <Box className={styles.stepContent}>
            <Box>
                <Typography variant="h5" component="h2">
                    Academic resources
                </Typography>

                <Typography color="text.secondary">
                    Configure the teachers and student groups involved
                    in your academic planning.
                </Typography>
            </Box>

            <ResourceEditor />
        </Box>
    );
}
