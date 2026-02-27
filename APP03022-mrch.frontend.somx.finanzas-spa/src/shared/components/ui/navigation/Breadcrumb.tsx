import { Link } from 'react-router-dom';

export type BreadcrumbItem = {
  label: string;
  to?: string;
};

interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

export default function Breadcrumb({ items = [] }: BreadcrumbProps) {
  return (
    <nav className="somx-breadcrumb">
      {items.map(({ label, to }, idx) => {
        const isLast = idx === items.length - 1;

        return (
          <span key={label} className="somx-breadcrumb-item">
            {idx > 0 && <span className="somx-breadcrumb-separator">{'>'}</span>}

            {isLast || !to ? (
              <span className="somx-breadcrumb-current">{label}</span>
            ) : (
              <Link to={to} className="somx-breadcrumb-link">
                {label}
              </Link>
            )}
          </span>
        );
      })}
    </nav>
  );
}
