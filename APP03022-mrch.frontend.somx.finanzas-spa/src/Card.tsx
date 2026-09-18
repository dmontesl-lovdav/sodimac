import React, { useEffect, useState } from 'react';
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
import { globalHomeStore } from './store/globalStore';

interface CardProps {
    onClick?: () => void;
    title?: string;
}

interface Country {
    name?: string;
}

interface SelectedTenant {
    name?: string;
    country?: Country;
}

interface ConfigurationState {
    selectedTenant?: SelectedTenant;
}

interface GlobalState {
    configuration?: ConfigurationState;
}

/**
 * Obtiene el país seleccionado desde el Common State.
 *
 * Estructura real:
 * state.configuration.selectedTenant.country.name
 */
const getSelectedCountry = (
    state: unknown
): string | undefined => {
    if (!state || typeof state !== 'object') {
        console.warn(
            '[Finanzas Card] Global state vacío o inválido:',
            state
        );

        return undefined;
    }

    const globalState = state as GlobalState;

    const selectedTenant =
        globalState.configuration?.selectedTenant;

    const country =
        selectedTenant?.country?.name;

    console.log(
        '[Finanzas Card] Tenant seleccionado:',
        selectedTenant
    );

    console.log(
        '[Finanzas Card] País seleccionado:',
        country
    );

    return country;
};

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
        <rect
            x="2"
            y="4"
            width="20"
            height="16"
            rx="2"
        />

        <path d="M12 8v8" />
        <path d="M8 12h8" />

        <circle
            cx="12"
            cy="12"
            r="3"
        />

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
    const [countryName, setCountryName] = useState<
        string | undefined
    >(undefined);

    useEffect(() => {
        const updateCountry = (
            globalState: unknown,
            origin: string
        ) => {
            console.log(
                `[Finanzas Card] Global state (${origin}):`,
                globalState
            );

            const country =
                getSelectedCountry(globalState);

            console.log(
                `[Finanzas Card] Country detectado (${origin}):`,
                country
            );

            setCountryName(country);
        };

        /**
         * Estado inicial.
         */
        try {
            const initialGlobalState =
                globalHomeStore.GetGlobalState();

            updateCountry(
                initialGlobalState,
                'initial'
            );
        } catch (error) {
            console.error(
                '[Finanzas Card] Error leyendo estado inicial:',
                error
            );
        }

        /**
         * Cambios posteriores de tenant.
         */
        const unsubscribe =
            globalHomeStore.SubscribeToGlobalState(
                'finanzas',
                (globalState: unknown) => {
                    updateCountry(
                        globalState,
                        'subscription'
                    );
                }
            );

        return () => {
            if (typeof unsubscribe === 'function') {
                unsubscribe();
            }
        };
    }, []);

    const normalizedCountry =
        countryName?.trim().toUpperCase();

    console.log(
        '[Finanzas Card] Render:',
        {
            countryName,
            normalizedCountry,
            shouldRender:
                normalizedCountry === 'MX',
        }
    );

    /**
     * Finanzas solamente se renderiza para México.
     */
    if (normalizedCountry !== 'MX') {
        return null;
    }

    const handleNavigate = (
        event: React.MouseEvent<HTMLElement>
    ) => {
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
                        component="p"
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

    errorBoundary(error) {
        console.error(
            '[Finanzas Card] ErrorBoundary:',
            error
        );

        return (
            <Box>
                Error al cargar el módulo de finanzas
            </Box>
        );
    },
});

export const {
    bootstrap,
    mount,
    unmount,
} = lifecycles;

export default Card;