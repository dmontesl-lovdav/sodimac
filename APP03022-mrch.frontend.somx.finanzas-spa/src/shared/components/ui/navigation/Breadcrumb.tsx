import { Link } from 'react-router-dom';
import './styles/Breadcrumb.css';

export interface BreadcrumbItem {
  label: string;
  to?: string;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

/**
 * items: [{ label: 'Inicio', to: '/' }, { label: 'Centro de ayuda' }]
 * El último ítem NO recibe enlace porque es el activo.
 */
export default function Breadcrumb({ items }: BreadcrumbProps) {
  return (
    <nav className="breadcrumb">
      {items.map(({ label, to }, idx) => {
        const isLast = idx === items.length - 1;

        return (
          <span key={`${label}-${idx}`} className="breadcrumb-item">

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
