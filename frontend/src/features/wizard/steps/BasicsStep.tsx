
import { Box, TextField, Typography } from '@mui/material';
import { Controller, useFormContext } from 'react-hook-form';

import type { AcademicWizardValues } from '../types/wizard';

import styles from '../components/PlanningWizard.module.css';

export default function BasicsStep() {
    const {
        control,
        formState: { errors },
    } = useFormContext<AcademicWizardValues>();

    return (
        <Box className={styles.stepContent}>
            <Box>
                <Typography variant="h5" component="h2">
                    Planning basics
                </Typography>

                <Typography color="text.secondary">
                    Give your planning a name and define its time horizon.
                </Typography>
            </Box>

            <Controller
                name="name"
                control={control}
                render={({ field }) => (
                    <TextField
                        {...field}
                        label="Planning name"
                        placeholder="e.g. First semester 2026–2027"
                        required
                        fullWidth
                        error={Boolean(errors.name)}
                        helperText={errors.name?.message}
                    />
                )}
            />

            <Box className={styles.formGrid}>
                <Controller
                    name="horizonStart"
                    control={control}
                    render={({ field }) => (
                        <TextField
                            {...field}
                            label="Start date and time"
                            type="datetime-local"
                            required
                            fullWidth
                            slotProps={{
                                inputLabel: { shrink: true },
                            }}
                            error={Boolean(errors.horizonStart)}
                            helperText={errors.horizonStart?.message}
                        />
                    )}
                />

                <Controller
                    name="horizonEnd"
                    control={control}
                    render={({ field }) => (
                        <TextField
                            {...field}
                            label="End date and time"
                            type="datetime-local"
                            required
                            fullWidth
                            slotProps={{
                                inputLabel: { shrink: true },
                            }}
                            error={Boolean(errors.horizonEnd)}
                            helperText={errors.horizonEnd?.message}
                        />
                    )}
                />
            </Box>
        </Box>
    );
}
