import { RelationPage } from './components/RelationPage';
import {
  useProfileUserAssignment,
  useProfileUserSearch,
  useSaveProfileUserAssignment,
} from './hooks/useSecurity';

export function ProfileUserPage() {
  return (
    <RelationPage
      title="Perfil Usuario"
      subtitle="Relación entre perfiles y usuarios existentes en el sistema."
      breadcrumb={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Perfil Usuario']}
      useSearchHook={useProfileUserSearch}
      useAssignmentHook={useProfileUserAssignment}
      useSaveHook={useSaveProfileUserAssignment}
      leftTitle="Usuarios disponibles"
      rightTitle="Usuarios asignados"
    />
  );
}
