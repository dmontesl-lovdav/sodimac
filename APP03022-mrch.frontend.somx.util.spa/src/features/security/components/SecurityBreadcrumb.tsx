import SharedBreadcrumb from '@shared/components/ui/navigation/Breadcrumb';

export function SecurityBreadcrumb({
  items,
  linkStateByLabel,
}: {
  items: string[];
  /** Estado de React Router para enlaces concretos (p. ej. devolver filtros al catálogo). */
  linkStateByLabel?: Record<string, unknown>;
}) {
  const routeByLabel: Record<string, string> = {
    Inicio: '/',
    'Herramientas y Utilerias': '/util',
    'Herramientas y Utilerías': '/util',
    Seguridad: '/seguridad',
    'Control de Acceso': '/seguridad',
    'Catálogo Usuarios': '/seguridad/gestion-usuarios',
    'Aplicativo Evento': '/seguridad/aplicativo-evento',
    'Perfil Aplicativo': '/seguridad/perfil-aplicativo',
    'Perfil Evento': '/seguridad/perfil-evento',
    'Perfil Usuario': '/seguridad/perfil-usuario',
    'Rol Usuario': '/seguridad/rol-usuario',
    'Rol Permiso': '/seguridad/rol-permiso',
    'Usuario Atributo': '/seguridad/usuario-atributo',
  };

  return (
    <SharedBreadcrumb
      items={items.map((label, index) => ({
        label,
        to: index < items.length - 1 ? routeByLabel[label] : undefined,
        state: linkStateByLabel?.[label],
      }))}
    />
  );
}

