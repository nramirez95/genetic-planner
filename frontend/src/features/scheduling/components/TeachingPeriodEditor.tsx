
import { useMemo, useState } from 'react';

import {
    Alert,
    Box,
    Button,
    Dialog, DialogActions, DialogContent, DialogTitle,
    IconButton,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    AddOutlined,
    CalendarMonthOutlined, Delete,
    EditOutlined,
    SearchOutlined,
} from '@mui/icons-material';

import {
    useFieldArray,
    useFormContext,
    useWatch,
} from 'react-hook-form';

import type { AcademicWizardValues } from '../../wizard/types/wizard';

import TeachingPeriodDialog from './TeachingPeriodDialog';
import RecurringPeriodDialog from './RecurringPeriodDialog';

import styles from './SchedulingEditor.module.css';

type TeachingPeriod =
    AcademicWizardValues['teachingPeriods'][number];

const EMPTY_PERIODS: TeachingPeriod[] = [];
const ROWS_PER_PAGE = 10;

function formatDateTime(value: string): string {
    if (!value) return '—';

    return value.replace('T', ' ');
}

interface TeachingPeriodEditorProps {
    invalidPeriodIndex: number | null;
}

export default function TeachingPeriodEditor({
               invalidPeriodIndex,
           }: TeachingPeriodEditorProps) {
    const {
        control,
        formState: { errors },
    } = useFormContext<AcademicWizardValues>();

    const { fields, append, update, remove } = useFieldArray({
        control,
        name: 'teachingPeriods',
    });

    const periods =
        useWatch({ control, name: 'teachingPeriods' }) ?? EMPTY_PERIODS;

    const horizonStart =
        useWatch({ control, name: 'horizonStart' }) ?? '';

    const horizonEnd =
        useWatch({ control, name: 'horizonEnd' }) ?? '';

    const [search, setSearch] = useState('');

    const [editingIndex, setEditingIndex] =
        useState<number | null>(null);

    const [periodDialogOpen, setPeriodDialogOpen] =
        useState(false);

    const [recurringOpen, setRecurringOpen] =
        useState(false);

    const [deleteIndex, setDeleteIndex] =
        useState<number | null>(null);

    const filteredPeriods = useMemo(() => {
        const query = search.trim().toLowerCase();

        return periods
            .map((period, index) => ({
                period,
                index,
            }))
            .filter(({ period }) => {
                if (!query) return true;

                return [
                    period.id,
                    period.label ?? '',
                    period.start,
                    period.end,
                ].some((value) =>
                    value.toLowerCase().includes(query),
                );
            })
            .sort((a, b) =>
                a.period.start.localeCompare(b.period.start),
            );
    }, [periods, search]);

    const initialPage = (() => {
        if (invalidPeriodIndex === null) return 0;

        const position = filteredPeriods.findIndex(
            ({ index }) => index === invalidPeriodIndex,
        );

        return position >= 0
            ? Math.floor(position / ROWS_PER_PAGE)
            : 0;
    })();

    const [page, setPage] = useState(initialPage);

    const pageCount = Math.max(
        1,
        Math.ceil(filteredPeriods.length / ROWS_PER_PAGE),
    );

    const safePage = Math.min(page, pageCount - 1);

    const visiblePeriods = filteredPeriods.slice(
        safePage * ROWS_PER_PAGE,
        (safePage + 1) * ROWS_PER_PAGE,
    );

    function openCreateDialog() {
        setEditingIndex(null);
        setPeriodDialogOpen(true);
    }

    function openEditDialog(index: number) {
        setEditingIndex(index);
        setPeriodDialogOpen(true);
    }

    function handleSave(period: TeachingPeriod) {
        if (editingIndex === null) {
            append(period);
        } else {
            update(editingIndex, period);
        }

        setPeriodDialogOpen(false);
        setEditingIndex(null);
    }

    function handleDelete() {
        if (deleteIndex === null) return;

        remove(deleteIndex);
        setDeleteIndex(null);
    }

    const editingPeriod =
        editingIndex === null ? null : periods[editingIndex];

    return (
        <Box className={styles.section}>
            <Box className={styles.sectionHeader}>
                <Box>
                    <Typography variant="h6" component="h3">
                        Teaching periods
                    </Typography>

                    <Typography variant="body2" color="text.secondary">
                        {periods.length} periods configured
                    </Typography>
                </Box>

                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button
                        variant="outlined"
                        startIcon={<AddOutlined />}
                        onClick={openCreateDialog}
                    >
                        Add period
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<CalendarMonthOutlined />}
                        onClick={() => setRecurringOpen(true)}
                    >
                        Generate recurring
                    </Button>
                </Box>
            </Box>

            <TextField
                size="small"
                fullWidth
                placeholder="Search by ID, label or date..."
                value={search}
                onChange={(event) => {
                    setSearch(event.target.value);
                    setPage(0);
                }}
                slotProps={{
                    input: {
                        startAdornment: <SearchOutlined sx={{ mr: 1 }} />,
                    },
                }}
            />

            {invalidPeriodIndex !== null && (
                <Alert severity="error">
                    Some teaching periods contain invalid data.
                    Review the highlighted row and select Edit to correct it.
                </Alert>
            )}

            <TableContainer className={styles.periodTable}>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Label</TableCell>
                            <TableCell>Start</TableCell>
                            <TableCell>End</TableCell>
                            <TableCell align="right">Actions</TableCell>
                        </TableRow>
                    </TableHead>

                    <TableBody>
                        {visiblePeriods.map(({ period, index }) => {
                            const periodErrors =
                                errors.teachingPeriods?.[index];

                            const hasError = Boolean(
                                periodErrors?.id ||
                                periodErrors?.start ||
                                periodErrors?.end ||
                                periodErrors?.label,
                            );

                            return (
                                <TableRow
                                    key={fields[index]?.id ?? index}
                                    hover
                                    sx={
                                        hasError || index === invalidPeriodIndex
                                            ? {
                                                backgroundColor: 'error.light',
                                                '& td': {
                                                    color: 'error.contrastText',
                                                },
                                            }
                                            : undefined
                                    }
                                >
                                    <TableCell>
                                        <Typography variant="body2">
                                            {period.id}
                                        </Typography>
                                    </TableCell>

                                    <TableCell>
                                        {period.label || '—'}
                                    </TableCell>

                                    <TableCell>
                                        {formatDateTime(period.start)}
                                    </TableCell>

                                    <TableCell>
                                        {formatDateTime(period.end)}
                                    </TableCell>

                                    <TableCell align="right">
                                        <Tooltip title="Edit period">
                                            <IconButton
                                                size="small"
                                                onClick={() => openEditDialog(index)}
                                            >
                                                <EditOutlined fontSize="small" />
                                            </IconButton>
                                        </Tooltip>

                                        <Tooltip title="Delete period">
                                            <IconButton
                                                size="small"
                                                color="error"
                                                onClick={() => setDeleteIndex(index)}
                                            >
                                                <Delete fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            );
                        })}

                        {visiblePeriods.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={5}>
                                    <Typography
                                        variant="body2"
                                        color="text.secondary"
                                        sx={{ py: 3, textAlign: 'center' }}
                                    >
                                        {periods.length === 0
                                            ? 'No teaching periods added yet.'
                                            : 'No periods match your search.'}
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            <TablePagination
                component="div"
                count={filteredPeriods.length}
                page={safePage}
                onPageChange={(_, nextPage) => setPage(nextPage)}
                rowsPerPage={ROWS_PER_PAGE}
                rowsPerPageOptions={[ROWS_PER_PAGE]}
            />

            {periodDialogOpen && (
                <TeachingPeriodDialog
                    key={editingIndex === null ? 'new' : `edit-${editingIndex}`}
                    open={periodDialogOpen}
                    period={editingPeriod ?? null}
                    existingPeriods={periods.filter(
                        (_, index) => index !== editingIndex,
                    )}
                    horizonStart={horizonStart}
                    horizonEnd={horizonEnd}
                    onClose={() => {
                        setPeriodDialogOpen(false);
                        setEditingIndex(null);
                    }}
                    onSave={handleSave}
                />
            )}

            <RecurringPeriodDialog
                open={recurringOpen}
                onClose={() => setRecurringOpen(false)}
                existingPeriods={periods}
                horizonStart={horizonStart}
                horizonEnd={horizonEnd}
                onGenerate={(newPeriods) => {
                    append(newPeriods);
                    setSearch('');
                    setPage(0);
                }}
            />

            <Dialog
                open={deleteIndex !== null}
                onClose={() => setDeleteIndex(null)}
            >
                <DialogTitle>Delete teaching period?</DialogTitle>
                <DialogContent>
                    <Typography>
                        This period will be removed from the planning.
                        Any lesson referencing it will need to be updated.
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteIndex(null)}>
                        Cancel
                    </Button>
                    <Button
                        color="error"
                        variant="contained"
                        onClick={handleDelete}
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}
