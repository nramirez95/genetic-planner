
import {
    Box,
    Button,
    Card,
    CardContent,
    Checkbox,
    Chip,
    FormControl,
    FormHelperText,
    IconButton,
    InputLabel,
    ListItemText,
    MenuItem,
    OutlinedInput,
    Select,
    TextField,
    Typography,
} from '@mui/material';
import {AddOutlined, Delete} from '@mui/icons-material';
import {
    Controller,
    useFieldArray,
    useFormContext,
    useWatch,
} from 'react-hook-form';

import type { AcademicWizardValues } from '../../wizard/types/wizard';
import styles from './ActivityEditor.module.css';

type Lesson = AcademicWizardValues['lessons'][number];
type SelectField =
    | 'teacherIds'
    | 'studentGroupIds'
    | 'allowedTeachingPeriodIds'
    | 'allowedClassroomIds';

interface ReferenceOption {
    id: string;
    name: string;
}

interface ReferenceSelectProps {
    name: `lessons.${number}.${SelectField}`;
    label: string;
    options: ReferenceOption[];
    error?: string;
    optional?: boolean;
}

function ReferenceSelect({
                             name,
                             label,
                             options,
                             error,
                             optional = false,
                         }: ReferenceSelectProps) {
    const { control } = useFormContext<AcademicWizardValues>();

    return (
        <Controller
            control={control}
            name={name}
            render={({ field }) => {
                const selected = field.value ?? [];

                return (
                    <FormControl fullWidth size="small" error={Boolean(error)}>
                        <InputLabel>{label}</InputLabel>

                        <Select<string[]>
                            multiple
                            label={label}
                            value={selected}
                            onChange={(event) => {
                                const value = event.target.value;
                                const ids =
                                    typeof value === 'string'
                                        ? value.split(',')
                                        : value;

                                field.onChange(
                                    optional && ids.length === 0 ? null : ids,
                                );
                            }}
                            input={<OutlinedInput label={label} />}

                            renderValue={(ids) => (
                                <Box className={styles.chips}>
                                    {ids.map((id) => {
                                        const option = options.find(
                                            (item) => item.id === id,
                                        );

                                        const isInvalid = !option;

                                        return (
                                            <Chip
                                                key={id}
                                                size="small"
                                                label={option?.name ?? `Unknown: ${id}`}
                                                color={isInvalid ? 'error' : 'default'}
                                                variant={isInvalid ? 'outlined' : 'filled'}
                                                onMouseDown={(event) => {
                                                    event.stopPropagation();
                                                }}
                                                onDelete={(event) => {
                                                    event.stopPropagation();

                                                    const updated = ids.filter(
                                                        (selectedId) => selectedId !== id,
                                                    );

                                                    field.onChange(
                                                        optional && updated.length === 0
                                                            ? null
                                                            : updated,
                                                    );
                                                }}
                                            />
                                        );
                                    })}
                                </Box>
                            )}
                        >
                            {options.map((option) => (
                                <MenuItem key={option.id} value={option.id}>
                                    <Checkbox checked={selected.includes(option.id)} />
                                    <ListItemText
                                        primary={option.name}
                                        secondary={option.id}
                                    />
                                </MenuItem>
                            ))}
                        </Select>

                        {error && <FormHelperText>{error}</FormHelperText>}
                    </FormControl>
                );
            }}
        />
    );
}

