import { FC } from 'react';

interface BreadcrumbItem {
    label: string;
    to?: string;
    state?: unknown;
    external?: boolean;
    onClick?: () => void;
}

interface BreadcrumbProps {
    items: BreadcrumbItem[];
}

declare const Breadcrumb: FC<BreadcrumbProps>;
export default Breadcrumb;
