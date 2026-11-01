
import { useState } from 'react';
import { useNavigate } from 'react-router';

import {
    Alert,
    Box,
    Button,
    Snackbar,
    Stack,
    Typography,
} from '@mui/material';

import {
    AddOutlined,
    CalendarMonthOutlined,
} from '@mui/icons-material';

import { PlanningList } from '../features/plannings/components/PlanningList';
import { DeletePlanningDialog } from '../features/plannings/components/DeletePlanningDialog';

import {
    useDeletePlanning,
    usePlannings,
} from '../features/plannings/hooks/usePlannings';

import type { PlanningSummary } from '../features/plannings/types/planning';

import styles from './DashboardPage.module.css';

export default function DashboardPage() {
    const navigate = useNavigate();

    const {
        data: plannings = [],
        isPending,
        isError,
        refetch,
    } = usePlannings();

    const deletePlanning = useDeletePlanning();

    const [planningToDelete, setPlanningToDelete] =
        useState<PlanningSummary | null>(null);

    const [notification, setNotification] = useState<string | null>(
        null
    );

    const handleCreate = () => {
        navigate('/planning/new');
    };

    const handleOpen = (id: string) => {
        navigate(`/planning/${encodeURIComponent(id)}`);
    };

    const handleDeleteRequest = (planning: PlanningSummary) => {
        deletePlanning.reset();
        setPlanningToDelete(planning);
    };

    const handleDeleteCancel = () => {
        if (deletePlanning.isPending) {
            return;
        }

        setPlanningToDelete(null);
        deletePlanning.reset();
    };

    const handleDeleteConfirm = async () => {
        if (!planningToDelete || deletePlanning.isPending) {
            return;
        }

        try {
            await deletePlanning.mutateAsync(planningToDelete.id);

            setPlanningToDelete(null);
            setNotification('Planning deleted successfully.');
        } catch {
            // The mutation exposes the error to the dialog.
        }
    };

    const handleRetry = () => {
        void refetch();
    };

    const deleteError = deletePlanning.isError
        ? 'We could not delete this planning. Please try again.'
        : null;

    return (
        <Box className={styles.dashboard}>
            <section className={styles.hero}>
                <Box className={styles.heroContent}>
                    <Box className={styles.heroText}>
                        <Typography
                            variant="h1"
                            component="h1"
                            className={styles.heroTitle}
                        >
                            Plan smarter, not harder.
                        </Typography>

                        <Typography
                            variant="body1"
                            className={styles.heroDescription}
                        >
                            Create, manage and optimize your planning
                            problems with genetic algorithms.
                        </Typography>

                        <Button
                            variant="contained"
                            className={styles.heroButton}
                            startIcon={<AddOutlined />}
                            onClick={handleCreate}
                        >
                            New Planning
                        </Button>
                    </Box>

                    <Box className={styles.heroDecoration} aria-hidden="true">
                        <CalendarMonthOutlined
                            className={styles.heroIcon}
                        />
                    </Box>
                </Box>
            </section>

            <section
                className={styles.planningsSection}
                aria-labelledby="plannings-heading"
            >
                <Box className={styles.sectionHeader}>
                    <Stack spacing={0.5}>
                        <Typography
                            id="plannings-heading"
                            variant="h4"
                            component="h2"
                        >
                            Your Plannings
                        </Typography>

                        <Typography variant="body2" color="text.secondary">
                            View and manage your existing planning problems.
                        </Typography>
                    </Stack>

                    {!isPending && !isError && (
                        <Typography
                            variant="body2"
                            color="text.secondary"
                            className={styles.planningCount}
                        >
                            {plannings.length}{' '}
                            {plannings.length === 1 ? 'planning' : 'plannings'}
                        </Typography>
                    )}
                </Box>

                <PlanningList
                    plannings={plannings}
                    isLoading={isPending}
                    isError={isError}
                    onRetry={handleRetry}
                    onCreate={handleCreate}
                    onOpen={handleOpen}
                    onDelete={handleDeleteRequest}
                />
            </section>

            <DeletePlanningDialog
                planning={planningToDelete}
                isDeleting={deletePlanning.isPending}
                error={deleteError}
                onClose={handleDeleteCancel}
                onConfirm={() => {
                    void handleDeleteConfirm();
                }}
            />

            <Snackbar
                open={Boolean(notification)}
                autoHideDuration={4000}
                onClose={() => setNotification(null)}
                anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'center',
                }}
            >
                <Alert
                    severity="success"
                    variant="filled"
                    onClose={() => setNotification(null)}
                >
                    {notification}
                </Alert>
            </Snackbar>
        </Box>
    );
}
