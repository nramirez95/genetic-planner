
import { useMemo, useState } from 'react';

import {
    Alert,
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    IconButton,
    TextField,
    Typography,
} from '@mui/material';

import {
    AddOutlined, Delete,
} from '@mui/icons-material';

import type { AcademicWizardValues } from '../../wizard/types/wizard';

import {
    WEEKDAYS,
    recurringPeriodsSchema,
    type RecurringPeriodsValues,
} from '../schemas/recurringPeriodsSchema';

import {
    generateRecurringPeriods,
} from '../utils/generateRecurringPeriods';

type TeachingPeriod =
    AcademicWizardValues['teachingPeriods'][number];

interface RecurringPeriodDialogProps {
    open: boolean;
    onClose: () => void;
    onGenerate: (periods: TeachingPeriod[]) => void;
    existingPeriods: TeachingPeriod[];
    horizonStart: string;
    horizonEnd: string;
}

const initialValues: RecurringPeriodsValues = {
    startDate: '',
    endDate: '',
    weekdays: [1, 2, 3, 4, 5],
    timeRanges: [{ start: '08:00', end: '09:00' }],
};

export default function RecurringPeriodDialog({
                                                  open,
                                                  onClose,
                                                  onGenerate,
                                                  existingPeriods,
                                                  horizonStart,
                                                  horizonEnd,
                                              }: RecurringPeriodDialogProps) {
    const [values, setValues] =
        useState<RecurringPeriodsValues>(initialValues);

    const [showErrors, setShowErrors] = useState(false);

    const validation = useMemo(
        () => recurringPeriodsSchema.safeParse(values),
        [values],
    );

    const preview = useMemo(() => {
        if (!validation.success) {
            return {
                result: null,
                error: null,
            };
        }

        try {
            return {
                result: generateRecurringPeriods(
                    values,
                    existingPeriods,
                    horizonStart,
                    horizonEnd,
                ),
                error: null,
            };
        } catch (error) {
            return {
                result: null,
                error:
                    error instanceof Error
                        ? error.message
                        : 'Unable to generate periods.',
            };
        }
    }, [
        values,
        validation,
        existingPeriods,
        horizonStart,
        horizonEnd,
    ]);

    function fieldError(path: string): string | undefined {
        if (!showErrors || validation.success) {
            return undefined;
        }

        const issue = validation.error.issues.find(
            (item) => item.path.join('.') === path,
        );

        return issue?.message;
    }

    function updateValues(
        patch: Partial<RecurringPeriodsValues>,
    ) {
        setValues((current) => ({
            ...current,
            ...patch,
        }));
    }

    function closeDialog() {
        setShowErrors(false);
        setValues({
            ...initialValues,
            weekdays: [...initialValues.weekdays],
            timeRanges: initialValues.timeRanges.map(
                (range) => ({ ...range }),
            ),
        });
        onClose();
    }

    function confirmGeneration() {
        setShowErrors(true);

        if (!validation.success || !preview.result) {
            return;
        }

        onGenerate(preview.result.periods);
        closeDialog();
    }

    return (
        <Dialog
            open={open}
            onClose={closeDialog}
            fullWidth
            maxWidth="sm"
        >
            <DialogTitle>
                Generate recurring teaching periods
            </DialogTitle>

            <DialogContent>
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 3,
                        pt: 1,
                    }}
                >
                    <Typography variant="body2" color="text.secondary">
                        Select a date range, weekdays and daily time ranges.
                        Generated periods will be added only after confirmation.
                    </Typography>

                    <Box sx={{ display: 'flex', gap: 2 }}>
                        <TextField
                            label="Start date"
                            type="date"
                            size="small"
                            fullWidth
                            value={values.startDate}
                            onChange={(event) =>
                                updateValues({
                                    startDate: event.target.value,
                                })
                            }
                            slotProps={{
                                inputLabel: { shrink: true },
                            }}
                            error={Boolean(fieldError('startDate'))}
                            helperText={fieldError('startDate')}
                        />

                        <TextField
                            label="End date"
                            type="date"
                            size="small"
                            fullWidth
                            value={values.endDate}
                            onChange={(event) =>
                                updateValues({
                                    endDate: event.target.value,
                                })
                            }
                            slotProps={{
                                inputLabel: { shrink: true },
                            }}
                            error={Boolean(fieldError('endDate'))}
                            helperText={fieldError('endDate')}
                        />
                    </Box>

                    <Box>
                        <Typography variant="subtitle2">
                            Repeat on
                        </Typography>

                        <Box sx={{ display: 'flex', flexWrap: 'wrap' }}>
                            {WEEKDAYS.map((day) => (
                                <FormControlLabel
                                    key={day.value}
                                    label={day.label}
                                    control={
                                        <Checkbox
                                            checked={values.weekdays.includes(day.value)}
                                            onChange={(event) => {
                                                const weekdays = event.target.checked
                                                    ? [...values.weekdays, day.value]
                                                    : values.weekdays.filter(
                                                        (value) => value !== day.value,
                                                    );

                                                updateValues({ weekdays });
                                            }}
                                        />
                                    }
                                />
                            ))}
                        </Box>

                        {fieldError('weekdays') && (
                            <Typography color="error" variant="caption">
                                {fieldError('weekdays')}
                            </Typography>
                        )}
                    </Box>

                    <Box>
                        <Typography variant="subtitle2" sx={{ mb: 1 }}>
                            Daily time ranges
                        </Typography>

                        <Box
                            sx={{
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 2,
                            }}
                        >
                            {values.timeRanges.map((range, index) => (
                                <Box
                                    key={index}
                                    sx={{
                                        display: 'flex',
                                        alignItems: 'flex-start',
                                        gap: 1,
                                    }}
                                >
                                    <TextField
                                        label="Start"
                                        type="time"
                                        size="small"
                                        fullWidth
                                        value={range.start}
                                        slotProps={{
                                            inputLabel: { shrink: true },
                                        }}
                                        onChange={(event) => {
                                            const timeRanges = [...values.timeRanges];

                                            timeRanges[index] = {
                                                ...range,
                                                start: event.target.value,
                                            };

                                            updateValues({ timeRanges });
                                        }}
                                        error={Boolean(
                                            fieldError(`timeRanges.${index}.start`),
                                        )}
                                        helperText={
                                            fieldError(`timeRanges.${index}.start`)
                                        }
                                    />

                                    <TextField
                                        label="End"
                                        type="time"
                                        size="small"
                                        fullWidth
                                        value={range.end}
                                        slotProps={{
                                            inputLabel: { shrink: true },
                                        }}
                                        onChange={(event) => {
                                            const timeRanges = [...values.timeRanges];

                                            timeRanges[index] = {
                                                ...range,
                                                end: event.target.value,
                                            };

                                            updateValues({ timeRanges });
                                        }}
                                        error={Boolean(
                                            fieldError(`timeRanges.${index}.end`),
                                        )}
                                        helperText={
                                            fieldError(`timeRanges.${index}.end`)
                                        }
                                    />

                                    <IconButton
                                        color="error"
                                        aria-label={`Remove time range ${index + 1}`}
                                        disabled={values.timeRanges.length === 1}
                                        onClick={() =>
                                            updateValues({
                                                timeRanges: values.timeRanges.filter(
                                                    (_, currentIndex) => currentIndex !== index,
                                                ),
                                            })
                                        }
                                    >
                                        <Delete />
                                    </IconButton>
                                </Box>
                            ))}
                        </Box>

                        <Button
                            sx={{ mt: 2 }}
                            variant="outlined"
                            startIcon={<AddOutlined />}
                            onClick={() =>
                                updateValues({
                                    timeRanges: [
                                        ...values.timeRanges,
                                        { start: '', end: '' },
                                    ],
                                })
                            }
                        >
                            Add time range
                        </Button>
                    </Box>

                    {preview.error && (
                        <Alert severity="error">
                            {preview.error}
                        </Alert>
                    )}

                    {preview.result && (
                        <Alert severity="info">
                            {preview.result.total} candidates ·{' '}
                            {preview.result.duplicates} duplicates ·{' '}
                            {preview.result.periods.length} new periods
                        </Alert>
                    )}

                    {!validation.success && showErrors && (
                        <Alert severity="warning">
                            Review the highlighted fields before generating.
                        </Alert>
                    )}
                </Box>
            </DialogContent>

            <DialogActions>
                <Button onClick={closeDialog}>
                    Cancel
                </Button>

                <Button
                    variant="contained"
                    onClick={confirmGeneration}
                    disabled={
                        !validation.success ||
                        !preview.result ||
                        preview.result.periods.length === 0
                    }
                >
                    Generate periods
                </Button>
            </DialogActions>
        </Dialog>
    );
}
