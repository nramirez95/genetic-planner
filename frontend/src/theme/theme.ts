import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
    cssVariables: true,
    palette: {
        mode: 'light',
        primary: {
            main: '#2563EB',
        },
        secondary: {
            main: '#64748B',
        },
        background: {
            default: '#F8FAFC',
        },
    },
    typography: {
        fontFamily: 'Roboto, Arial, sans-serif',
    },
});