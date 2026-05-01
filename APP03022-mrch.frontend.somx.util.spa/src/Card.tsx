import React from 'react';

interface CardProps {
    onClick?: () => void;
    title?: string;
    description?: string;
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
        style={{ width: '100%', height: '100%' }}
    >
        <path d="M12 15.5A3.5 3.5 0 1 0 12 8a3.5 3.5 0 0 0 0 7.5Z" />
        <path d="M19.4 15a1.8 1.8 0 0 0 .36 1.98l.05.05a2.1 2.1 0 1 1-2.97 2.97l-.05-.05A1.8 1.8 0 0 0 15 19.4a1.8 1.8 0 0 0-1 .6l-.03.03a2.1 2.1 0 1 1-2.97-2.97l.03-.03A1.8 1.8 0 0 0 11.6 15a1.8 1.8 0 0 0-1.6-1h-.1a2.1 2.1 0 1 1 0-4.2h.1a1.8 1.8 0 0 0 1.6-1 1.8 1.8 0 0 0-.36-1.98l-.05-.05a2.1 2.1 0 1 1 2.97-2.97l.05.05A1.8 1.8 0 0 0 15 4.6a1.8 1.8 0 0 0 1-.6l.03-.03A2.1 2.1 0 1 1 19 6.94l-.03.03A1.8 1.8 0 0 0 18.4 9c.2.6.75 1 1.4 1h.1a2.1 2.1 0 1 1 0 4.2h-.1a1.8 1.8 0 0 0-1.4.8Z" />
    </svg>
);

const Card: React.FC<CardProps> = ({
    onClick,
    title = 'Módulo Utilerías',
    description = 'Gestión de catálogos, configuraciones y herramientas de soporte.',
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
                <h4
                    style={{
                        fontWeight: 500,
                        fontSize: '20px',
                        color: '#333333',
                        margin: 0,
                    }}
                >
                    {title}
                </h4>

                <p
                    style={{
                        fontSize: '14px',
                        color: '#555555',
                        marginTop: '12px',
                        maxWidth: '300px',
                        lineHeight: '1.5',
                    }}
                >
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

            <div
                style={{
                    position: 'absolute',
                    right: '24px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    height: '80px',
                    width: '80px',
                    opacity: 0.7,
                    color: '#2E7D32',
                    pointerEvents: 'none',
                }}
            >
                <UtilIcon />
            </div>
        </div>
    );
};

export default Card;