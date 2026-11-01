
export const designTokens = {
    colors: {
        brand: {
            main: '#FE5000',
            peach: '#FFB07C',
        },

        primary: {
            main: '#B83B00',
            hover: '#A63500',
            contrastText: '#FFFFFF',
        },

        teal: '#147D7E',

        background: {
            default: '#FAF8F5',
            paper: '#FFFFFF',
            subtle: '#F5F1EC',
        },

        text: {
            primary: '#292524',
            secondary: '#57534E',
        },

        divider: '#E7E1DA',

        success: '#2E7D5B',
        warning: '#875900',
        error: '#D32F2F',
        info: '#147D7E',
    },

    typography: {
        display: '"Space Grotesk", Arial, sans-serif',
        body: 'Inter, Arial, sans-serif',
    },

    radius: {
        control: 10,
        card: 12,
    },
} as const;
