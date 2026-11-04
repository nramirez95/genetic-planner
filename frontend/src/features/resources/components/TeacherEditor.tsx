import {Box, Button, IconButton, TextField, Typography} from '@mui/material';
import {AddOutlined, Delete} from '@mui/icons-material';
import {Controller, useFieldArray, useFormContext} from 'react-hook-form';

import type {AcademicWizardValues} from '../../wizard/types/wizard';
import styles from './ResourceEditor.module.css';

export default function TeacherEditor() {
    const {
        control,
        formState: { errors },
    } = useFormContext<AcademicWizardValues>();

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'teachers',
    });

    return (
        <Box className={styles.section}>
            <Box className={styles.sectionHeader}>
                <Box>
                    <Typography variant="h6">Teachers</Typography>
                    <Typography variant="body2" color="text.secondary">
                        Add the teachers available for academic scheduling.
                    </Typography>
                </Box>

                <Button
                    startIcon={<AddOutlined />}
                    variant="outlined"
                    onClick={() => append({ id: '', name: '' })}
                >
                    Add teacher
                </Button>
            </Box>

            {fields.length === 0 && (
                <Typography color="text.secondary">
                    No teachers added yet.
                </Typography>
            )}

            {fields.map((field, index) => (
                <Box key={field.id} className={styles.row}>
                    <Controller
                        control={control}
                        name={`teachers.${index}.id`}
                        render={({ field: input }) => (
                            <TextField
                                {...input}
                                label="Teacher ID"
                                size="small"
                                fullWidth
                                error={Boolean(errors.teachers?.[index]?.id)}
                                helperText={errors.teachers?.[index]?.id?.message}
                            />
                        )}
                    />

                    <Controller
                        control={control}
                        name={`teachers.${index}.name`}
                        render={({ field: input }) => (
                            <TextField
                                {...input}
                                label="Teacher name"
                                size="small"
                                fullWidth
                                error={Boolean(errors.teachers?.[index]?.name)}
                                helperText={errors.teachers?.[index]?.name?.message}
                            />
                        )}
                    />

                    <IconButton
                        aria-label={`Remove teacher ${index + 1}`}
                        color="error"
                        onClick={() => remove(index)}
                    >
                        <Delete />
                    </IconButton>
                </Box>
            ))}

            {errors.teachers?.message && (
                <Typography color="error" variant="body2">
                    {errors.teachers.message}
                </Typography>
            )}
        </Box>
    );
}
