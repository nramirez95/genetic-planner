import { Button, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router';

export default function NotFoundPage() {
    return (
        <Stack
            spacing={2}
            sx={{ alignItems: 'flex-start' }}
        >
            <Typography variant="h4">
                Page not found
            </Typography>

            <Typography color="text.secondary">
                The requested page does not exist.
            </Typography>

            <Button
                component={RouterLink}
                to="/"
                variant="contained"
            >
                Back to Dashboard
            </Button>
        </Stack>
    );
}