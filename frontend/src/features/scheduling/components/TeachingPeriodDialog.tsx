
import { useState } from 'react';

import {
    Alert,
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    TextField,
} from '@mui/material';

import type { AcademicWizardValues } from '../../wizard/types/wizard';

import { teachingPeriodSchema } from '../schemas/teachingPeriodsSchema';

type TeachingPeriod =
    AcademicWizardValues['teachingPeriods'][number];

interface TeachingPeriodDialogProps {
    open: boolean;
    period: TeachingPeriod | null;
    existingPeriods: TeachingPeriod[];
    horizonStart: string;
    horizonEnd: string;
    onClose: () => void;
    onSave: (period: TeachingPeriod) => void;
}

const emptyPeriod: TeachingPeriod = {
    id: '',
    label: null,
    start: '',
    end: '',
};

type FieldName = keyof TeachingPeriod;

export default function TeachingPeriodDialog({
                                                 open,
                                                 period,
                                                 existingPeriods,
                                                 horizonStart,
                                                 horizonEnd,
                                                 onClose,
                                                 onSave,
                                             }: TeachingPeriodDialogProps) {
    const [values, setValues] = useState<TeachingPeriod>(
        () => period ? { ...period } : { ...emptyPeriod },
    );
    const [errors, setErrors] =
        useState<Partial<Record<FieldName, string>>>({});

    function updateField(
        field: FieldName,
        value: string | null,
    ) {
        setValues((current) => ({
            ...current,
            [field]: value,
        }));

        setErrors((current) => ({
            ...current,
            [field]: undefined,
        }));
    }

    function handleSave() {
        const result = teachingPeriodSchema.safeParse(values);

        if (!result.success) {
            const nextErrors: Partial<Record<FieldName, string>> = {};

            result.error.issues.forEach((issue) => {
                const field = issue.path[0] as FieldName;

                nextErrors[field] = issue.message;
            });

            setErrors(nextErrors);
            return;
        }

        const nextErrors: Partial<Record<FieldName, string>> = {};

        if (values.start >= values.end) {
            nextErrors.end = 'End date must be after start date.';
        }

        if (
            horizonStart &&
            horizonEnd &&
            (
                values.start < horizonStart ||
                values.end > horizonEnd
            )
        ) {
            nextErrors.start =
                'Teaching period must be within the planning horizon.';
        }

        const otherPeriods = existingPeriods;

        if (
            otherPeriods.some(
                (item) => item.id.trim() === values.id.trim(),
            )
        ) {
            nextErrors.id = 'Another period already uses this ID.';
        }

        if (
            otherPeriods.some(
                (item) =>
                    item.start === values.start &&
                    item.end === values.end,
            )
        ) {
            nextErrors.start =
                'Another period already has the same start and end.';
        }

        if (Object.keys(nextErrors).length > 0) {
            setErrors(nextErrors);
            return;
        }

        onSave(result.data);
        onClose();
    }

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle>
                {period ? 'Edit teaching period' : 'Add teaching period'}
            </DialogTitle>

            <DialogContent>
                <Box
                    sx={{
                        display: 'grid',
                        gridTemplateColumns: {
                            xs: '1fr',
                            sm: '1fr 1fr',
                        },
                        gap: 2,
                        pt: 1,
                    }}
                >
                    <TextField
                        label="Period ID"
                        required
                        fullWidth
                        size="small"
                        value={values.id}
                        onChange={(event) =>
                            updateField('id', event.target.value)
                        }
                        error={Boolean(errors.id)}
                        helperText={errors.id}
                    />

                    <TextField
                        label="Label"
                        fullWidth
                        size="small"
                        value={values.label ?? ''}
                        onChange={(event) =>
                            updateField('label', event.target.value || null)
                        }
                        helperText="Optional"
                    />

                    <TextField
                        label="Start"
                        type="datetime-local"
                        required
                        fullWidth
                        size="small"
                        value={values.start}
                        onChange={(event) =>
                            updateField('start', event.target.value)
                        }
                        slotProps={{
                            inputLabel: { shrink: true },
                        }}
                        error={Boolean(errors.start)}
                        helperText={errors.start}
                    />

                    <TextField
                        label="End"
                        type="datetime-local"
                        required
                        fullWidth
                        size="small"
                        value={values.end}
                        onChange={(event) =>
                            updateField('end', event.target.value)
                        }
                        slotProps={{
                            inputLabel: { shrink: true },
                        }}
                        error={Boolean(errors.end)}
                        helperText={errors.end}
                    />
                </Box>

                {Object.keys(errors).length > 0 && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        Review the highlighted fields.
                    </Alert>
                )}
            </DialogContent>

            <DialogActions>
                <Button onClick={onClose}>Cancel</Button>

                <Button variant="contained" onClick={handleSave}>
                    Save period
                </Button>
            </DialogActions>
        </Dialog>
    );
}
