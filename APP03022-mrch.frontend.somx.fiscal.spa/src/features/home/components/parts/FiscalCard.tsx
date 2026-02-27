import { Link } from 'react-router-dom';
import { ComponentType, useState } from 'react';

interface FiscalCardProps {
    title: string;
    description: string;
    to: string;
    buttonText: string;
    Icon: ComponentType;
    dark?: boolean;
}

const styles = {
    card: {
        position: 'relative' as const,
        display: 'flex',
        flexDirection: 'column' as const,
        justifyContent: 'space-between',
        borderRadius: '1rem',
        border: '1px solid #CFE1F5',
        padding: '2rem',
        boxShadow: '0 1px 2px 0 rgb(0 0 0 / 0.05)',
        transition: 'box-shadow 200ms ease',
        minHeight: '180px',
    },
    cardLight: {
        backgroundColor: '#EAF3FB',
    },
    cardDark: {
        backgroundColor: '#E4EFF7',
    },
    cardHover: {
        boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
    },
    content: {
        paddingRight: '7rem',
    },
    title: {
        fontWeight: 500,
        fontSize: '1.25rem',
        color: '#333333',
        margin: 0,
    },
    description: {
        fontSize: '0.875rem',
        color: '#374151',
        marginTop: '0.75rem',
        maxWidth: '28rem',
        lineHeight: 1.5,
    },
    link: {
        marginTop: '2rem',
        alignSelf: 'flex-start',
        textDecoration: 'none',
    },
    buttonLight: {
        padding: '0.5rem 1.25rem',
        borderRadius: '0.25rem',
        fontSize: '0.875rem',
        fontWeight: 500,
        cursor: 'pointer',
        border: '1px solid #003865',
        color: '#003865',
        backgroundColor: 'transparent',
        transition: 'background-color 150ms ease',
    },
    buttonDark: {
        padding: '0.5rem 1.25rem',
        borderRadius: '0.25rem',
        fontSize: '0.875rem',
        fontWeight: 500,
        cursor: 'pointer',
        border: 'none',
        backgroundColor: '#002B55',
        color: '#ffffff',
        transition: 'filter 150ms ease',
    },
    iconWrapper: {
        position: 'absolute' as const,
        right: '1.5rem',
        top: '50%',
        transform: 'translateY(-50%)',
        height: '6rem',
        width: '6rem',
        pointerEvents: 'none' as const,
        userSelect: 'none' as const,
        opacity: 0.9,
    },
};

export default function FiscalCard({ title, description, to, buttonText, Icon, dark = false }: FiscalCardProps) {
    const [isHovered, setIsHovered] = useState(false);
    const [isButtonHovered, setIsButtonHovered] = useState(false);

    const cardStyle = {
        ...styles.card,
        ...(dark ? styles.cardDark : styles.cardLight),
        ...(isHovered ? styles.cardHover : {}),
    };

    const buttonStyle = dark
        ? {
            ...styles.buttonDark,
            ...(isButtonHovered ? { filter: 'brightness(1.1)' } : {}),
        }
        : {
            ...styles.buttonLight,
            ...(isButtonHovered ? { backgroundColor: '#e6f1ff' } : {}),
        };

    return (
        <div
            style={cardStyle}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <div style={styles.content}>
                <h4 style={styles.title}>{title}</h4>
                <p style={styles.description}>{description}</p>
            </div>

            <Link to={to} style={styles.link}>
                <button
                    style={buttonStyle}
                    onMouseEnter={() => setIsButtonHovered(true)}
                    onMouseLeave={() => setIsButtonHovered(false)}
                >
                    {buttonText}
                </button>
            </Link>

            <div style={styles.iconWrapper}>
                <Icon />
            </div>
        </div>
    );
}
