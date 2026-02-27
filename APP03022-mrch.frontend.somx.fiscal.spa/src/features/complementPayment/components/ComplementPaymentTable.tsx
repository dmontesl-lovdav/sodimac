import { GenericTable } from '@shared/components/ui';
import pdfIcon from '@assets/pdf.svg';
import xmlIcon from '@assets/xml.svg';
import viewIcon from '@assets/eye-show.svg';
import { useNavigate } from 'react-router-dom';

interface ComplementPayment {
  paymentsUuid: string;
  fiscalUuid: string;
  series: string;
  folio: string;
  subtotal: number;
  totalAmount: number;
  issuerRfc: string;
  issuerName: string;
  receiverRfc: string;
  receiverName: string;
  createdAt: string;
  paymentDate: string;
  statusDescription: string;
  relatedDocumentsCount?: number;
}

interface ComplementPaymentTableProps {
  rows: ComplementPayment[];
  page: number;
  perPage: number;
  totalPages: number;
  totalItems: number;
  loading: boolean;
  onChangePage: (page: number) => void;
  onChangePerPage: (n: number) => void;
  onView: (row: ComplementPayment) => void;
  onDownloadPDF: (row: ComplementPayment) => void;
  onDownloadXML: (row: ComplementPayment) => void;
  enableSelection?: boolean;
  selectedIds?: string[];
  onSelectRow?: (id: string, checked: boolean) => void;
}

const styles = {
  uuid: {
    fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
    fontSize: '0.75rem',
    wordBreak: 'break-all' as const,
  },
};

export default function ComplementPaymentTable({
  rows,
  page,
  perPage,
  totalPages,
  totalItems,
  loading,
  onChangePage,
  onChangePerPage,
  onView,
  onDownloadPDF,
  onDownloadXML,
  enableSelection = false,
  selectedIds = [],
  onSelectRow = () => { },
}: ComplementPaymentTableProps) {
  const navigate = useNavigate();

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    if (!dateString.includes('T')) return dateString;
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
        date.getDate()
      ).padStart(2, '0')}`;
      const formattedTime = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(
        2,
        '0'
      )}:${String(date.getSeconds()).padStart(2, '0')}`;
      return `${formattedDate} ${formattedTime}`;
    } catch {
      return dateString;
    }
  };

  const columns = [
    { header: 'Serie', render: (r: ComplementPayment) => r.series || '-' },
    { header: 'Folio', render: (r: ComplementPayment) => r.folio || '-' },
    { header: 'Fecha Emisión', render: (r: ComplementPayment) => formatDate(r.createdAt) },
    { header: 'RFC Emisor', render: (r: ComplementPayment) => r.issuerRfc || '-' },
    { header: 'Nombre Emisor', render: (r: ComplementPayment) => r.issuerName || '-' },
    { header: 'RFC Receptor', render: (r: ComplementPayment) => r.receiverRfc || '-' },
    { header: 'Nombre Receptor', render: (r: ComplementPayment) => r.receiverName || '-' },
    { header: 'Subtotal', render: (r: ComplementPayment) => r.subtotal?.toFixed(2) ?? '0.00' },
    { header: 'Total', render: (r: ComplementPayment) => r.totalAmount?.toFixed(2) ?? '0.00' },
    { header: 'Fecha Pago', render: (r: ComplementPayment) => formatDate(r.paymentDate) },
    { header: 'Estatus', render: (r: ComplementPayment) => r.statusDescription || '-' },
    { header: 'Relaciones', render: (r: ComplementPayment) => r.relatedDocumentsCount ?? 0 },
    {
      header: 'UUID',
      render: (r: ComplementPayment) => <span style={styles.uuid}>{r.paymentsUuid}</span>,
    },
  ];

  const actions = [
    {
      title: 'Ver facturas',
      icon: viewIcon,
      onClick: (r: ComplementPayment) => navigate(`/fiscal/facturas/${r.paymentsUuid}`),
    },
    { title: 'Descargar XML', icon: xmlIcon, onClick: (r: ComplementPayment) => onDownloadXML(r) },
    { title: 'Descargar PDF', icon: pdfIcon, onClick: (r: ComplementPayment) => onDownloadPDF(r) },
  ];

  const handleSelect = (rowOrId: any, checked: boolean) => {
    const id = typeof rowOrId === 'string' ? rowOrId : rowOrId?.paymentsUuid ?? rowOrId?.id ?? rowOrId?.uuid;
    if (!id) return;
    onSelectRow(id, checked);
  };

  return (
    <GenericTable
      rows={rows}
      columns={columns}
      actions={actions}
      loading={loading}
      page={page}
      perPage={perPage}
      totalPages={totalPages}
      totalItems={totalItems}
      onChangePage={onChangePage}
      onChangePerPage={onChangePerPage}
      emptyLabel="Sin resultados con los criterios establecidos (INF6000)"
      enableSelection={enableSelection}
      selectedIds={selectedIds}
      rowIdKey="paymentsUuid"
      onSelectRow={handleSelect}
    />
  );
}
