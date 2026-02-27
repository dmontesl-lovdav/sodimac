import checkIcon from '@assets/RequestConfirmIcon.svg';
import { useNavigate } from 'react-router-dom';
import '../styles/RequestConfirm.css';
import { GenericButton } from '@shared/components/ui';
import { translateIdToString } from './RequestUtils';

export default function RequestConfirm({ form, backCallback, businessUnits, modules, reasons, details }) {
    const navigate = useNavigate();
    const SLA_IN_DAYS = 7;
    const ticket = form?.ticket ?? '---';

    return (
        <div className="rc-wrapper">

            <div className="rc-header-row">
                <div className="rc-check-container">
                    <img src={checkIcon} className="rc-check-icon" alt="check" />
                </div>

                <div className="rc-title">
                    Solicitud {ticket} creada con éxito
                </div>
            </div>

            <div className="rc-paragraph">
                Tu solicitud de creación de caso se ha enviado.
                Te responderemos en un máximo de {SLA_IN_DAYS} días.
                <p />
                Para consultar el estado de tu solicitud, accede a la sección
                <strong> "Mis Solicitudes" </strong>
                e ingresa el número de seguimiento.
            </div>

            <div className="rc-section-title">Resumen de tu caso</div>

            <div className="rc-field"><span>Unidad de negocio:</span> {translateIdToString(form?.businessUnit, businessUnits)}</div>
            <div className="rc-field"><span>Módulo:</span> {translateIdToString(form?.module, modules)}</div>
            <div className="rc-field"><span>Motivo:</span> {translateIdToString(form?.reason, reasons)}</div>
            <div className="rc-field"><span>Tipo:</span> {translateIdToString(form?.detail, details)}</div>
            <div className="rc-field"><span>Descripción del caso:</span> {form?.description ?? '---'}</div>

            <div className="rc-ticket-row">
                <span className="rc-ticket-label">Tu número de solicitud es:</span>
                <div className="rc-ticket-value">{ticket}</div>
            </div>

            <div className="rc-buttons">
                <GenericButton
                    variant="text"
                    className="rc-btn-back"
                    onClick={backCallback}
                >
                    Volver
                </GenericButton>

                <GenericButton
                    className="rc-btn-help"
                    onClick={() => navigate('/')}
                >
                    Ir al Centro de Ayuda
                </GenericButton>
            </div>

        </div>
    );
}
