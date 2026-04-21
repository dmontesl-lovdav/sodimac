import { Link } from 'react-router-dom';
import { ComponentType, KeyboardEvent } from 'react';
import './FiscalCard.css';
export interface FiscalCardProps {
    title: string;
    description: string;
    to?: string;
    Icon: ComponentType;
    dark?: boolean;
    className?: string;
    onClick?: () => void | Promise<void>;
}

export default function FiscalCard({
    title,
    description,
    to,
    Icon,
    dark = false,
    className = "",
    onClick,
}: FiscalCardProps): React.ReactElement {
    const cardClass = `fiscal-card ${dark ? 'fiscal-card-dark' : 'fiscal-card-light'} ${className}`.trim();

    const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>): void => {
        if (!onClick) return;

        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            onClick();
        }
    };

    const content = (
        <>
            <div className="fiscal-card-icon">
                <Icon />
            </div>

            <div className="fiscal-card-content">
                <h4 className="fiscal-card-title">{title}</h4>
                <p className="fiscal-card-description">{description}</p>
            </div>
        </>
    );

    return (
        <div className={cardClass}>
            {to ? (
                <Link to={to} className="fiscal-card-link">
                    {content}
                </Link>
            ) : (
                <div
                    className="fiscal-card-link"
                    role="button"
                    tabIndex={0}
                    onClick={onClick}
                    onKeyDown={handleKeyDown}
                >
                    {content}
                </div>
            )}
        </div>
    );
}