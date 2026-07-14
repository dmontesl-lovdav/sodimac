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

type DisplayCrumb = BreadcrumbItem & {
  externalHref?: string;
};

export function getFbcHomeUrl(): string {
  const raw = process.env.FBC_HOME ?? '';
  return raw.trim() || '/';
}

export function isHomeCrumb(item: BreadcrumbItem): boolean {
  const normalized = item.label.trim().toLowerCase();
  return normalized === 'home' || normalized === 'inicio';
}

export function buildDisplayItems(items: BreadcrumbItem[]): DisplayCrumb[] {
  const rest =
    items.length > 0 && isHomeCrumb(items[0]) ? items.slice(1) : items;

  return [{ label: 'Inicio', externalHref: getFbcHomeUrl() }, ...rest];
}

function renderCrumb(item: DisplayCrumb, isLast: boolean): React.ReactNode {
  if (item.externalHref) {
    return (
      <a href={item.externalHref} className="fiscal-breadcrumb-link" rel="noopener noreferrer">
        {item.label}
      </a>
    );
  }
  if (isLast || !item.to) {
    return <span className="fiscal-breadcrumb-current">{item.label}</span>;
  }
  return <Link to={item.to} className="fiscal-breadcrumb-link">{item.label}</Link>;
}

export { renderCrumb };

export default function Breadcrumb({ items = [], className = '' }: BreadcrumbProps): React.ReactElement {
  const navClass = `fiscal-breadcrumb ${className}`.trim();
  const displayItems = buildDisplayItems(items);

  return (
    <nav className={navClass}>
      {displayItems.map((item, idx) => {
        const isLast = idx === displayItems.length - 1;
        const key = `${idx}-${item.label}`;

        return (
          <span key={key} className="fiscal-breadcrumb-item">
            {idx > 0 ? <span className="fiscal-breadcrumb-separator">{'>'}</span> : null}
            {renderCrumb(item, isLast)}
          </span>
        );
      })}
    </nav>
  );
}
