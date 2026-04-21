import React from 'react';

interface CardProps {
    onClick?: () => void;
    title?: string;
    description?: string;
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
        style={{ width: '100%', height: '100%' }}
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
    title = 'Módulo Finanzas',
    description = 'Gestión de pagos, descuentos comerciales y recepciones.',
}) => {
    const handleClick = () => {
        if (onClick) {
            onClick();
        }
    };

    return (
        <div
            onClick={handleClick}
            style={{
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                borderRadius: '16px',
                border: '1px solid #D4E8D4',
                backgroundColor: '#E8F5E9',
                padding: '32px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                transition: 'box-shadow 0.2s ease',
                cursor: onClick ? 'pointer' : 'default',
                minHeight: '180px',
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.1)';
            }}
        >
            <div style={{ paddingRight: '100px' }}>
                <h4 style={{
                    fontWeight: 500,
                    fontSize: '20px',
                    color: '#333333',
                    margin: 0,
                }}>
                    {title}
                </h4>
                <p style={{
                    fontSize: '14px',
                    color: '#555555',
                    marginTop: '12px',
                    maxWidth: '300px',
                    lineHeight: '1.5',
                }}>
                    {description}
                </p>
            </div>

            <button
                onClick={(e) => {
                    e.stopPropagation();
                    handleClick();
                }}
                style={{
                    marginTop: '24px',
                    alignSelf: 'flex-start',
                    padding: '8px 20px',
                    borderRadius: '4px',
                    fontSize: '14px',
                    fontWeight: 500,
                    border: '1px solid #2E7D32',
                    color: '#2E7D32',
                    backgroundColor: 'transparent',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s ease',
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#e8f5e9';
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = 'transparent';
                }}
            >
                Ir al módulo
            </button>

            <div style={{
                position: 'absolute',
                right: '24px',
                top: '50%',
                transform: 'translateY(-50%)',
                height: '80px',
                width: '80px',
                opacity: 0.7,
                color: '#2E7D32',
                pointerEvents: 'none',
            }}>
                <FinanzasIcon />
            </div>
        </div>
    );
};

export default Card;

