// src/features/cases/components/RequestDetail.jsx
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { requestStatus as REQUEST_STATUSES, translateDate, translateIdToString } from './RequestUtils';
import AttachmentSummary from '../utils/AttachmentSummary';
import HeaderSummary from './HeaderSummary';
import { GenericButton } from '@shared/components/ui';
import '../styles/RequestDetail.css';

export default function RequestDetail({ request, backCallback, businessUnits, modules, reasons, details }) {
    function buildBadge() {
        let status = REQUEST_STATUSES ?? [];
        status = status.filter((_status) => _status.id === request.status);
        status = status.length > 0 ? status[0] : { class: 'warn', description: '---' };

        const colorMap = {
            success: 'rd-badge-success',
            error: 'rd-badge-error',
            warn: 'rd-badge-warn',
            info: 'rd-badge-info',
        };
        const colorClass = colorMap[status.class] ?? 'rd-badge-gray';

        return (
            <span className={`rd-badge ${colorClass}`}>
                {status.description}
            </span>
        );
    }

    function buildHeader() {
        return (
            <HeaderSummary
                headers={[
                    { name: 'N° de OC:', value: request.orderId },
                    { name: 'Fecha de solicitud:', value: translateDate(request.creationTime) },
                    { name: 'Días transcurridos:', value: request.elapsedTime },
                    { name: 'Responsable:', value: request.operator },
                    { name: 'Estado', value: buildBadge() },
                ]}
            />
        );
    }

    function build() {
        return (
            <div>
                <div>
                    <div className="rd-title">
                        Detalle Solicitud {request.id}
                    </div>
                </div>

                <div>{buildHeader()}</div>

                <div className="rd-section-divider" />

                <div className="rd-container">
                    <div>
                        <span className="rd-label">Unidad de negocio:</span>{' '}
                        {translateIdToString(request.businessUnit, businessUnits)}
                    </div>

                    <div>
                        <span className="rd-label">Módulo:</span>{' '}
                        {translateIdToString(request.module, modules)}
                    </div>

                    <div>
                        <span className="rd-label">Motivo:</span>{' '}
                        {translateIdToString(request.reason, reasons)}
                    </div>

                    <div>
                        <span className="rd-label">Tipo:</span>{' '}
                        {translateIdToString(request.detail, details)}
                    </div>

                    <div>
                        <span className="rd-label">Descripción del caso:</span>{' '}
                        {request.description}
                    </div>

                    <div className="rd-attachments">
                        <AttachmentSummary requestId={request.id} attachments={request.attachments} />
                    </div>

                    <div className="rd-footer-row">
                        <GenericButton
                            variant="text"
                            className="rd-footer-link"
                            onClick={backCallback}
                        >
                            Volver
                        </GenericButton>
                    </div>
                </div>
            </div>
        );
    }

    return <div className="rd-wrapper">{build()}</div>;
}
