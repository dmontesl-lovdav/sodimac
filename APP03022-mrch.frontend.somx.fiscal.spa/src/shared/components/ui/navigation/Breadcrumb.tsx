import { Link } from 'react-router-dom';

export type BreadcrumbItem = {
    label: string;
    to?: string;
};

interface BreadcrumbProps {
    items: BreadcrumbItem[];
}

const styles = {
    nav: {
        fontSize: '0.875rem',
        color: '#4b5563',
        padding: '0.75rem 0.125rem',
    },
    separator: {
        margin: '0 0.25rem',
    },
    current: {
        color: '#0284c7',
        fontWeight: 500,
    },
    link: {
        color: '#4b5563',
        textDecoration: 'none',
    },
};

export default function Breadcrumb({ items = [] }: BreadcrumbProps) {
    return (
        <nav style={styles.nav}>
            {items.map(({ label, to }, idx) => {
                const isLast = idx === items.length - 1;

                return (
                    <span key={label} style={{ display: 'inline-flex', alignItems: 'center' }}>
                        {idx > 0 && <span style={styles.separator}>{'>'}</span>}

                        {isLast || !to ? (
                            <span style={styles.current}>{label}</span>
                        ) : (
                            <Link
                                to={to}
                                style={styles.link}
                                onMouseEnter={(e) => (e.currentTarget.style.textDecoration = 'underline')}
                                onMouseLeave={(e) => (e.currentTarget.style.textDecoration = 'none')}
                            >
                                {label}
                            </Link>
                        )}
                    </span>
                );
            })}
        </nav>
    );
}
