// src/features/requests/components/RequestGrid.jsx
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { buildStatusPill } from '@shared/utils/statusPill';
import { translateDate, translateIdToString } from './RequestUtils';

import { GenericButton, GenericLinearProgress, GenericTable, GenericModal } from '@shared/components/ui';
import RequestBoard from './RequestBoard';
import RequestFilters from './RequestFilters';
import RequestBoardSummary from './RequestBoardSummary';

import deleteIcon from '@assets/delete.svg';
import eyeIcon from '@assets/eye-show.svg';

import { useAppSelector } from '@/store/hooks/useAppSelector';
import '../styles/RequestGrid.css';

const RAW_STATUS_CLAZZ_OPTIONS = [
    { id: 23, description: 'Sin atender' },
    { id: 24, description: 'En atención' },
    { id: 25, description: 'Resuelto' },
    { id: 26, description: 'Cancelado' },
    { id: 52, description: 'Rechazado' },
];

const statusFromClazz = (clazz) => {
    switch (Number(clazz)) {
        case 23: return 10;
        case 24: return 20;
        case 25: return 30;
        case 26: return 40;
        case 52: return 50;
        default: return undefined;
    }
};

export default function RequestGrid({ reasons = [], onShowHelper }) {
    const STATE_LOADING = 1;
    const STATE_LOADED = 2;
    const [noModulesModal, setNoModulesModal] = useState(false);

    const [isAdmin, setIsAdmin] = useState(false);
    const [state, setState] = useState(STATE_LOADING);
    const [view, setView] = useState('grid');
    const [filters, setFilters] = useState({
        criteria: '',
        reason: '',
        clazz: '',
        dateFrom: '',
        dateTo: '',
    });

    const [requests, setRequests] = useState([]);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(5);
    const [totalItems, setTotalItems] = useState(0);
    const [globalSummary, setGlobalSummary] = useState([]);
    const [summaryLoaded, setSummaryLoaded] = useState(false);
    const [boardLoading, setBoardLoading] = useState(false);

    const apiClient = ConfigurationBuilder.client;
    const navigate = useNavigate();

    const roles =
        useAppSelector(
            (s) => s.authentication?.tokenDecoded?.resource_access?.['fbc-aclaraciones']?.roles
        ) || [];

    const userEmail =
        useAppSelector(
            (s) =>
                s.authentication?.tokenDecoded?.email ||
                s.authentication?.tokenDecoded?.preferred_username
        ) || '';

    const canSwitchViews =
        Array.isArray(roles) &&
        (roles.includes('ppsomx-admin') || roles.includes('ppsomx-resolver'));

    const canDelete = canSwitchViews;

    const normalize = (arr) =>
        arr.map((x) => ({
            label: x?.label ?? x?.description ?? '',
            value: String(x?.value ?? x?.id ?? ''),
        }));

    const reasonOptions = normalize(reasons);
    const statusClazzOptions = normalize(RAW_STATUS_CLAZZ_OPTIONS);

    async function loadGlobalSummaryResolver() {
        try {
            setSummaryLoaded(false);
            if (!userEmail) return;
            const details = await apiClient.getResolverDetails(userEmail);
            if (!details?.length) return;
            const moduleIds = details.map(d => d.moduleId);
            const res = await apiClient.getRequestsByModules(moduleIds, {
                page: 1,
                size: 9999,
            });
            const mapped = res.data.map(req => ({ status: statusFromClazz(req.clazz) }));
            setGlobalSummary(mapped);
        } catch (err) {
            console.error('Error loading global summary (resolver)', err);
        } finally {
            setSummaryLoaded(true);
        }
    }

    async function loadGlobalSummaryProvider() {
        try {
            setSummaryLoaded(false);
            const res = await apiClient.getRequests({});
            const mapped = res.data.map(req => ({ status: statusFromClazz(req.clazz) }));
            setGlobalSummary(mapped);
        } catch (err) {
            console.error('Error loading global summary (provider)', err);
        } finally {
            setSummaryLoaded(true);
        }
    }

    useEffect(() => {
        if (canSwitchViews) loadGlobalSummaryResolver();
        else loadGlobalSummaryProvider();
    }, [canSwitchViews]);

    async function loadRequests() {
        try {
            const res = await apiClient.getRequests({
                criteria: filters.criteria?.trim() || undefined,
                dateFrom: filters.dateFrom || undefined,
                dateTo: filters.dateTo || undefined,
                reason: filters.reason || undefined,
                clazz: filters.clazz || undefined,
                page,
                size: perPage,
            });

            const mapped = res.data.map((req) => ({
                id: req.id,
                orderId: req.orderId,
                creationTime: translateDate(req.creationTime),
                reason: translateIdToString(req.reason, reasons),
                company: req.company,
                statusPill: buildStatusPill(req.clazz),
                clazz: req.clazz,
            }));

            setRequests(mapped);
            setTotalItems(res.total);

            setGlobalSummary(
                res.data.map(r => ({ status: statusFromClazz(r.clazz) }))
            );

            setState(STATE_LOADED);
        } catch (err) {
            console.error('Error loading provider requests', err);
        }
    }

    async function loadRequestsByResolver() {
        try {
            if (!userEmail) return;
            const details = await apiClient.getResolverDetails(userEmail);
            if (!details?.length) {
                setNoModulesModal(true);
                setState(STATE_LOADED);
                return;
            }

            const moduleIds = details.map(d => d.moduleId);
            const [modulesCatalog, reasonsCatalog] = await Promise.all([
                apiClient.getCatalog(3),
                apiClient.getCatalog(4),
            ]);

            const modulesMap = new Map(modulesCatalog.map((c) => [c.id, c.description]));
            const reasonsMap = new Map(reasonsCatalog.map((c) => [c.id, c.description]));

            const res = await apiClient.getRequestsByModules(moduleIds, {
                criteria: filters.criteria?.trim() || undefined,
                dateFrom: filters.dateFrom || undefined,
                dateTo: filters.dateTo || undefined,
                reason: filters.reason || undefined,
                clazz: filters.clazz || undefined,
                page,
                size: perPage,
            });

            const mapped = res.data.map((req) => ({
                id: req.id,
                orderId: req.orderId,
                creationTime: translateDate(req.creationTime),
                module: modulesMap.get(req.module) || '-',
                reason: reasonsMap.get(req.reason) || '-',
                company: req.company,
                responsible: req.responsible,
                statusPill: buildStatusPill(req.clazz),
                clazz: req.clazz,
            }));

            setRequests(mapped);
            setTotalItems(res.total);
            setGlobalSummary(mapped.map(r => ({ status: statusFromClazz(r.clazz) })));

            setState(STATE_LOADED);
        } catch (err) {
            console.error('Error loading resolver requests', err);
        }
    }

    useEffect(() => {
        (async () => {
            const flag = await ConfigurationBuilder.authenticator.isAdmin();
            setIsAdmin(flag);
        })();
    }, []);

    useEffect(() => {
        const handler = () => {
            console.log("📌 Recargando solicitudes por cambio de país…");

            setPage(1);
            setState(STATE_LOADING);

            if (canSwitchViews) loadGlobalSummaryResolver();
            else loadGlobalSummaryProvider();
        };

        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [canSwitchViews]);

    useEffect(() => {
        if (state === STATE_LOADING) {
            if (canSwitchViews) loadRequestsByResolver();
            else loadRequests();
        }
    }, [state, filters, canSwitchViews, page, perPage]);

    useEffect(() => {
        setPage(1);
    }, [filters]);

    useEffect(() => {
        if (view === 'board' && canSwitchViews) {
            setBoardLoading(true);
            (async () => {
                try {
                    const details = await apiClient.getResolverDetails(userEmail);
                    if (!details?.length) return;
                    const moduleIds = details.map(d => d.moduleId);
                    const res = await apiClient.getRequestsByModules(moduleIds, { page: 1, size: 9999 });

                    const mapped = res.data.map((req) => ({
                        id: req.id,
                        orderId: req.orderId,
                        creationTime: translateDate(req.creationTime),
                        module: req.module,
                        reason: req.reason,
                        company: req.company,
                        responsible: req.responsible,
                        clazz: req.clazz,
                    }));

                    setRequests(mapped);
                    setTotalItems(res.total);
                } catch (e) {
                    console.error('Error loading board data', e);
                } finally {
                    setBoardLoading(false);
                }
            })();
        }
    }, [view]);

    const handleDelete = async (id) => {
        setState(STATE_LOADING);
        try {
            await apiClient.deleteRequest(id);
            await loadRequests();
            setState(STATE_LOADED);
        } catch (error) {
            console.log(error);
        }
    };

    const totalPages = Math.max(1, Math.ceil(totalItems / perPage));
    const displayRows = requests;

    const baseColumns = [
        { header: 'N° Solicitud', render: (r) => <>{r.id}</> },
        { header: 'Orden', render: (r) => <>{r.orderId}</> },
        { header: 'Fecha', render: (r) => <>{r.creationTime}</> },
        { header: 'Categoria', render: (r) => <>{r.reason}</> },
        { header: 'Proveedor', render: (r) => <>{r.company}</> },
        { header: 'Estado', render: (r) => <span dangerouslySetInnerHTML={{ __html: r.statusPill }} /> },
    ];

    const adminColumns = [
        { header: 'Módulo', render: (r) => <>{r.module}</> },
        { header: 'Responsable', render: (r) => <>{r.responsible}</> },
    ];

    const columns = canSwitchViews
        ? [
            baseColumns[0],
            baseColumns[1],
            baseColumns[2],
            adminColumns[0],
            baseColumns[3],
            baseColumns[4],
            adminColumns[1],
            baseColumns[5],
        ]
        : baseColumns;

    const actions = [
        {
            title: 'Ver',
            icon: eyeIcon,
            onClick: (row) => onShowHelper?.(row.id),
        },
        ...(canDelete
            ? [
                {
                    title: 'Eliminar',
                    icon: deleteIcon,
                    onClick: (row) => handleDelete(row.id),
                },
            ]
            : []),
    ];

    const summaryList = useMemo(
        () =>
            (Array.isArray(requests) ? requests : [])
                .map((r) => ({ status: statusFromClazz(r?.clazz) }))
                .filter((x) => typeof x.status === 'number'),
        [requests]
    );

    const gridView = (
        <>
            <RequestFilters
                reasons={reasonOptions}
                statusClazzOptions={statusClazzOptions}
                filters={{
                    ...filters,
                    dateFrom: filters.dateFrom
                        ? new Date(filters.dateFrom + 'T00:00:00')
                        : null,
                    dateTo: filters.dateTo
                        ? new Date(filters.dateTo + 'T00:00:00')
                        : null,
                }}
                setFilters={setFilters}
                targetState={STATE_LOADING}
                targetStateFunction={setState}
            />

            <GenericTable
                rows={displayRows}
                columns={columns}
                actions={actions}
                emptyLabel="Sin resultados"
                perPage={perPage}
                page={page}
                totalPages={totalPages}
                onChangePerPage={(n) => {
                    setPerPage(n);
                    setPage(1);
                    setState(STATE_LOADING);
                }}
                onChangePage={(n) => {
                    setPage(n);
                    setState(STATE_LOADING);
                }}
                totalItems={totalItems}
            />
        </>
    );

    const handleShow = (id) => {
        onShowHelper?.(id);
    };

    const boardView = (
        <RequestBoard
            requests={requests}
            reasons={reasons}
            onShow={handleShow}
        />
    );

    if (state === STATE_LOADING || !summaryLoaded) {
        return (
            <div className="rg-loading-wrapper">
                <GenericLinearProgress indeterminate fullWidth />
                <div className="rg-loading-text">
                    Cargando solicitudes...
                </div>
            </div>
        );
    }

    return (
        <div className="page-wrapper">

            {roles.includes('ppsomx-vendor') ? (
                <div className="rg-block">
                    <h3 className="title" style={{ fontWeight: 600, marginBottom: '4px' }}>
                        Busca tus solicitudes
                    </h3>
                    <p className="subtitle" style={{ marginBottom: '20px' }}>
                        Para obtener mejores resultados, puedes buscar por N° de orden, folio, seleccionar un rango de fecha y filtrar por estado.
                    </p>
                </div>
            ) : (
                <div className="rg-block">
                    <h3 className="title">Mantenedor de solicitudes de casos</h3>
                    <p className="subtitle" style={{ marginBottom: '20px' }}>
                        Para obtener mejores resultados, puedes buscar por N° de orden, folio, seleccionar un rango de fecha y filtrar por estado.
                    </p>
                </div>
            )}

            {!roles.includes('ppsomx-vendor') && (
                <div className="rg-view-switch">
                    <GenericButton
                        variant={view === 'grid' ? 'primary' : 'outline'}
                        onClick={() => setView('grid')}
                        style={{ minWidth: 30 }}
                    >
                        Grilla
                    </GenericButton>

                    <GenericButton
                        variant={view === 'board' ? 'primary' : 'outline'}
                        onClick={() => {
                            setView('board');
                            setFilters({
                                criteria: '',
                                reason: '',
                                clazz: '',
                                dateFrom: '',
                                dateTo: '',
                            });
                            setRequests([]);
                            setGlobalSummary([]);
                            setState(STATE_LOADING);
                        }}
                        style={{ minWidth: 30 }}
                    >
                        Tablero
                    </GenericButton>
                </div>
            )}

            <div className="rg-summary">
                <RequestBoardSummary list={globalSummary} />
            </div>

            <div className="box">
                {view === 'grid' ? (
                    gridView
                ) : boardLoading ? (
                    <GenericLinearProgress indeterminate fullWidth />
                ) : (
                    boardView
                )}
            </div>

            <div className="rg-back-row">
                <GenericButton variant="link" onClick={() => navigate(-1)}>
                    Volver
                </GenericButton>
            </div>
            <GenericModal
                visible={noModulesModal}
                variant="alert"
                severity="warning"
                title="Sin módulos asignados"
                message="No es posible mostrar las solicitudes porque no tienes módulos asignados. Por favor, solicita a un administrador que te asigne un módulo."
                onClose={() => {
                    setNoModulesModal(false);
                    navigate(-1);
                }}
            />

        </div>
    );
}
