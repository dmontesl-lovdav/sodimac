import { RelationPage } from './components/RelationPage';
import {
  useApplicationEventAssignment,
  useApplicationEventSearch,
  useSaveApplicationEventAssignment,
} from './hooks/useSecurity';

export function ApplicationEventPage() {
  return (
    <RelationPage
      title="Aplicativo Evento"
      subtitle="Por cada módulo (aplicativo), vincula procesos (eventos)."
      breadcrumb={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Aplicativo Evento']}
      useSearchHook={useApplicationEventSearch}
      useAssignmentHook={useApplicationEventAssignment}
      useSaveHook={useSaveApplicationEventAssignment}
      leftTitle="Eventos (procesos) disponibles"
      rightTitle="Eventos vinculados al aplicativo"
    />
  );
}
