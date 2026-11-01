
import { useNavigate, useParams } from 'react-router';

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Stack,
    Typography,
} from '@mui/material';

import {
    ArrowBackOutlined,
    CalendarMonthOutlined,
    RefreshOutlined,
} from '@mui/icons-material';

import { usePlanning } from '../features/plannings/hooks/usePlannings';

import styles from './PlanningDetailPage.module.css';

function formatDate(value: string): string {
    const date = value.split('T')[0];
    const [year, month, day] = date.split('-');

    if (!year || !month || !day) {
        return value;
    }

    return `${day}/${month}/${year}`;
}

export default function PlanningDetailPage() {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const {
        data: planning,
        isPending,
        isError,
        refetch,
    } = usePlanning(id ?? '');

    if (!id) {
        return (
            <Alert severity="error">
                Invalid planning identifier.
            </Alert>
        );
    }

    if (isPending) {
        return (
            <Box className={styles.loading}>
                <CircularProgress />
                <Typography color="text.secondary">
                    Loading planning...
                </Typography>
            </Box>
        );
    }

    if (isError || !planning) {
        return (
            <Stack spacing={2}>
                <Alert severity="error">
                    We couldn't load this planning.
                    It may have been deleted or is temporarily unavailable.
                </Alert>

                <Stack direction="row" spacing={2}>
                    <Button
                        variant="outlined"
                        startIcon={<ArrowBackOutlined />}
                        onClick={() => navigate('/')}
                    >
                        Back to Dashboard
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<RefreshOutlined />}
                        onClick={() => void refetch()}
                    >
                        Retry
                    </Button>
                </Stack>
            </Stack>
        );
    }

    const metrics = [
        { label: 'Activities', value: planning.activities.length },
        { label: 'Resources', value: planning.resources.length },
        { label: 'Time slots', value: planning.timeSlots.length },
        { label: 'Constraints', value: planning.constraints.length },
    ];

    return (
        <Box className={styles.page}>
            <Button
                color="inherit"
                startIcon={<ArrowBackOutlined />}
                onClick={() => navigate('/')}
                className={styles.backButton}
            >
                Back to Dashboard
            </Button>

            <Box className={styles.header}>
                <Box>
                    <Typography variant="h2" component="h1">
                        {planning.name}
                    </Typography>

                    <Typography variant="body1" color="text.secondary">
                        Planning configuration
                    </Typography>
                </Box>

                <Chip
                    label={planning.templateType ?? 'Custom'}
                    variant="outlined"
                    color="secondary"
                />
            </Box>

            <Box className={styles.metrics}>
                {metrics.map((metric) => (
                    <Card key={metric.label}>
                        <CardContent>
                            <Typography variant="h3">
                                {metric.value}
                            </Typography>

                            <Typography variant="body2" color="text.secondary">
                                {metric.label}
                            </Typography>
                        </CardContent>
                    </Card>
                ))}
            </Box>

            <Card>
                <CardContent>
                    <Stack spacing={3}>
                        <Typography variant="h5" component="h2">
                            Planning details
                        </Typography>

                        <Box className={styles.detailRow}>
                            <Typography color="text.secondary">
                                Planning ID
                            </Typography>

                            <Typography className={styles.detailValue}>
                                {planning.id}
                            </Typography>
                        </Box>

                        <Box className={styles.detailRow}>
                            <Typography color="text.secondary">
                                Template
                            </Typography>

                            <Typography>
                                {planning.templateType ?? 'Custom'}
                            </Typography>
                        </Box>

                        <Box className={styles.detailRow}>
                            <Typography color="text.secondary">
                                Planning horizon
                            </Typography>

                            <Box className={styles.horizon}>
                                <CalendarMonthOutlined
                                    fontSize="small"
                                    color="secondary"
                                />

                                <Typography>
                                    {formatDate(planning.horizonStart)}
                                    {' — '}
                                    {formatDate(planning.horizonEnd)}
                                </Typography>
                            </Box>
                        </Box>
                    </Stack>
                </CardContent>
            </Card>
        </Box>
    );
}
