import { Link } from 'react-router-dom';
import './styles/Breadcrumb.css';

export interface BreadcrumbItem {
  label: string;
  to?: string;
  onClick?: () => void;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

type DisplayCrumb = BreadcrumbItem & {
  externalHref?: string;
};

function getFbcHomeUrl(): string {
  const raw = process.env.FBC_HOME ?? '';
  return raw.trim() || '/';
}

function isHomeCrumb(item: BreadcrumbItem): boolean {
  const normalized = item.label.trim().toLowerCase();
  return normalized === 'home' || normalized === 'inicio';
}

function buildDisplayItems(items: BreadcrumbItem[]): DisplayCrumb[] {
  const rest =
    items.length > 0 && isHomeCrumb(items[0]) ? items.slice(1) : items;

  return [{ label: 'Inicio', externalHref: getFbcHomeUrl() }, ...rest];
}

function renderCrumbContent(item: DisplayCrumb, isLast: boolean) {
  const { label, to, onClick, externalHref } = item;

  if (externalHref) {
    return (
      <a
        href={externalHref}
        className="breadcrumb-link"
        rel="noopener noreferrer"
      >
        {label}
      </a>
    );
  }

  if (isLast) {
    return <span className="breadcrumb-current">{label}</span>;
  }

  if (onClick) {
    return (
      <button
        type="button"
        onClick={onClick}
        className="breadcrumb-link"
        style={{
          background: 'none',
          border: 'none',
          padding: 0,
          cursor: 'pointer',
          font: 'inherit',
        }}
      >
        {label}
      </button>
    );
  }

  if (to) {
    return (
      <Link to={to} className="breadcrumb-link">
        {label}
      </Link>
    );
  }

  return <span className="breadcrumb-current">{label}</span>;
}

/**
 * El primer ítem siempre es "Inicio" y enlaza a FBC_HOME (.env).
 * El último ítem no recibe enlace porque es el paso activo.
 * Si un ítem trae `onClick`, se renderiza como botón (útil para navegar con state).
 */
export default function Breadcrumb({ items }: BreadcrumbProps) {
  const displayItems = buildDisplayItems(items);

  return (
    <nav className="breadcrumb">
      {displayItems.map((item, idx) => {
        const isLast = idx === displayItems.length - 1;

        return (
          <span key={`${item.label}-${idx}`} className="breadcrumb-item">
            {idx > 0 && (
              <span className="breadcrumb-separator">{'>'}</span>
            )}
            {renderCrumbContent(item, isLast)}
          </span>
        );
      })}
    </nav>
  );
}
