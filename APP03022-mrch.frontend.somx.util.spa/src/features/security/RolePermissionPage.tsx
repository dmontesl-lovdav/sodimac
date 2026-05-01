import { RelationPage } from './components/RelationPage';
import {
  useRolePermissionAssignment,
  useRolePermissionSearch,
  useSaveRolePermissionAssignment,
} from './hooks/useSecurity';

export function RolePermissionPage() {
  return (
    <RelationPage
      title="Rol Permiso"
      subtitle="Relación entre roles y permisos existentes en el sistema."
      breadcrumb={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Rol Permiso']}
      useSearchHook={useRolePermissionSearch}
      useAssignmentHook={useRolePermissionAssignment}
      useSaveHook={useSaveRolePermissionAssignment}
      leftTitle="Permisos disponibles"
      rightTitle="Permisos asignados"
    />
  );
}
