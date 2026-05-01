import SharedBreadcrumb from '@shared/components/ui/navigation/Breadcrumb';

export function SecurityBreadcrumb({ items }: { items: string[] }) {
  const routeByLabel: Record<string, string> = {
    Inicio: '/',
    'Herramientas y Utilerias': '/util',
    'Herramientas y Utilerías': '/util',
    Seguridad: '/seguridad',
    'Control de Acceso': '/seguridad',
  };

  return (
    <SharedBreadcrumb
      items={items.map((label, index) => ({
        label,
        to: index < items.length - 1 ? routeByLabel[label] : undefined,
      }))}
    />
  );
}

