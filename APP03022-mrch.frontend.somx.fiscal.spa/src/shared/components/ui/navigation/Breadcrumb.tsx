import { Link } from 'react-router-dom';
import './Breadcrumb.css';

export type BreadcrumbItem = {
  label: string;
  to?: string;
};

export interface BreadcrumbProps {
  items?: BreadcrumbItem[];
  className?: string;
}

export default function Breadcrumb({ items = [], className = '' }: BreadcrumbProps): React.ReactElement {
  const navClass = `fiscal-breadcrumb ${className}`.trim();
  return (
    <nav className={navClass}>
      {items.map(({ label, to }, idx) => {
        const isLast = idx === items.length - 1;
        return (
          <span key={label} className="fiscal-breadcrumb-item">
            {idx > 0 ? <span className="fiscal-breadcrumb-separator">{'>'}</span> : null}
            {isLast || !to ? (
              <span className="fiscal-breadcrumb-current">{label}</span>
            ) : (
              <Link to={to} className="fiscal-breadcrumb-link">
                {label}
              </Link>
            )}
          </span>
        );
      })}
    </nav>
  );
}
