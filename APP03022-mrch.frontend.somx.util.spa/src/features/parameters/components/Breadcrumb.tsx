import type { FC } from 'react';

interface BreadcrumbProps {
  items: string[];
}

export const Breadcrumb: FC<BreadcrumbProps> = ({ items }) => {
  return (
    <nav
      aria-label="breadcrumb"
      className="fbc-breadcrumb"
    >
      <ol className="fbc-breadcrumb__list">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;

          return (
            <li
              key={item}
              className={`fbc-breadcrumb__item${
                isLast ? ' fbc-breadcrumb__item--current' : ''
              }`}
              aria-current={isLast ? 'page' : undefined}
            >
              {item}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};

