import React from 'react';
import ReactDOMClient from 'react-dom/client';
import singleSpaReact from 'single-spa-react';
import { navigateToUrl } from 'single-spa';
import {
    Box,
    CardActionArea,
    CardContent,
    Paper,
    Typography,
} from '@mui/material';
import { styles } from './cardStyle';

interface CardProps {
    onClick?: () => void;
    title?: string;
}

const FinanzasIcon: React.FC = () => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        style={{
            width: '54px',
            height: '54px',
        }}
    >
        <rect x="2" y="4" width="20" height="16" rx="2" />
        <path d="M12 8v8" />
        <path d="M8 12h8" />
        <circle cx="12" cy="12" r="3" />
        <path d="M6 8h.01" />
        <path d="M6 16h.01" />
        <path d="M18 8h.01" />
        <path d="M18 16h.01" />
    </svg>
);

const Card: React.FC<CardProps> = ({
    onClick,
    title = 'Finanzas',
}) => {
    const handleNavigate = (event: React.MouseEvent<HTMLElement>) => {
        event.preventDefault();

        if (onClick) {
            onClick();
            return;
        }

        navigateToUrl('/finanzas');
    };

    return (
        <Paper
            data-testid="paper-card"
            elevation={0}
            square
            sx={styles.paper}
            variant="outlined"
        >
            <CardActionArea
                data-testid="card-action-area"
                href="/finanzas"
                onClick={handleNavigate}
                sx={styles.cardActionArea}
            >
                <Box sx={styles.iconWrapper}>
                    <Box sx={styles.iconCircle}>
                        <FinanzasIcon />
                    </Box>
                </Box>

                <CardContent sx={styles.cardContent}>
                    <Typography
                        component="h2"
                        data-testid="card-title"
                        sx={styles.cardText}
                    >
                        {title}
                    </Typography>
                </CardContent>
            </CardActionArea>
        </Paper>
    );
};

const lifecycles = singleSpaReact({
    React,
    ReactDOMClient,
    rootComponent: Card,
    errorBoundary() {
        return <Box>Error al cargar el módulo de finanzas</Box>;
    },
});

export const { bootstrap, mount, unmount } = lifecycles;
export default Card;