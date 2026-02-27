import { useMemo } from 'react';
import { GenericTable, Switch } from '@shared/components/ui';
import eyeIcon from '@assets/eye-show.svg';
import editIcon from '@assets/edit.svg';
import deleteIcon from '@assets/delete.svg';

import '../styles/RelatedInformationGridTable.css';

export default function RelatedInformationGridTable({
    rows,
    buOptions = [],
    countryOptions = [],
    emptyLabel,
    perPage,
    page,
    totalPages,
    onChangePerPage,
    onChangePage,
    onShow,
    onEdit,
    onDelete,
    onTogglePublished,
    loading,
}) {
    const buMap = useMemo(() => {
        const m = new Map();
        buOptions.forEach((o) => m.set(String(o.value), String(o.label)));
        return m;
    }, [buOptions]);

    const countryMap = useMemo(() => {
        const m = new Map();
        countryOptions.forEach((o) => m.set(String(o.value), String(o.label)));
        return m;
    }, [countryOptions]);

    const resolveBU = (r) =>
        buMap.get(String(r.businessUnitId)) ??
        r.businessUnitName ??
        String(r.businessUnitId ?? '');

    const resolveCountry = (r) =>
        countryMap.get(String(r.countryId)) ??
        r.countryName ??
        String(r.countryId ?? '');

    const columns = [
        { header: 'Título de la información relacionada', render: (r) => r.title },
        { header: 'Unidad de negocio', render: (r) => resolveBU(r) },
        { header: 'País', render: (r) => resolveCountry(r) },
        {
            header: 'Publicado',
            align: 'center',
            render: (r) => (
                <Switch
                    on={Boolean(r.isActive)}
                    onClick={() => onTogglePublished?.(r.id, Boolean(r.isActive))}
                />
            ),
        },
    ];

    const actions = [
        { title: 'Editar', icon: editIcon, onClick: (r) => onEdit?.(r.id) },
        { title: 'Eliminar', icon: deleteIcon, onClick: (r) => onDelete?.(r.id) },
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
            loading={loading}
        />
    );
}
