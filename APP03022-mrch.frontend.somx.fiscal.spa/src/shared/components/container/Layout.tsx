import React from 'react';
import './Layout.css';

export interface LayoutProps {
  children?: React.ReactNode;
  className?: string;
}

export function Layout({ children, className = '' }: LayoutProps): React.ReactElement {
  const outerClass = `fiscal-layout-outer ${className}`.trim();
  return (
    <div className={outerClass}>
      <div className="fiscal-layout-inner">{children}</div>
    </div>
  );
}
