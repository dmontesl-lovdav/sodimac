import { GenericTable } from '@shared/components/ui';
import editIcon from '@assets/edit.svg';
import deleteIcon from '@assets/delete.svg';

export default function ModuleResolverGridTable({
    rows,
    perPage,
    page,
    totalPages,
    totalItems,
    onChangePerPage,
    onChangePage,
    onEdit,
    onDelete,
    loading,
}) {
    const sortedRows = [...rows].sort((a, b) =>
        a.moduleName.localeCompare(b.moduleName, 'es')
    );

    const columns = [
        { header: 'Módulo', render: (r) => r.moduleName },
        { header: 'Área', render: (r) => r.area },
        { header: 'Persona', render: (r) => r.personName },
        { header: 'Email', render: (r) => r.resolverEmail },
    ];

    const actions = [
        { title: 'Editar', icon: editIcon, onClick: (r) => onEdit?.(r.id) },
        { title: 'Eliminar', icon: deleteIcon, onClick: (r) => onDelete?.(r.id) },
    ];

    return (
        <GenericTable
            rows={sortedRows}
            columns={columns}
            actions={actions}
            emptyLabel="Sin resolutores encontrados"
            perPage={perPage}
            page={page}
            totalPages={totalPages}
            totalItems={totalItems}
            onChangePerPage={onChangePerPage}
            onChangePage={onChangePage}
            loading={loading}
        />
    );
}
