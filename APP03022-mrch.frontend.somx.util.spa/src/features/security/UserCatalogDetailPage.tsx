import { Fragment, useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getErrorMessage, useAlertModal } from '@shared/hooks/useAlertModal';
import { GenericTable } from '@shared/components/ui/table';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import { CatalogDetailSection } from './components/CatalogDetailSection';
import { SecurityTablePagination } from './components/SecurityTablePagination';
import { useUserCatalogDetail } from './hooks/useUserCatalog';
import type { PermissionEventMatrixRow, UserCatalogSearchFilters } from './types';
import './styles/SecurityCommon.css';
import './styles/UserCatalogDetailPage.css';

type AppEvent = { processId: number; moduleProcessId: number; name: string; description: string; assigned: boolean };
type AppRow = { id: number; name: string; description: string; events?: AppEvent[] };

function dedupeEventsByProcessId(events: AppEvent[] | undefined): AppEvent[] {
  const map = (events ?? []).reduce((acc, ev) => {
    const prev = acc.get(ev.processId);
    if (!prev || (!prev.assigned && ev.assigned)) acc.set(ev.processId, ev);
    return acc;
  }, new Map<number, AppEvent>());
  return Array.from(map.values());
}

function EventsSubtable({ events }: { events: AppEvent[] }) {
  if (events.length === 0) {
    return (
      <tr>
        <td colSpan={4} style={{ textAlign: 'center' }}>
          <div style={{ padding: '1.5rem', color: '#6b7280' }}>Sin eventos.</div>
        </td>
      </tr>
    );
  }
  return (
    <>
      {events.map((ev) => (
        <tr key={`${ev.moduleProcessId}-${ev.processId}`}>
          <td>{ev.processId}</td>
          <td>{ev.name}</td>
          <td>{ev.description}</td>
          <td className="user-catalog-detail__check">{ev.assigned ? '✓' : 'X'}</td>
        </tr>
      ))}
    </>
  );
}

function AppRowWithEvents({ row }: { row: AppRow }) {
  const events = dedupeEventsByProcessId(row.events);
  return (
    <Fragment key={row.id}>
      <tr>
        <td>{row.id}</td>
        <td>{row.name}</td>
        <td>{row.description}</td>
      </tr>
      <tr className="user-catalog-detail__app-nested-row">
        <td colSpan={3}>
          <div className="user-catalog-detail__app-nested">
            <p className="user-catalog-detail__app-nested-title">Eventos del aplicativo</p>
            <table className="security-table user-catalog-detail__nested-events-table">
              <thead>
                <tr>
                  <th>Id evento</th>
                  <th>Nombre</th>
                  <th>Clave</th>
                  <th className="user-catalog-detail__check">Asignado</th>
                </tr>
              </thead>
              <tbody>
                <EventsSubtable events={events} />
              </tbody>
            </table>
          </div>
        </td>
      </tr>
    </Fragment>
  );
}

