import GenericButton from '@shared/components/ui/button/GenericButton';
import { GenericTable } from '@shared/components/ui/table';
import type { CatalogDetailGridRow } from '../types';

interface Props {
  title: string;
  columns: { key: string; label: string }[];
  section: { items: CatalogDetailGridRow[]; total: number; page: number; pageSize: number };
  onPageChange: (page: number) => void;
  addLabel?: string;
  onAdd?: () => void;
  extraAction?: { label: string; onClick: () => void };
}

export function CatalogDetailSection({
  title,
  columns,
  section,
  onPageChange,
  addLabel,
  onAdd,
  extraAction,
}: Props) {
  const totalPages = Math.max(1, Math.ceil(section.total / section.pageSize));
  const tableColumns = columns.map((column) => ({
    header: column.label,
    render: (row: CatalogDetailGridRow) => String((row as unknown as Record<string, unknown>)[column.key] ?? ''),
  }));

  return (
    <section className="security-box user-catalog-detail__card user-catalog-detail__table-surface">
      <div className="user-catalog-detail__section-head">
        <h2 className="user-catalog-detail__section-title">{title}</h2>
        {onAdd && addLabel ? (
          <GenericButton type="button" variant="outlineFill" onClick={onAdd}>
            {addLabel}
          </GenericButton>
        ) : null}
        {extraAction ? (
          <GenericButton type="button" variant="outlineFill" onClick={extraAction.onClick}>
            {extraAction.label}
          </GenericButton>
        ) : null}
      </div>
      <div style={{ marginTop: '0.5rem' }}>
        <GenericTable
          rows={section.items}
          columns={tableColumns}
          emptyLabel="Sin registros en esta sección."
          perPage={section.pageSize}
          page={section.page}
          totalPages={totalPages}
          totalItems={section.total}
          onChangePage={onPageChange}
          onChangePerPage={() => {}}
          showPagination={section.total > 0}
          showPageSizeSelector={false}
        />
      </div>
    </section>
  );
}
