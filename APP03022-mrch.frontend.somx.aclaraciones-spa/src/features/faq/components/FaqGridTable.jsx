// src/features/faq/components/FaqGridTable.jsx
import { GenericTable, Switch } from '@shared/components/ui';
import eyeIcon from '@assets/eye-show.svg';
import editIcon from '@assets/edit.svg';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '@/store/hooks/useAppSelector';
import { shallowEqual } from 'react-redux';
import { useEffect, useRef } from 'react';

export default function FaqGridTable(props) {
    const {
        rows,
        categories,
        emptyLabel,
        perPage,
        page,
        totalPages,
        onChangePerPage,
        onChangePage,
        onShow,
        onEdit,
        onTogglePublished,

        // nuevas props
        enableSelection = false,
        selectedIds = [],
        onSelectRow = () => { },
        onSelectAll = () => { },
    } = props;

    const nav = useNavigate();
    const categoryOf = (id) => categories.find((c) => c.id === id);

    const roles = useAppSelector(
        (s) => s.authentication?.tokenDecoded?.realm_access?.roles ?? [],
        shallowEqual
    );

    const didLogRef = useRef(false);
    useEffect(() => {
        if (!didLogRef.current) {
            console.log('Global roles of current user:', roles);
            didLogRef.current = true;
        }
    }, [roles]);

    /* ---- columnas ---- */
    const columns = [
        { header: 'Pregunta', render: (r) => r.question },
        {
            header: 'Módulo',
            render: (r) =>
                r.moduleName ||
                r.module ||
                categoryOf(r.categoryId)?.label ||
                '—',
        },
        {
            header: 'Categoría',
            render: (r) =>
                r.categoryName ||
                categoryOf(r.categoryId)?.description ||
                '—',
        },
        {
            header: 'Publicado',
            align: 'center',
            render: (r) => (
                <Switch
                    on={r.published}
                    onClick={() => onTogglePublished(r.id, r.published)}
                />
            ),
        },
    ];

    const actions = [
        {
            title: 'Editar',
            icon: editIcon,
            onClick: (r) =>
                onEdit ? onEdit(r.id) : nav(`/faq/${r.id}/edit`),
        },
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
            enableSelection={enableSelection}
            selectedIds={selectedIds}
            onSelectRow={onSelectRow}
            onSelectAll={onSelectAll}
        />
    );
}
