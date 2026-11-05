
import {
    Box,
    Button,
    IconButton,
    TextField,
    Typography,
} from '@mui/material';

import {
    AddOutlined, Delete,
} from '@mui/icons-material';

import {
    Controller,
    useFieldArray,
    useFormContext,
} from 'react-hook-form';

import type { AcademicWizardValues } from '../../wizard/types/wizard';

import styles from './SchedulingEditor.module.css';

export default function ClassroomEditor() {
    const {
        control,
        formState: { errors },
    } = useFormContext<AcademicWizardValues>();

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'classrooms',
    });

    return (
        <Box className={styles.section}>
            <Box className={styles.sectionHeader}>
                <Box>
                    <Typography variant="h6" component="h3">
                        Classrooms
                    </Typography>

                    <Typography variant="body2" color="text.secondary">
                        Define the classrooms available for academic scheduling.
                        Capacity is optional.
                    </Typography>
                </Box>

                <Button
                    variant="outlined"
                    startIcon={<AddOutlined />}
                    onClick={() =>
                        append({
                            id: '',
                            name: '',
                            capacity: null,
                        })
                    }
                >
                    Add classroom
                </Button>
            </Box>

            {fields.length === 0 && (
                <Box className={styles.emptyState}>
                    <Typography color="text.secondary">
                        No classrooms added yet.
                    </Typography>

                    <Typography variant="body2" color="text.secondary">
                        Select Add classroom to create your first classroom.
                    </Typography>
                </Box>
            )}

            {fields.map((item, index) => (
                <Box key={item.id} className={styles.classroomRow}>
                    <Controller
                        control={control}
                        name={`classrooms.${index}.id`}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                label="Classroom ID"
                                placeholder="e.g. ROOM-101"
                                size="small"
                                required
                                fullWidth
                                error={Boolean(errors.classrooms?.[index]?.id)}
                                helperText={
                                    errors.classrooms?.[index]?.id?.message
                                }
                            />
                        )}
                    />

                    <Controller
                        control={control}
                        name={`classrooms.${index}.name`}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                label="Classroom name"
                                placeholder="e.g. Room 101"
                                size="small"
                                required
                                fullWidth
                                error={Boolean(errors.classrooms?.[index]?.name)}
                                helperText={
                                    errors.classrooms?.[index]?.name?.message
                                }
                            />
                        )}
                    />

                    <Controller
                        control={control}
                        name={`classrooms.${index}.capacity`}
                        render={({ field }) => (
                            <TextField
                                label="Capacity"
                                type="number"
                                size="small"
                                fullWidth
                                value={field.value ?? ''}
                                onBlur={field.onBlur}
                                inputRef={field.ref}
                                onChange={(event) => {
                                    const value = event.target.value;

                                    field.onChange(
                                        value === '' ? null : Number(value),
                                    );
                                }}
                                slotProps={{
                                    htmlInput: {
                                        min: 0,
                                        step: 1,
                                    },
                                }}
                                error={Boolean(
                                    errors.classrooms?.[index]?.capacity,
                                )}
                                helperText={
                                    errors.classrooms?.[index]?.capacity?.message ??
                                    'Optional'
                                }
                            />
                        )}
                    />

                    <IconButton
                        color="error"
                        aria-label={`Remove classroom ${index + 1}`}
                        onClick={() => remove(index)}
                    >
                        <Delete />
                    </IconButton>
                </Box>
            ))}
        </Box>
    );
}
