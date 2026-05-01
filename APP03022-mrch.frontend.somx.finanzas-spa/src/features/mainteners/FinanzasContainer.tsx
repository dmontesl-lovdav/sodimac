import { useState } from 'react';
import { Link } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import GenericModal from '@shared/components/ui/modal/GenericModal';

import iconDocument from '@assets/icons/document.svg';
import iconSupport from '@assets/icons/support.png';
import iconSetting from '@assets/icons/setting.png';
import iconReport from '@assets/icons/report.png';
import iconDoneCheck from '@assets/icons/done-check.png';
import iconWarning from '@assets/icons/warning.png';
import iconAudit from '@assets/icons/warning.png';

import { getHealthcheck } from './api';

import './styles/FinanzasContainer.css';

interface FinanzasCard {
    title: string;
    description: string;
    link?: string;
    icon?: any;
    disabled?: boolean;
    onClick?: () => void;
}

export default function FinanzasContainer({ cards }: { cards?: FinanzasCard[] }) {
    const [modalVisible, setModalVisible] = useState(false);
    const [modalVariant, setModalVariant] = useState<'loading' | 'alert'>('loading');
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');
    const [modalSeverity, setModalSeverity] = useState<'success' | 'error' | 'warning' | 'info'>('info');

    async function handleHealthcheck() {
        try {
            setModalVariant('loading');
            setModalMessage('Validando conexión con el servicio...');
            setModalVisible(true);

            const response = await getHealthcheck();
            const firstRecord = response.data?.[0];

            setModalVariant('alert');
            setModalSeverity(response.alive ? 'success' : 'warning');
            setModalTitle(response.alive ? 'Servicio activo' : 'Servicio sin respuesta válida');
            setModalMessage(
                response.alive
                    ? `Healthcheck exitoso. Servicio: ${firstRecord?.serviceName ?? 'N/A'} | Status: ${firstRecord?.status ?? 'N/A'} | Registros: ${response.count}`
                    : 'El servicio respondió, pero no confirmó estado activo.'
            );
        } catch (error: any) {
            setModalVariant('alert');
            setModalSeverity('error');
            setModalTitle('Error en healthcheck');
            setModalMessage(
                error?.response?.data?.message ||
                error?.message ||
                'No fue posible validar el estado del servicio.'
            );
        }
    }

    const DEFAULT_CARDS: FinanzasCard[] = [
        {
            title: 'Guías de embarque',
            description: 'Visualiza las guías de embarque registradas en el sistema.',
            link: '/finanzas/guias',
            icon: iconSupport,
        },
        {
            title: 'Lista de Recepciones',
            description: 'Consulta las recepciones de las órdenes de servicio y su estatus.',
            link: '/finanzas/recepciones',
            icon: iconDocument,
        },
        {
            title: 'Descuentos comerciales',
            description: 'Consulta y gestiona los descuentos comerciales de la aplicación.',
            link: '/finanzas/descuentos-comerciales',
            icon: iconSetting,
        },
        {
            title: 'Pagos',
            description: 'Consulta y gestiona los pagos de proveedores.',
            link: '/finanzas/pagos',
            icon: iconDoneCheck,
        },
        {
            title: 'Estado de cuenta',
            description: 'Consulta los estados de cuenta de proveedores y genera el documento en PDF.',
            link: '/finanzas/estado-cuenta',
            icon: iconReport,
        },
        {
            title: 'Three Way Match',
            description: 'Consulta y validación de orden de compra, recepción y factura pendientes de pago o pagadas.',
            link: '/finanzas/three-way-match',
            icon: iconWarning,
        },
        {
            title: 'Auditoría',
            description: 'Consulta la bitácora de actividades, eventos y errores del sistema.',
            link: '/auditoria/bitacora-actividades',
            icon: iconAudit,
        },
        {
            title: 'Publicación de recepción MIGO',
            description: 'Consultar, publicar, autorizar o rechazar recepciones MIGO',
            link: '/finanzas/migo',
            icon: iconDocument,
        },
        {
            title: 'Healthcheck',
            description: 'Valida si el servicio de Finanzas y la conexión a base de datos están activos.',
            icon: iconDoneCheck,
            onClick: handleHealthcheck,
        },
    ];

    const finalCards = cards ?? DEFAULT_CARDS;

    return (
        <div className="finanzas-root">
            <Breadcrumb
                items={[
                    { label: 'Inicio', to: '/' },
                    { label: 'Finanzas' },
                ]}
            />

            <main className="finanzas-main">
                <section className="finanzas-box">
                    <h1 className="maintainers-title">Operaciones</h1>

                    <section className="cards-grid">
                        {finalCards.map((it, idx) => {
                            const cardClasses = ['card', it.disabled ? 'disabled' : ''].join(' ').trim();

                            const inner = (
                                <div className="card-content">
                                    {it.icon && (
                                        <img
                                            src={it.icon}
                                            className="card-icon"
                                            alt={it.title}
                                        />
                                    )}

                                    <div className="card-right">
                                        <h3 className="card-title">{it.title}</h3>
                                        <p className="card-desc">{it.description}</p>
                                    </div>
                                </div>
                            );

                            if (it.disabled) {
                                return (
                                    <div
                                        key={idx}
                                        className={cardClasses}
                                        title="Deshabilitado"
                                    >
                                        {inner}
                                    </div>
                                );
                            }

                            if (it.onClick) {
                                return (
                                    <button
                                        key={idx}
                                        type="button"
                                        className={cardClasses}
                                        onClick={it.onClick}
                                        title={it.title}
                                    >
                                        {inner}
                                    </button>
                                );
                            }

                            return (
                                <Link
                                    key={idx}
                                    to={it.link ?? '#'}
                                    className={cardClasses}
                                >
                                    {inner}
                                </Link>
                            );
                        })}
                    </section>
                </section>
            </main>

            <GenericModal
                visible={modalVisible}
                variant={modalVariant}
                severity={modalSeverity}
                title={modalTitle}
                message={modalMessage}
                buttonText="Aceptar"
                onClose={() => setModalVisible(false)}
            />
        </div>
    );
}