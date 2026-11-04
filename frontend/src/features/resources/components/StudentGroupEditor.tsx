
import { Box, Button, IconButton, TextField, Typography } from '@mui/material';
import {AddOutlined, Delete} from '@mui/icons-material';
import { Controller, useFieldArray, useFormContext } from 'react-hook-form';

import type { AcademicWizardValues } from '../../wizard/types/wizard';
import styles from './ResourceEditor.module.css';

export default function StudentGroupEditor() {
    const {
        control,
        formState: { errors },
    } = useFormContext<AcademicWizardValues>();

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'studentGroups',
    });

    return (
        <Box className={styles.section}>
            <Box className={styles.sectionHeader}>
                <Box>
                    <Typography variant="h6">Student Groups</Typography>
                    <Typography variant="body2" color="text.secondary">
                        Define the student groups that will attend lessons.
                    </Typography>
                </Box>

                <Button
                    startIcon={<AddOutlined />}
                    variant="outlined"
                    onClick={() =>
                        append({ id: '', name: '', size: null })
                    }
                >
                    Add group
                </Button>
            </Box>

            {fields.length === 0 && (
                <Typography color="text.secondary">
                    No student groups added yet.
                </Typography>
            )}

            {fields.map((field, index) => (
                <Box key={field.id} className={styles.groupRow}>
                    <Controller
                        control={control}
                        name={`studentGroups.${index}.id`}
                        render={({ field: input }) => (
                            <TextField
                                {...input}
                                label="Group ID"
                                size="small"
                                fullWidth
                                error={Boolean(errors.studentGroups?.[index]?.id)}
                                helperText={errors.studentGroups?.[index]?.id?.message}
                            />
                        )}
                    />

                    <Controller
                        control={control}
                        name={`studentGroups.${index}.name`}
                        render={({ field: input }) => (
                            <TextField
                                {...input}
                                label="Group name"
                                size="small"
                                fullWidth
                                error={Boolean(errors.studentGroups?.[index]?.name)}
                                helperText={errors.studentGroups?.[index]?.name?.message}
                            />
                        )}
                    />

                    <Controller
                        control={control}
                        name={`studentGroups.${index}.size`}
                        render={({ field: input }) => (
                            <TextField
                                label="Students"
                                type="number"
                                size="small"
                                fullWidth
                                value={input.value ?? ''}
                                onBlur={input.onBlur}
                                inputRef={input.ref}
                                onChange={(event) => {
                                    const value = event.target.value;
                                    input.onChange(
                                        value === '' ? null : Number(value),
                                    );
                                }}
                                slotProps={{
                                    htmlInput: { min: 0, step: 1 },
                                }}
                                error={Boolean(errors.studentGroups?.[index]?.size)}
                                helperText={
                                    errors.studentGroups?.[index]?.size?.message
                                }
                            />
                        )}
                    />

                    <IconButton
                        aria-label={`Remove student group ${index + 1}`}
                        color="error"
                        onClick={() => remove(index)}
                    >
                        <Delete />
                    </IconButton>
                </Box>
            ))}

            {errors.studentGroups?.message && (
                <Typography color="error" variant="body2">
                    {errors.studentGroups.message}
                </Typography>
            )}
        </Box>
    );
}
