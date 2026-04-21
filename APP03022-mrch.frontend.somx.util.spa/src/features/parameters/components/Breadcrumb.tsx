import { FC } from 'react';
import SharedBreadcrumb from '@shared/components/ui/navigation/Breadcrumb';

interface BreadcrumbProps {
  items: string[];
}

export const Breadcrumb: FC<BreadcrumbProps> = ({ items }) => {
  const breadcrumbItems = items.map((label, index) => ({
    label,
    to: index === 0 ? '/' : undefined,
  }));

  return <SharedBreadcrumb items={breadcrumbItems} />;
};
