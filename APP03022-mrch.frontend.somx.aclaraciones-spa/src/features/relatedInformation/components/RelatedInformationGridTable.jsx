import { GenericTable, Switch } from '@shared/components/ui';
import editIcon from '@assets/edit.svg';
import deleteIcon from '@assets/delete.svg';

import '../styles/RelatedInformationGridTable.css';

export default function RelatedInformationGridTable({
    rows,
    emptyLabel,
    perPage,
    page,
    totalPages,
    onChangePerPage,
    onChangePage,
    onEdit,
    onDelete,
    onTogglePublished,
    loading,
}) {
    const columns = [
        { header: 'Título de la información relacionada', render: (r) => r.title },
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
