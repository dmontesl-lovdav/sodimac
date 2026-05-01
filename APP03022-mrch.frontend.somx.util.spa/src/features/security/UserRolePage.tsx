import { RelationPage } from './components/RelationPage';
import {
  useRoleUserAssignment,
  useRoleUserSearch,
  useSaveRoleUserAssignment,
} from './hooks/useSecurity';

export function UserRolePage() {
  return (
    <RelationPage
      title="Rol Usuario"
      subtitle="Consulta roles activos y gestiona la relacion de usuarios por rol."
      breadcrumb={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Rol Usuario']}
      useSearchHook={useRoleUserSearch}
      useAssignmentHook={useRoleUserAssignment}
      useSaveHook={useSaveRoleUserAssignment}
      leftTitle="Usuarios disponibles"
      rightTitle="Usuarios asignados"
    />
  );
}
