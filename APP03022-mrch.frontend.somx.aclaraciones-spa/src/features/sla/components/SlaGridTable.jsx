import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import deleteIcon from '@assets/delete.svg';
import editIcon from '@assets/edit.svg';
import { GenericTable } from '@shared/components/ui';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function SlaGridTable(props) {
    const {
        rows, emptyLabel,
        perPage, page, totalPages,
        onChangePerPage, onChangePage,
        onEdit, onDelete, onTogglePublished,
    } = props;

    const nav = useNavigate();

    /* ---------- catalogs ---------- */
    const api = ConfigurationBuilder.client;

    const CATALOG_BUSINESSUNITS = 1;
    const CATALOG_COUNTRIES = 2;
    const CATALOG_MODULES = 3;
    const CATALOG_REASONS = 4;
    const CATALOG_PRIORITIES = 13;
    const CATALOG_FIRSTRESPONSELEVEL = 21;
    const CATALOG_RESOLUTIONLEVEL = 22;
    //
    const [businessUnits, setBusinessUnits] = useState([]); // [{value,label}]
    const [countries, setCountries] = useState([]); // [{value,label}]
    const [modules, setModules] = useState([]); // [{value,label}]
    const [reasons, setReasons] = useState([]); // [{value,label}]
    const [priorities, setPriorities] = useState([]); // [{value,label}]
    const [firstResponseLevels, setFirstResponseLevels] = useState([]); // [{value,label}]
    const [resolutionLevels, setResolutionLevels] = useState([]); // [{value,label}]

    useEffect(() => {
        (async () => {
            try {
                setBusinessUnits(await api.getCatalog(CATALOG_BUSINESSUNITS));
                setCountries(await api.getCatalog(CATALOG_COUNTRIES));
                setModules(await api.getCatalog(CATALOG_MODULES));
                setReasons(await api.getCatalog(CATALOG_REASONS));
                setPriorities(await api.getCatalog(CATALOG_PRIORITIES));
                setFirstResponseLevels(await api.getCatalog(CATALOG_FIRSTRESPONSELEVEL));
                setResolutionLevels(await api.getCatalog(CATALOG_RESOLUTIONLEVEL));
            } catch (err) {
                console.error('Could not load catalogs', err);
            }
        })();
    }, []);

    function resolveCatalogValue(catalogId, value) {
        let helper = [];
        switch (catalogId) {
            case CATALOG_BUSINESSUNITS: {
                helper = businessUnits;
                break;
            }
            case CATALOG_COUNTRIES: {
                helper = countries;
                break;
            }
            case CATALOG_FIRSTRESPONSELEVEL: {
                helper = firstResponseLevels;
                break;
            }
            case CATALOG_MODULES: {
                helper = modules;
                break;
            }
            case CATALOG_PRIORITIES: {
                helper = priorities;
                break;
            }
            case CATALOG_REASONS: {
                helper = reasons;
                break;
            }
            case CATALOG_RESOLUTIONLEVEL: {
                helper = resolutionLevels;
                break;
            }

        }
        return helper.filter(item => item.id === value)[0]?.description ?? "---";
    }


    /* ---- columnas ---- */
    const columns = [
        { header: 'Unidad de Negocio', render: (r) => resolveCatalogValue(CATALOG_BUSINESSUNITS, r.businessUnit) },
        { header: 'País', render: (r) => resolveCatalogValue(CATALOG_COUNTRIES, r.country) },
        { header: 'Módulo', render: (r) => resolveCatalogValue(CATALOG_MODULES, r.module) },
        { header: 'Motivo', render: (r) => resolveCatalogValue(CATALOG_REASONS, r.reason) },
        { header: 'priority', render: (r) => resolveCatalogValue(CATALOG_PRIORITIES, r.priority) },
        { header: 'Responder en', render: (r) => resolveCatalogValue(CATALOG_FIRSTRESPONSELEVEL, r.firstResponseLevel) },
        { header: 'Solucionar en', render: (r) => resolveCatalogValue(CATALOG_RESOLUTIONLEVEL, r.resolutionLevel) },
        { header: 'Correo', render: (r) => r.manager },
    ];

    /* ---- acciones ---- */
    const actions = [
        { title: 'Editar', icon: editIcon, onClick: (r) => (onEdit ? onEdit(r.id) : nav(`/slas/${r.id}`)) },
        { title: 'Eliminar', icon: deleteIcon, onClick: (r) => onDelete(r.id) },
    ];

    return (
        <GenericTable
            rows={rows}
            columns={columns}
            actions={actions}
            emptyLabel={emptyLabel}
            perPage={perPage}
            page={page}
            totalPages={totalPages}
            onChangePerPage={onChangePerPage}
            onChangePage={onChangePage}
        />
    );
}
