import { Link } from 'react-router-dom';
import './Breadcrumb.css';

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

                        {isLast || !to ? (
                            <span className="breadcrumb-current">{label}</span>
                        ) : external ? (
                            <a href={to} className="breadcrumb-link">
                                {label}
                            </a>
                        ) : (
                            <Link to={to} state={state} className="breadcrumb-link">
                                {label}
                            </Link>
                        )}
                    </span>
                );
            })}
        </nav>
    );
}
