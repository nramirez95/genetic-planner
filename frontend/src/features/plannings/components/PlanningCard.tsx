
import {
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Stack,
    Typography,
} from '@mui/material';

import {
    CalendarMonthOutlined, Delete,
    EditOutlined,
} from '@mui/icons-material';

import type { PlanningSummary } from '../types/planning';
import styles from './PlanningCard.module.css';

interface PlanningCardProps {
    planning: PlanningSummary;
    onOpen: (id: string) => void;
    onDelete: (planning: PlanningSummary) => void;
}

function formatDate(value: string): string {
    const [date] = value.split('T');

    if (!date) {
        return value;
    }

    const [year, month, day] = date.split('-');

    if (!year || !month || !day) {
        return value;
    }

    return `${day}/${month}/${year}`;
}

function getTemplateLabel(templateType: string | null): string {
    if (!templateType) {
        return 'Custom';
    }

    switch (templateType.toUpperCase()) {
        case 'ACADEMIC':
            return 'Academic';
        case 'WORK_SHIFT':
            return 'Work Shift';
        default:
            return templateType;
    }
}

export function PlanningCard({
                                 planning,
                                 onOpen,
                                 onDelete,
                             }: PlanningCardProps) {
    return (
        <Card className={styles.card}>
            <CardContent className={styles.content}>
                <Stack spacing={2}>
                    <Box className={styles.header}>
                        <Box className={styles.iconContainer}>
                            <CalendarMonthOutlined />
                        </Box>

                        <Chip
                            label={getTemplateLabel(planning.templateType)}
                            size="small"
                            variant="outlined"
                        />
                    </Box>

                    <Box>
                        <Typography
                            variant="h6"
                            component="h3"
                            className={styles.title}
                        >
                            {planning.name}
                        </Typography>

                        <Typography variant="body2" color="text.secondary">
                            {formatDate(planning.horizonStart)}
                            {' — '}
                            {formatDate(planning.horizonEnd)}
                        </Typography>
                    </Box>

                    <Box className={styles.metrics}>
                        <Box>
                            <Typography variant="h6">
                                {planning.activities.length}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                                Activities
                            </Typography>
                        </Box>

                        <Box>
                            <Typography variant="h6">
                                {planning.resources.length}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                                Resources
                            </Typography>
                        </Box>
                    </Box>

                    <Box className={styles.actions}>
                        <Button
                            variant="contained"
                            size="small"
                            startIcon={<EditOutlined />}
                            onClick={() => onOpen(planning.id)}
                        >
                            Open
                        </Button>

                        <Button
                            variant="outlined"
                            color="error"
                            size="small"
                            startIcon={<Delete />}
                            onClick={() => onDelete(planning)}
                            aria-label={`Delete ${planning.name}`}
                        >
                            Delete
                        </Button>
                    </Box>
                </Stack>
            </CardContent>
        </Card>
    );
}