export default function ActivityEditor() {
    const {
        control,
        formState: { errors },
    } = useFormContext<AcademicWizardValues>();

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'lessons',
    });

    const teachers = useWatch({ control, name: 'teachers' }) ?? [];
    const studentGroups =
        useWatch({ control, name: 'studentGroups' }) ?? [];
    const teachingPeriods =
        useWatch({ control, name: 'teachingPeriods' }) ?? [];
    const classrooms = useWatch({ control, name: 'classrooms' }) ?? [];

    const teacherOptions = teachers.map((item) => ({
        id: item.id,
        name: item.name || item.id,
    }));

    const groupOptions = studentGroups.map((item) => ({
        id: item.id,
        name: item.name || item.id,
    }));

    const periodOptions = teachingPeriods.map((item) => ({
        id: item.id,
        name: item.label || item.id,
    }));

    const classroomOptions = classrooms.map((item) => ({
        id: item.id,
        name: item.name || item.id,
    }));

    const createLesson = (): Lesson => ({
        id: crypto.randomUUID(),
        name: '',
        teacherIds: [],
        studentGroupIds: [],
        allowedTeachingPeriodIds: null,
        allowedClassroomIds: null,
        requiredCapacity: null,
    });

    return (
        <Box className={styles.editor}>
            <Box className={styles.header}>
                <Box>
                    <Typography variant="h6">Lessons</Typography>
                    <Typography variant="body2" color="text.secondary">
                        Define lessons and associate their academic resources.
                    </Typography>
                </Box>

                <Button
                    variant="outlined"
                    startIcon={<AddOutlined />}
                    onClick={() => append(createLesson())}
                >
                    Add lesson
                </Button>
            </Box>

            {fields.length === 0 && (
                <Typography color="text.secondary">
                    No lessons added yet. Select Add lesson to create one.
                </Typography>
            )}

            {fields.map((item, index) => (
                <Card key={item.id} variant="outlined">
                    <CardContent className={styles.lesson}>
                        <Box className={styles.lessonHeader}>
                            <Typography variant="subtitle1">
                                Lesson {index + 1}
                            </Typography>

                            <IconButton
                                color="error"
                                aria-label={`Remove lesson ${index + 1}`}
                                onClick={() => remove(index)}
                            >
                                <Delete />
                            </IconButton>
                        </Box>

                        <Box className={styles.grid}>
                            <Controller
                                control={control}
                                name={`lessons.${index}.id`}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Lesson ID"
                                        size="small"
                                        fullWidth
                                        error={Boolean(errors.lessons?.[index]?.id)}
                                        helperText={errors.lessons?.[index]?.id?.message}
                                    />
                                )}
                            />

                            <Controller
                                control={control}
                                name={`lessons.${index}.name`}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Lesson name"
                                        size="small"
                                        fullWidth
                                        error={Boolean(errors.lessons?.[index]?.name)}
                                        helperText={errors.lessons?.[index]?.name?.message}
                                    />
                                )}
                            />

                            <ReferenceSelect
                                name={`lessons.${index}.teacherIds`}
                                label="Required teachers"
                                options={teacherOptions}
                                error={errors.lessons?.[index]?.teacherIds?.message}
                            />

                            <ReferenceSelect
                                name={`lessons.${index}.studentGroupIds`}
                                label="Student groups"
                                options={groupOptions}
                                error={errors.lessons?.[index]?.studentGroupIds?.message}
                            />

                            <ReferenceSelect
                                name={`lessons.${index}.allowedTeachingPeriodIds`}
                                label="Allowed periods"
                                options={periodOptions}
                                optional
                                error={
                                    errors.lessons?.[index]?.allowedTeachingPeriodIds?.message
                                }
                            />

                            <ReferenceSelect
                                name={`lessons.${index}.allowedClassroomIds`}
                                label="Allowed classrooms"
                                options={classroomOptions}
                                optional
                                error={
                                    errors.lessons?.[index]?.allowedClassroomIds?.message
                                }
                            />

                            <Controller
                                control={control}
                                name={`lessons.${index}.requiredCapacity`}
                                render={({ field }) => (
                                    <TextField
                                        label="Required capacity"
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
                                            htmlInput: { min: 0, step: 1 },
                                        }}
                                        error={Boolean(
                                            errors.lessons?.[index]?.requiredCapacity,
                                        )}
                                        helperText={
                                            errors.lessons?.[index]?.requiredCapacity?.message
                                        }
                                    />
                                )}
                            />
                        </Box>

                        {(periodOptions.length === 0 ||
                            classroomOptions.length === 0) && (
                            <Typography variant="caption" color="text.secondary">
                                Teaching periods and classrooms can be configured
                                in the next step. Leaving these selections empty
                                means no specific restriction.
                            </Typography>
                        )}
                    </CardContent>
                </Card>
            ))}
        </Box>
    );
}
