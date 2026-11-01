import {
    AppBar,
    Box,
    Button,
    IconButton,
    Toolbar,
    Typography,
} from '@mui/material';

import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AddIcon from '@mui/icons-material/Add';

import {
    Link as RouterLink,
    useLocation,
    matchPath,
} from 'react-router';

import styles from './AppHeader.module.css';

type HeaderPage = 'dashboard' | 'wizard' | 'results' | 'notFound';

function getHeaderPage(pathname: string): HeaderPage {
    if (pathname === '/') {
        return 'dashboard';
    }

    if (pathname === '/planning/new') {
        return 'wizard';
    }

    if (matchPath('/planning/:id/results', pathname)) {
        return 'results';
    }

    return 'notFound';
}

export default function AppHeader() {
    const { pathname } = useLocation();
    const page = getHeaderPage(pathname);

    const isDashboard = page === 'dashboard';

    const subtitle =
        page === 'wizard'
            ? 'New Planning'
            : page === 'results'
                ? 'Planning Results'
                : null;

    return (
        <AppBar
            position="static"
            color="inherit"
            elevation={0}
            className={styles.header}
        >
            <Toolbar className={styles.toolbar}>
                <Box className={styles.headerIdentity}>
                    {!isDashboard && (
                        <IconButton
                            component={RouterLink}
                            to="/"
                            aria-label="Back to Dashboard"
                            color="inherit"
                        >
                            <ArrowBackIcon />
                        </IconButton>
                    )}

                    <Box className={styles.titleGroup}>
                        <Typography
                            component={RouterLink}
                            to="/"
                            className={styles.brand}
                        >
                            Genetic Planner
                        </Typography>

                        {subtitle && (
                            <Typography
                                variant="subtitle2"
                                className={styles.subtitle}
                            >
                                {subtitle}
                            </Typography>
                        )}
                    </Box>
                </Box>

                {isDashboard && (
                    <Button
                        component={RouterLink}
                        to="/planning/new"
                        variant="contained"
                        startIcon={<AddIcon />}
                    >
                        New Planning
                    </Button>
                )}
            </Toolbar>
        </AppBar>
    );
}