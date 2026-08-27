import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import './Breadcrumb.css';

function renderBreadcrumbLabel({ label, to, state, external, onClick, isLast }) {
    if (isLast) {
        return <span className="breadcrumb-current">{label}</span>;
    }
    if (to) {
        if (external) {
            return (
                <a href={to} className="breadcrumb-link">
                    {label}
                </a>
            );
        }
        return (
            <Link to={to} state={state} className="breadcrumb-link">
                {label}
            </Link>
        );
    }
    if (onClick) {
        return (
            <button
                type="button"
                className="breadcrumb-link"
                onClick={onClick}
                style={{ background: 'none', border: 'none', padding: 0, font: 'inherit', cursor: 'pointer' }}
            >
                {label}
            </button>
        );
    }
    return <span className="breadcrumb-current">{label}</span>;
}

export default function Breadcrumb({ items = [] }) {
    return (
        <nav className="breadcrumb">
            {items.map(({ label, to, state, external, onClick }, idx) => {
                const isLast = idx === items.length - 1;
                return (
                    <span key={label} className="breadcrumb-item">
                        {idx > 0 && (
                            <span className="breadcrumb-separator">{'/'}</span>
                        )}
                        {renderBreadcrumbLabel({ label, to, state, external, onClick, isLast })}
                    </span>
                );
            })}
        </nav>
    );
}

Breadcrumb.propTypes = {
    items: PropTypes.array,
};
