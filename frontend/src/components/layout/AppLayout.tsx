import { Box, Container } from '@mui/material';
import { Outlet } from 'react-router';

import AppHeader from './AppHeader';
import styles from './AppLayout.module.css';

export default function AppLayout() {
    return (
        <Box className={styles.appLayout}>
            <AppHeader />

            <Container
                maxWidth="xl"
                className={styles.layoutContainer}
            >
                <Box component="main" className={styles.mainContent}>
                    <Outlet />
                </Box>
            </Container>
        </Box>
    );
}