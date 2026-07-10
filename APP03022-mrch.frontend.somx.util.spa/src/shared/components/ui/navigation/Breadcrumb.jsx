import { Link } from 'react-router-dom';
import './Breadcrumb.css';

function renderBreadcrumbLabel({ label, to, state, external, isLast }) {
    if (isLast || !to) {
        return <span className="breadcrumb-current">{label}</span>;
    }
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

export default function Breadcrumb({ items = [] }) {
    return (
        <nav className="breadcrumb">
            {items.map(({ label, to, state, external }, idx) => {
                const isLast = idx === items.length - 1;
                return (
                    <span key={label} className="breadcrumb-item">
                        {idx > 0 && (
                            <span className="breadcrumb-separator">{'/'}</span>
                        )}
                        {renderBreadcrumbLabel({ label, to, state, external, isLast })}
                    </span>
                );
            })}
        </nav>
    );
}
