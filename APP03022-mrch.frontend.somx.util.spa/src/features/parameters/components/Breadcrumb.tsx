import { FC } from 'react';
import SharedBreadcrumb from '@shared/components/ui/navigation/Breadcrumb';

interface BreadcrumbProps {
  items: string[];
}

const HOME_PATH = (typeof process !== 'undefined' && process.env?.FBC_HOME) || '/';

const KNOWN_ROUTES: Record<string, string> = {
  'Inicio': HOME_PATH,
  'Herramientas y Utilerías': '/util',
  'Configuración de Parámetros': '/util/parametros',
};

export const Breadcrumb: FC<BreadcrumbProps> = ({ items }) => {
  const breadcrumbItems = items.map((label, index) => {
    const isLast = index === items.length - 1;
    return {
      label,
      to: isLast ? undefined : KNOWN_ROUTES[label],
      external: label === 'Inicio',
    };
  });

  return <SharedBreadcrumb items={breadcrumbItems} />;
};
