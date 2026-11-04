import { useState } from 'react';
import {
    Box,
    Button,
    Card,
    CardActionArea,
    CardContent,
    Chip,
    Typography,
} from '@mui/material';
import {
    ArrowForwardOutlined,
    CalendarMonthOutlined,
    CheckCircleOutlined,
    GroupsOutlined,
} from '@mui/icons-material';

import styles from './TemplateSelector.module.css';

type TemplateType = 'ACADEMIC' | 'WORK_SHIFT';

interface TemplateSelectorProps {
    onContinue: (template: TemplateType) => void;
}

export default function TemplateSelector({
                                             onContinue,
                                         }: TemplateSelectorProps) {
    const [selected, setSelected] = useState<TemplateType | null>(null);

    return (
        <Box className={styles.container}>
            <Box className={styles.header}>
                <Typography variant="h2" component="h1">
                    Choose a template
                </Typography>

                <Typography color="text.secondary">
                    Select the type of planning you want to create.
                    We'll guide you through the configuration.
                </Typography>
            </Box>

            <Box className={styles.grid}>
                <Card
                    variant="outlined"
                    className={`${styles.card} ${
                        selected === 'ACADEMIC' ? styles.selected : ''
                    }`}
                >
                    <CardActionArea
                        onClick={() => setSelected('ACADEMIC')}
                        className={styles.cardAction}
                    >
                        <CardContent className={styles.cardContent}>
                            <Box className={styles.cardTop}>
                                <Box className={styles.iconContainer}>
                                    <CalendarMonthOutlined fontSize="large" />
                                </Box>

                                {selected === 'ACADEMIC' && (
                                    <CheckCircleOutlined color="success" />
                                )}
                            </Box>

                            <Typography variant="h5" component="h2">
                                Academic Planning
                            </Typography>

                            <Typography variant="body2" color="text.secondary">
                                Create academic schedules by configuring teachers,
                                student groups, lessons, teaching periods and classrooms.
                            </Typography>

                            <Chip
                                label="Available"
                                size="small"
                                color="success"
                                variant="outlined"
                            />
                        </CardContent>
                    </CardActionArea>
                </Card>

                <Card
                    variant="outlined"
                    className={`${styles.card} ${styles.disabled}`}
                >
                    <CardContent className={styles.cardContent}>
                        <Box className={styles.cardTop}>
                            <Box className={styles.iconContainer}>
                                <GroupsOutlined fontSize="large" />
                            </Box>
                        </Box>

                        <Typography variant="h5" component="h2">
                            Work Shift Planning
                        </Typography>

                        <Typography variant="body2" color="text.secondary">
                            Organize employee shifts, availability and workplace
                            assignments.
                        </Typography>

                        <Chip
                            label="Coming soon"
                            size="small"
                            variant="outlined"
                        />
                    </CardContent>
                </Card>
            </Box>

            <Box className={styles.actions}>
                <Button
                    variant="contained"
                    size="large"
                    endIcon={<ArrowForwardOutlined />}
                    disabled={selected !== 'ACADEMIC'}
                    onClick={() => {
                        if (selected) {
                            onContinue(selected);
                        }
                    }}
                >
                    Continue
                </Button>
            </Box>
        </Box>
    );
}