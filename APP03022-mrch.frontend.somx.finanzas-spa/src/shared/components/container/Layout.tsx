import type { ReactNode } from 'react';
import './Layout.css';

type Props = {
    children: ReactNode;
};

export const Layout = ({ children }: Props) => {
    return <div className="finanzas-layout">{children}</div>;
};