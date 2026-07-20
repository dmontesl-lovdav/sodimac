import React from 'react';
import ReactDOMClient from 'react-dom/client';
import singleSpaReact from 'single-spa-react';
import { navigateToUrl } from 'single-spa';

interface CardProps {
    onClick?: () => void;
    title?: string;
}

const UtilIcon: React.FC = () => (
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
        <path d="M12 15.5A3.5 3.5 0 1 0 12 8a3.5 3.5 0 0 0 0 7.5Z" />
        <path d="M19.4 15a1.8 1.8 0 0 0 .36 1.98l.05.05a2.1 2.1 0 1 1-2.97 2.97l-.05-.05A1.8 1.8 0 0 0 15 19.4a1.8 1.8 0 0 0-1 .6l-.03.03a2.1 2.1 0 1 1-2.97-2.97l.03-.03A1.8 1.8 0 0 0 11.6 15a1.8 1.8 0 0 0-1.6-1h-.1a2.1 2.1 0 1 1 0-4.2h.1a1.8 1.8 0 0 0 1.6-1 1.8 1.8 0 0 0-.36-1.98l-.05-.05a2.1 2.1 0 1 1 2.97-2.97l.05.05A1.8 1.8 0 0 0 15 4.6a1.8 1.8 0 0 0 1-.6l.03-.03A2.1 2.1 0 1 1 19 6.94l-.03.03A1.8 1.8 0 0 0 18.4 9c.2.6.75 1 1.4 1h.1a2.1 2.1 0 1 1 0 4.2h-.1a1.8 1.8 0 0 0-1.4.8Z" />
    </svg>
);

const Card: React.FC<CardProps> = ({
    onClick,
    title = 'Utilerías',
}) => {
    const handleClick = () => {
        if (onClick) {
            onClick();
            return;
        }

        navigateToUrl('/util');
    };

    return (
        <div
            role="button"
            tabIndex={0}
            aria-label={title}
            onClick={handleClick}
            onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    handleClick();
                }
            }}
            style={{
                width: '255px',
                height: '246px',
                minWidth: '255px',
                minHeight: '246px',
                border: '1px solid rgba(0, 0, 0, 0.12)',
                backgroundColor: '#FFFFFF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                boxSizing: 'border-box',
                transition: 'box-shadow 0.2s ease, border-color 0.2s ease, transform 0.2s ease',
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.12)';
                e.currentTarget.style.borderColor = '#D0D0D0';
                e.currentTarget.style.transform = 'translateY(-1px)';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.boxShadow = 'none';
                e.currentTarget.style.borderColor = 'rgba(0, 0, 0, 0.12)';
                e.currentTarget.style.transform = 'translateY(0)';
            }}
        >
            <div
                style={{
                    width: '100%',
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    textAlign: 'center',
                    boxSizing: 'border-box',
                }}
            >
                <div
                    style={{
                        width: '110px',
                        height: '110px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                    }}
                >
                    <div
                        style={{
                            width: '78px',
                            height: '78px',
                            borderRadius: '50%',
                            backgroundColor: '#FAFAFC',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: '#003865',
                            flexShrink: 0,
                        }}
                    >
                        <UtilIcon />
                    </div>
                </div>

                <h2
                    style={{
                        margin: 0,
                        fontSize: '18px',
                        fontWeight: 500,
                        color: '#000000',
                        lineHeight: '27px',
                        fontFamily: 'inherit',
                    }}
                >
                    {title}
                </h2>
            </div>
        </div>
    );
};

const lifecycles = singleSpaReact({
    React,
    ReactDOMClient,
    rootComponent: Card,
    errorBoundary() {
        return <div>Error al cargar el módulo de utilerías</div>;
    },
});

export const { bootstrap, mount, unmount } = lifecycles;
export default Card;