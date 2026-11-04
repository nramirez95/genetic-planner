
import { Box } from '@mui/material';

import TeacherEditor from './TeacherEditor';
import StudentGroupEditor from './StudentGroupEditor';

import styles from './ResourceEditor.module.css';

export default function ResourceEditor() {
    return (
        <Box className={styles.editor}>
            <TeacherEditor />
            <StudentGroupEditor />
        </Box>
    );
}
