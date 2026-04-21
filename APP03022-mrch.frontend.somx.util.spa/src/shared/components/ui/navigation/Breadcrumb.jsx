import { Link } from 'react-router-dom';
import './Breadcrumb.css';

export default function Breadcrumb({ items = [] }) {
    return (
        <nav className="breadcrumb">
            {items.map(({ label, to }, idx) => {
                const isLast = idx === items.length - 1;

                return (
                    <span key={label} className="breadcrumb-item">

                        {idx > 0 && (
                            <span className="breadcrumb-separator">{'>'}</span>
                        )}

                        {isLast || !to ? (
                            <span className="breadcrumb-current">{label}</span>
                        ) : (
                            <Link to={to} className="breadcrumb-link">
                                {label}
                            </Link>
                        )}
                    </span>
                );
            })}
        </nav>
    );
}
