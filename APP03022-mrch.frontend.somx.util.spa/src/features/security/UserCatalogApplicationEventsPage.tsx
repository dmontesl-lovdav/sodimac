import { useEffect, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getErrorMessage, useAlertModal } from '@shared/hooks/useAlertModal';
import { GenericTable } from '@shared/components/ui/table';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import { useUserCatalogApplicationEvents } from './hooks/useUserCatalog';
import type { UserApplicationEventRow } from './types';
import './styles/SecurityCommon.css';

export function UserCatalogApplicationEventsPage() {
  const { userId: uid, moduleId: mid } = useParams<{ userId: string; moduleId: string }>();
  const userId = Number(uid);
  const moduleId = Number(mid);
  const navigate = useNavigate();
  const { showAlert, alertModal } = useAlertModal();

  const eventsQuery = useUserCatalogApplicationEvents(userId, moduleId, userId > 0 && moduleId > 0);

  useEffect(() => {
    if (eventsQuery.error) console.error('[Eventos aplicación]', eventsQuery.error);
  }, [eventsQuery.error]);

  useEffect(() => {
    if (!eventsQuery.isError) return;
    showAlert({
      title: 'Error',
      message: getErrorMessage(eventsQuery.error, 'Error al cargar eventos. Intente de nuevo.'),
      severity: 'error',
    });
  }, [eventsQuery.isError, eventsQuery.error, showAlert]);

  useEffect(() => {
    if (!eventsQuery.isSuccess || eventsQuery.isFetching) return;
    const app = eventsQuery.data?.application;
    const list = eventsQuery.data?.events ?? [];
    if (!app || list.length > 0) return;
    showAlert({
      title: 'Sin eventos',
      message: 'No hay eventos registrados para este aplicativo en el contexto consultado.',
      severity: 'info',
    });
  }, [eventsQuery.isSuccess, eventsQuery.isFetching, eventsQuery.data, showAlert]);

  const app = eventsQuery.data?.application;
  const rawEvents = eventsQuery.data?.events ?? [];
  const rows = useMemo(() => {
    const byProcess = new Map<number, UserApplicationEventRow>();
    for (const ev of rawEvents) {
      const prev = byProcess.get(ev.processId);
      if (!prev || (!prev.assigned && ev.assigned)) {
        byProcess.set(ev.processId, ev);
      }
    }
    return Array.from(byProcess.values());
  }, [rawEvents]);
  const columns = [
    {
      header: 'Id Evento',
      render: (row: UserApplicationEventRow) => row.processId,
    },
    {
      header: 'Nombre',
      render: (row: UserApplicationEventRow) => row.name,
    },
    {
      header: 'Descripción',
      render: (row: UserApplicationEventRow) => row.description,
    },
    {
      header: 'Asignado',
      align: 'center' as const,
      render: (row: UserApplicationEventRow) => (row.assigned ? '✓' : '✗'),
    },
  ];

  return (
    <div className="security-layout">
      <SecurityBreadcrumb
        items={[
          'Inicio',
          'Herramientas y Utilerias',
          'Control de Acceso',
          'Catálogo Usuarios',
          `Usuario ${userId}`,
          'Eventos',
        ]}
      />
      <div className="security-box">
        {app ? (
          <div>
            <h1 className="security-title">Eventos del aplicativo</h1>
            <p className="security-subtitle">
              Solo lectura. ✓ indica evento asignado al usuario; ✗ indica que no está asignado.
            </p>
            <p>
              <strong>Id Aplicación:</strong> {app.id} · <strong>Nombre:</strong> {app.name}
            </p>
            <p className="security-subtitle">{app.description}</p>
          </div>
        ) : null}
        <div style={{ marginTop: '1rem' }}>
          <GenericTable rows={rows} columns={columns} emptyLabel="Sin eventos." showPagination={false} />
        </div>
        {eventsQuery.isLoading ? <div className="security-empty">Cargando eventos...</div> : null}
        <div className="dual-back">
          <GenericButton variant="back" onClick={() => navigate(`/seguridad/gestion-usuarios/${userId}`)}>
            Volver al detalle
          </GenericButton>
        </div>
      </div>
      {eventsQuery.isFetching ? <GenericModal visible variant="loading" message="Cargando..." /> : null}
      {alertModal}
    </div>
  );
}
