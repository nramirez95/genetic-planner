
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Skeleton,
    Stack,
    Typography,
} from '@mui/material';

import {
    AddOutlined,
    CalendarMonthOutlined,
    RefreshOutlined,
} from '@mui/icons-material';

import type { PlanningSummary } from '../types/planning';
import { PlanningCard } from './PlanningCard';
import styles from './PlanningList.module.css';

interface PlanningListProps {
    plannings: PlanningSummary[];
    isLoading: boolean;
    isError: boolean;
    onRetry: () => void;
    onCreate: () => void;
    onOpen: (id: string) => void;
    onDelete: (planning: PlanningSummary) => void;
}

export function PlanningList({
                                 plannings,
                                 isLoading,
                                 isError,
                                 onRetry,
                                 onCreate,
                                 onOpen,
                                 onDelete,
                             }: PlanningListProps) {
    if (isLoading) {
        return (
            <Box className={styles.grid} aria-label="Loading plannings">
                {Array.from({ length: 3 }).map((_, index) => (
                    <Card key={index}>
                        <CardContent>
                            <Stack spacing={2}>
                                <Skeleton variant="rounded" width={44} height={44} />
                                <Skeleton variant="text" width="75%" height={32} />
                                <Skeleton variant="text" width="55%" />
                                <Skeleton variant="rounded" height={72} />
                                <Skeleton variant="rounded" height={36} />
                            </Stack>
                        </CardContent>
                    </Card>
                ))}
            </Box>
        );
    }

    if (isError) {
        return (
            <Alert
                severity="error"
                action={
                    <Button
                        color="inherit"
                        size="small"
                        startIcon={<RefreshOutlined />}
                        onClick={onRetry}
                    >
                        Retry
                    </Button>
                }
            >
                We couldn't load your plannings. Please try again.
            </Alert>
        );
    }

    if (plannings.length === 0) {
        return (
            <Box className={styles.emptyState}>
                <Box className={styles.emptyIcon}>
                    <CalendarMonthOutlined fontSize="large" />
                </Box>

                <Typography variant="h5" component="h3">
                    No plannings yet
                </Typography>

                <Typography variant="body2" color="text.secondary">
                    Create your first planning problem to get started.
                </Typography>

                <Button
                    variant="contained"
                    startIcon={<AddOutlined />}
                    onClick={onCreate}
                >
                    New Planning
                </Button>
            </Box>
        );
    }

    return (
        <Box className={styles.grid}>
            {plannings.map((planning) => (
                <PlanningCard
                    key={planning.id}
                    planning={planning}
                    onOpen={onOpen}
                    onDelete={onDelete}
                />
            ))}
        </Box>
    );
}
