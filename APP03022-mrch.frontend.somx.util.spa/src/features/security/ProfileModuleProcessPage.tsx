import { RelationPage } from './components/RelationPage';
import {
  useProfileModuleProcessAssignment,
  useProfileModuleProcessSearch,
  useSaveProfileModuleProcessAssignment,
} from './hooks/useSecurity';

export function ProfileModuleProcessPage() {
  return (
    <RelationPage
      title="Perfil Evento"
      subtitle="Asigna pares módulo–proceso (aplicativo–evento) a cada perfil."
      breadcrumb={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Perfil Evento']}
      useSearchHook={useProfileModuleProcessSearch}
      useAssignmentHook={useProfileModuleProcessAssignment}
      useSaveHook={useSaveProfileModuleProcessAssignment}
      leftTitle="Aplicativo–evento disponibles"
      rightTitle="Asignados al perfil"
    />
  );
}
