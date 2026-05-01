import { RelationPage } from './components/RelationPage';
import {
  useProfileModuleAssignment,
  useProfileModuleSearch,
  useSaveProfileModuleAssignment,
} from './hooks/useSecurity';

export function ProfileModulePage() {
  return (
    <RelationPage
      title="Perfil Aplicativo"
      subtitle="Asigna módulos (aplicativos) a cada perfil."
      breadcrumb={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Perfil Aplicativo']}
      useSearchHook={useProfileModuleSearch}
      useAssignmentHook={useProfileModuleAssignment}
      useSaveHook={useSaveProfileModuleAssignment}
      leftTitle="Módulos disponibles"
      rightTitle="Módulos asignados al perfil"
    />
  );
}
