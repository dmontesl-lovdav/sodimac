import editIcon from '@assets/edit.svg';
import deleteIcon from '@assets/delete.svg';
import { GenericTable, Switch } from '@shared/components/ui';

export default function NoticeGridTable({
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
}) {

    /* -------- columnas -------- */
    const columns = [
        {
            header: 'Título de la información',
            align: 'left',
            render: (row) => row.name,
        },
        {
            header: 'Publicado',
            align: 'center',
            render: (row) => (
                <Switch
                    on={row.published}
                    onClick={() => onTogglePublished(row.id, row.published)}
                />
            ),
        },
    ];

    /* -------- acciones -------- */
    const actions = [
        {
            title: 'Editar',
            icon: editIcon,
            onClick: (row, nav) =>
                onEdit ? onEdit(row) : nav(`/notices/${row.id}`),
        },
        {
            title: 'Eliminar',
            icon: deleteIcon,
            onClick: (row) => onDelete(row.id),
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

            enableSelection={false}
        />
    );
}
