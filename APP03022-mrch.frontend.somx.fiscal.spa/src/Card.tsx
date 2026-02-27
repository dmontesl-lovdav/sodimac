import React from 'react';

interface CardProps {
   
    onClick?: () => void;
   
    title?: string;
   
    description?: string;
}

const FiscalIcon: React.FC = () => (
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
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14,2 14,8 20,8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
        <polyline points="10,9 9,9 8,9" />
    </svg>
);

const Card: React.FC<CardProps> = ({
    onClick,
    title = 'Módulo Fiscal',
    description = 'Gestión de facturas, complementos de pago y documentos fiscales.',
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
                border: '1px solid #CFE1F5',
                backgroundColor: '#EAF3FB',
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
                    border: '1px solid #003865',
                    color: '#003865',
                    backgroundColor: 'transparent',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s ease',
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#e6f1ff';
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
                color: '#003865',
                pointerEvents: 'none',
            }}>
                <FiscalIcon />
            </div>
        </div>
    );
};

export default Card;

