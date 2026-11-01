
import { createTheme, type Shadows } from '@mui/material/styles';
import { designTokens } from './tokens';

const { colors, typography: fonts, radius } = designTokens;

const cardShadow =
    '0 1px 3px rgba(41, 37, 36, 0.04), 0 2px 8px rgba(41, 37, 36, 0.03)';

const shadows = [
    'none',
    cardShadow,
    '0 2px 8px rgba(41, 37, 36, 0.06)',
    '0 4px 12px rgba(41, 37, 36, 0.08)',
    ...Array(21).fill('0 8px 24px rgba(41, 37, 36, 0.10)'),
] as Shadows;

export const theme = createTheme({
    cssVariables: true,

    palette: {
        mode: 'light',

        primary: {
            main: colors.primary.main,
            contrastText: colors.primary.contrastText,
        },

        secondary: {
            main: colors.teal,
            contrastText: '#FFFFFF',
        },

        success: {
            main: colors.success,
            contrastText: '#FFFFFF',
        },

        warning: {
            main: colors.warning,
            contrastText: '#FFFFFF',
        },

        error: {
            main: colors.error,
            contrastText: '#FFFFFF',
        },

        info: {
            main: colors.info,
            contrastText: '#FFFFFF',
        },

        background: {
            default: colors.background.default,
            paper: colors.background.paper,
        },

        text: {
            primary: colors.text.primary,
            secondary: colors.text.secondary,
        },

        divider: colors.divider,
    },

    typography: {
        fontFamily: fonts.body,

        h1: {
            fontFamily: fonts.display,
            fontSize: '2.5rem',
            fontWeight: 700,
            lineHeight: 1.2,
        },

        h2: {
            fontFamily: fonts.display,
            fontSize: '2rem',
            fontWeight: 700,
            lineHeight: 1.25,
        },

        h3: {
            fontFamily: fonts.display,
            fontSize: '1.75rem',
            fontWeight: 700,
            lineHeight: 1.3,
        },

        h4: {
            fontFamily: fonts.display,
            fontSize: '1.5rem',
            fontWeight: 700,
            lineHeight: 1.35,
        },

        h5: {
            fontFamily: fonts.body,
            fontSize: '1.25rem',
            fontWeight: 600,
        },

        h6: {
            fontFamily: fonts.body,
            fontSize: '1.125rem',
            fontWeight: 600,
        },

        body1: {
            fontFamily: fonts.body,
            fontSize: '1rem',
            fontWeight: 400,
            lineHeight: 1.5,
        },

        body2: {
            fontFamily: fonts.body,
            fontSize: '0.875rem',
            fontWeight: 400,
            lineHeight: 1.5,
        },

        subtitle1: {
            fontFamily: fonts.body,
            fontWeight: 600,
        },

        subtitle2: {
            fontFamily: fonts.body,
            fontWeight: 600,
        },

        button: {
            fontFamily: fonts.body,
            fontSize: '0.875rem',
            fontWeight: 600,
            textTransform: 'none',
        },

        caption: {
            fontFamily: fonts.body,
            fontSize: '0.75rem',
            fontWeight: 400,
        },
    },

    shape: {
        borderRadius: radius.card,
    },

    shadows,

    components: {
        MuiCssBaseline: {
            styleOverrides: {
                ':root': {
                    '--gp-brand-main': colors.brand.main,
                    '--gp-brand-peach': colors.brand.peach,
                    '--gp-background-subtle': colors.background.subtle,
                    '--gp-primary-hover': colors.primary.hover,
                    '--gp-gradient-hero': `linear-gradient(
            110deg,
            ${colors.brand.main} 0%,
            ${colors.brand.peach} 100%
          )`,
                },

                body: {
                    backgroundColor: colors.background.default,
                },

                '::selection': {
                    backgroundColor: '#FFE0CC',
                    color: colors.text.primary,
                },
            },
        },

        MuiButton: {
            defaultProps: {
                disableElevation: true,
            },

            styleOverrides: {
                root: {
                    borderRadius: radius.control,
                    padding: '9px 18px',
                    fontFamily: fonts.body,
                    fontWeight: 600,
                    textTransform: 'none',

                    '&.MuiButton-containedPrimary:hover': {
                        backgroundColor: colors.primary.hover,
                    },
                },

                outlined: {
                    borderWidth: 1,
                },
            },
        },

        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: radius.card,
                    border: `1px solid ${colors.divider}`,
                    boxShadow: cardShadow,
                    backgroundColor: colors.background.paper,
                },
            },
        },

        MuiPaper: {
            styleOverrides: {
                rounded: {
                    borderRadius: radius.card,
                },
            },
        },

        MuiTextField: {
            defaultProps: {
                variant: 'outlined',
            },
        },

        MuiOutlinedInput: {
            styleOverrides: {
                root: {
                    borderRadius: radius.control,
                },
            },
        },

        MuiChip: {
            styleOverrides: {
                root: {
                    fontFamily: fonts.body,
                    fontWeight: 600,
                },
            },
        },

        MuiTab: {
            styleOverrides: {
                root: {
                    fontFamily: fonts.body,
                    fontWeight: 600,
                    textTransform: 'none',
                },
            },
        },

        MuiTableCell: {
            styleOverrides: {
                head: {
                    fontFamily: fonts.body,
                    fontWeight: 700,
                },

                body: {
                    fontFamily: fonts.body,
                    fontWeight: 400,
                },
            },
        },
    },
});

export default theme;
