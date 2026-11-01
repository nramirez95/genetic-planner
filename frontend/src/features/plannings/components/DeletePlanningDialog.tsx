
import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Stack,
} from '@mui/material';

import type { PlanningSummary } from '../types/planning';

interface DeletePlanningDialogProps {
    planning: PlanningSummary | null;
    isDeleting: boolean;
    error: string | null;
    onClose: () => void;
    onConfirm: () => void;
}

export function DeletePlanningDialog({
                                         planning,
                                         isDeleting,
                                         error,
                                         onClose,
                                         onConfirm,
                                     }: DeletePlanningDialogProps) {
    return (
        <Dialog
            open={Boolean(planning)}
            onClose={isDeleting ? undefined : onClose}
            fullWidth
            maxWidth="xs"
            aria-labelledby="delete-planning-title"
            aria-describedby="delete-planning-description"
        >
            <DialogTitle id="delete-planning-title">
                Delete planning?
            </DialogTitle>

            <DialogContent>
                <Stack spacing={2}>
                    <DialogContentText id="delete-planning-description">
                        Are you sure you want to delete{' '}
                        <strong>{planning?.name}</strong>?
                        This action cannot be undone.
                    </DialogContentText>

                    {error && (
                        <Alert severity="error" role="alert">
                            {error}
                        </Alert>
                    )}
                </Stack>
            </DialogContent>

            <DialogActions>
                <Button
                    onClick={onClose}
                    disabled={isDeleting}
                    color="inherit"
                >
                    Cancel
                </Button>

                <Button
                    onClick={onConfirm}
                    disabled={isDeleting}
                    color="error"
                    variant="contained"
                    startIcon={
                        isDeleting ? (
                            <CircularProgress size={16} color="inherit" />
                        ) : undefined
                    }
                >
                    {isDeleting ? 'Deleting...' : 'Delete'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}