export function UserCatalogDetailPage() {
  const { userId: userIdParam } = useParams<{ userId: string }>();
  const userId = Number(userIdParam);
  const location = useLocation();
  const navigate = useNavigate();
  const catalogNav = location.state as { catalogFilters?: UserCatalogSearchFilters } | null | undefined;
  const catalogListLinkState =
    catalogNav?.catalogFilters != null ? { catalogFilters: catalogNav.catalogFilters } : undefined;

  const handleBack = () => {
    navigate('/seguridad/gestion-usuarios', {
      state: catalogListLinkState,
    });
  };

  const [rolesPage, setRolesPage] = useState(1);
  const [applicationsPage, setApplicationsPage] = useState(1);
  const [attributesPage, setAttributesPage] = useState(1);
  const [matrixPage, setMatrixPage] = useState(1);
  const [matrixPageSize, setMatrixPageSize] = useState(15);
  const { showAlert, alertModal } = useAlertModal();

  const detailQuery = useUserCatalogDetail(
    userId,
    {
      rolesPage,
      applicationsPage,
      attributesPage,
      matrixPage,
      matrixPageSize,
    },
    userId > 0,
  );

  useEffect(() => {
    if (detailQuery.error) console.error('[Detalle catálogo usuario]', detailQuery.error);
  }, [detailQuery.error]);

  useEffect(() => {
    if (!detailQuery.isError) return;
    showAlert({
      title: 'Error',
      message: getErrorMessage(detailQuery.error, 'No fue posible cargar el detalle. Revise bitácora o intente de nuevo.'),
      severity: 'error',
    });
  }, [detailQuery.isError, detailQuery.error, showAlert]);

  const d = detailQuery.data;
  const appsSection = d?.applications;
  const totalAppPages = appsSection ? Math.max(1, Math.ceil(appsSection.total / appsSection.pageSize)) : 1;
  const matrix = d?.permissionEventMatrix;
  const totalMatrixPages = matrix ? Math.max(1, Math.ceil(matrix.total / matrix.pageSize)) : 1;
  const matrixColumns = [
    {
      header: 'Permiso',
      render: (row: PermissionEventMatrixRow) => (
        <>
          {row.permissionName}
          <br />
          <small style={{ color: '#666' }}>{row.permissionKey}</small>
        </>
      ),
    },
    {
      header: 'Rol',
      render: (row: PermissionEventMatrixRow) => row.roleName,
    },
    {
      header: 'Aplicativo',
      render: (row: PermissionEventMatrixRow) => row.moduleName,
    },
    {
      header: 'Evento',
      render: (row: PermissionEventMatrixRow) => (
        <>
          {row.processName}
          <br />
          <small style={{ color: '#666' }}>{row.processKey}</small>
        </>
      ),
    },
    {
      header: 'Vigente',
      align: 'center' as const,
      render: (row: PermissionEventMatrixRow) => (row.effective ? '✓' : 'X'),
    },
  ];

  if (!userId || Number.isNaN(userId)) {
    return <div className="security-layout security-box">Identificador de usuario no válido.</div>;
  }

  return (
    <div className="security-layout">
      <SecurityBreadcrumb
        items={[
          'Inicio',
          'Herramientas y Utilerias',
          'Control de Acceso',
          'Catálogo Usuarios',
          d?.header.fullName ?? `Usuario ${userId}`,
        ]}
        linkStateByLabel={
          catalogListLinkState ? { 'Catálogo Usuarios': catalogListLinkState } : undefined
        }
      />
      <div className="security-box">
        <h1 className="security-title" style={{ marginTop: '1rem' }}>
          Detalle de usuario
        </h1>
        <p className="security-subtitle" style={{ marginTop: '0.35rem' }}>
          Vista solo lectura del perfil efectivo, roles, aplicaciones, matriz permiso–evento y atributos.
        </p>

        {d ? (
          <>
            <div className="user-catalog-detail__summary-profile-row">
              <div className="user-catalog-detail__card">
                <span className="user-catalog-detail__badge">Resumen</span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1.25rem 2rem' }}>
                  <div>
                    <div>
                      <strong>Id Usuario:</strong> {d.header.id}
                    </div>
                    <div>
                      <strong>Usuario:</strong> {d.header.username}
                    </div>
                    <div>
                      <strong>Nombre:</strong> {d.header.fullName}
                    </div>
                  </div>
                  <div>
                    <div>
                      <strong>Email:</strong> {d.header.email ?? '—'}
                    </div>
                    <div>
                      <strong>Creación:</strong> {d.header.createdAt?.split('T')[0]}
                    </div>
                    <div>
                      <strong>Modificación:</strong> {d.header.modifiedAt?.split('T')[0]}
                    </div>
                  </div>
                  <div>
                    <div>
                      <strong>Estatus:</strong>{' '}
                      <span className={`security-status ${d.header.status === 1 ? 'active' : 'inactive'}`}>
                        {d.header.status === 1 ? 'Activo' : 'Inactivo'}
                      </span>
                    </div>
                    <div style={{ marginTop: '0.35rem' }}>
                      <strong>Contexto:</strong> <code>{d.userLookupKey}</code>
                    </div>
                  </div>
                </div>
              </div>
              <div className="user-catalog-detail__card">
                <span className="user-catalog-detail__badge">Perfil</span>
                
                {d.multipleProfilesDetected ? (
                  <div className="user-catalog-detail__warn">
                    Se detectaron varios perfiles en datos; revise la configuración. Se muestra el primero como
                    referencia.
                  </div>
                ) : null}
                {d.profile ? (
                  <>
                    <p className="user-catalog-detail__profile-value">{d.profile.name}</p>
                    <p style={{ margin: '0.35rem 0 0', fontSize: '0.9rem', color: '#555' }}>
                      Id {d.profile.id} · {d.profile.description}
                    </p>
                  </>
                ) : (
                  <p className="security-empty">Sin perfil configurado.</p>
                )}
              </div>
            </div>

            <div className="user-catalog-detail">
              <div className="user-catalog-detail__span-full">
                <CatalogDetailSection
                  title="Roles"
                  columns={[
                    { key: 'id', label: 'Id Rol' },
                    { key: 'name', label: 'Nombre' },
                    { key: 'description', label: 'Clave' },
                  ]}
                  section={d.roles}
                  onPageChange={setRolesPage}
                />
              </div>

            <div className="user-catalog-detail__span-full">
              <section className="security-box user-catalog-detail__card user-catalog-detail__table-surface user-catalog-detail__apps-section">
                <h2 className="user-catalog-detail__section-title">Aplicaciones</h2>
                <div className="user-catalog-detail__apps-table-wrap">
                  <table className="security-table">
                  <thead>
                    <tr>
                      <th>Id</th>
                      <th>Nombre</th>
                      <th>Clave</th>
                    </tr>
                  </thead>
                  <tbody>
                    {appsSection && appsSection.items.length === 0 ? (
                      <tr>
                        <td colSpan={3} style={{ textAlign: 'center' }}>
                          <div style={{ padding: '1.5rem', color: '#6b7280' }}>Sin aplicaciones.</div>
                        </td>
                      </tr>
                    ) : null}
                    {appsSection?.items.map((row) => (
                      <AppRowWithEvents key={row.id} row={row as AppRow} />
                    ))}
                  </tbody>
                  </table>
                </div>
                {appsSection && appsSection.total > 0 ? (
                  <SecurityTablePagination
                    currentPage={appsSection.page}
                    totalPages={totalAppPages}
                    totalItems={appsSection.total}
                    pageSize={appsSection.pageSize}
                    onPrev={() => setApplicationsPage((p) => Math.max(1, p - 1))}
                    onNext={() => setApplicationsPage((p) => Math.min(totalAppPages, p + 1))}
                    onPageSizeChange={() => {}}
                    showPageSizeSelector={false}
                  />
                ) : null}
              </section>
            </div>

            <div className="user-catalog-detail__span-full">
              <section className="security-box user-catalog-detail__card user-catalog-detail__table-surface" style={{ margin: 0 }}>
                <h2 className="user-catalog-detail__section-title">Permisos × eventos</h2>
                <div className="user-catalog-detail__matrix-wrap">
                  <GenericTable
                    rows={matrix?.items ?? []}
                    columns={matrixColumns}
                    emptyLabel="Sin filas: se requieren permisos por rol y eventos por aplicativo para formar la matriz."
                    perPage={matrix?.pageSize ?? matrixPageSize}
                    page={matrix?.page ?? 1}
                    totalPages={totalMatrixPages}
                    totalItems={matrix?.total ?? 0}
                    onChangePage={setMatrixPage}
                    onChangePerPage={(value) => {
                      setMatrixPageSize(value);
                      setMatrixPage(1);
                    }}
                    showPagination={Boolean(matrix && matrix.total > 0)}
                  />
                </div>
              </section>
            </div>

            <div className="user-catalog-detail__span-full">
              <CatalogDetailSection
                title="Atributos"
                columns={[
                  { key: 'id', label: 'Id' },
                  { key: 'name', label: 'Nombre' },
                  { key: 'description', label: 'Clave' },
                ]}
                section={d.attributes}
                onPageChange={setAttributesPage}
              />
            </div>
            </div>
          </>
        ) : null}

        <div className="dual-back">
          <GenericButton variant="back" onClick={handleBack}>
            Volver
          </GenericButton>
        </div>
      </div>
      {detailQuery.isFetching ? <GenericModal visible variant="loading" message="Cargando información..." /> : null}
      {alertModal}
    </div>
  );
}
