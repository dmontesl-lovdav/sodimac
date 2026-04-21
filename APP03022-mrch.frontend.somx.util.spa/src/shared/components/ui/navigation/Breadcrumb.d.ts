import { FC } from 'react';

interface BreadcrumbItem {
    label: string;
    to?: string;
}

interface BreadcrumbProps {
    items: BreadcrumbItem[];
}

declare const Breadcrumb: FC<BreadcrumbProps>;
export default Breadcrumb;
