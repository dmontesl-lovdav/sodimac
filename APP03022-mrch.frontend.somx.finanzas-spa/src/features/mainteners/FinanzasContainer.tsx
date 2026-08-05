import { Fragment, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { breadcrumbFinanceHomePage } from '@shared/components/ui/navigation/financeBreadcrumb';
import GenericModal from '@shared/components/ui/modal/GenericModal';

import iconDocument from '@assets/icons/document.svg';
import iconSupport from '@assets/icons/support.png';
import iconSetting from '@assets/icons/setting.png';
import iconReport from '@assets/icons/report.png';
import iconDoneCheck from '@assets/icons/done-check.png';
import iconWarning from '@assets/icons/warning.png';
import { getHealthcheck } from './api';
import { APP_KEYS, PermissionGate } from '@shared/security';

import './styles/FinanzasContainer.css';
import { syncFinanzasUser } from '@/services/finanzasUserSync';

interface FinanzasCard {
    title: string;
    description: string;
    link?: string;
    icon?: any;
    disabled?: boolean;
    onClick?: () => void;
    hidden?: boolean;
    requiredApp?: string;
    requiredAnyApp?: string[];
}

function cardKey(card: FinanzasCard): string {
    return card.link ?? card.title;
}

export default function FinanzasContainer({ cards }: { cards?: FinanzasCard[] }) {
    const [modalVisible, setModalVisible] = useState(false);
    const [modalVariant, setModalVariant] = useState<'loading' | 'alert'>('loading');
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');
    const [modalSeverity, setModalSeverity] = useState<'success' | 'error' | 'warning' | 'info'>('info');

    useEffect(() => {
        syncFinanzasUser();
    }, []);
    
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
                error?.response?.data?.message ??
                error?.message ?? 'No fue posible validar el estado del servicio.'
            );
        }
    }

    const DEFAULT_CARDS: FinanzasCard[] = [
        {
            title: 'Guías de embarque',
            description: 'Visualiza las guías de embarque registradas en el sistema.',
            link: '/finanzas/guias',
            icon: iconSupport,
            requiredApp: APP_KEYS.CARTA_PORTE,
        },
        {
            title: 'Lista de Recepciones',
            description: 'Consulta las recepciones de las órdenes de servicio y su estatus.',
            link: '/finanzas/recepciones',
            icon: iconDocument,
            requiredApp: APP_KEYS.RECEPTIONS,
        },
        {
            title: 'Descuentos comerciales',
            description: 'Consulta y gestiona los descuentos comerciales de la aplicación.',
            link: '/finanzas/descuentos-comerciales',
            icon: iconSetting,
            requiredApp: APP_KEYS.DISCOUNTS,
        },
        {
            title: 'Pagos',
            description: 'Consulta y gestiona los pagos de proveedores.',
            link: '/finanzas/pagos',
            icon: iconDoneCheck,
            requiredApp: APP_KEYS.PAYMENTS,
        },
        {
            title: 'Estado de cuenta',
            description: 'Consulta los estados de cuenta de proveedores y genera el documento en PDF.',
            link: '/finanzas/estado-cuenta',
            icon: iconReport,
            requiredApp: APP_KEYS.ACCOUNT_STATEMENT,
        },
        {
            title: 'Three Way Match',
            description: 'Consulta y validación de orden de compra, recepción y factura pendientes de pago o pagadas.',
            link: '/finanzas/three-way-match',
            icon: iconWarning,
            requiredApp: APP_KEYS.THREE_WAY_MATCH,
        },
        {
            title: 'Publicación de recepción MIGO',
            description: 'Consultar, publicar, autorizar o rechazar recepciones MIGO',
            link: '/finanzas/migo',
            icon: iconDocument,
            requiredApp: APP_KEYS.MIGO,
        },
        {
            title: 'Healthcheck',
            description: 'Valida si el servicio de Finanzas y la conexión a base de datos están activos.',
            icon: iconDoneCheck,
            onClick: () => {
                handleHealthcheck().catch(() => undefined);
            },
        },
    ];

    const finalCards = cards ?? DEFAULT_CARDS;

    return (
        <div className="finanzas-root">
            <Breadcrumb items={breadcrumbFinanceHomePage} />

            <main className="finanzas-main">
                <section className="finanzas-box">
                    <h1 className="maintainers-title">Gestión de recepciones, pagos y descuentos</h1>

                    <section className="cards-grid">
                        {finalCards.map((it) => {
                            const key = cardKey(it);
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

                            let element;
                            if (it.disabled) {
                                element = (
                                    <div className={cardClasses} title="Deshabilitado">
                                        {inner}
                                    </div>
                                );
                            } else if (it.onClick) {
                                element = (
                                    <button
                                        type="button"
                                        className={cardClasses}
                                        onClick={it.onClick}
                                        title={it.title}
                                    >
                                        {inner}
                                    </button>
                                );
                            } else {
                                element = (
                                    <Link
                                        to={(it.link ?? '') + '?reset=true'}
                                        className={cardClasses}
                                    >
                                        {inner}
                                    </Link>
                                );
                            }

                            if (it.requiredApp || it.requiredAnyApp) {
                                return (
                                    <PermissionGate
                                        key={key}
                                        app={it.requiredApp}
                                        anyApp={it.requiredAnyApp}
                                    >
                                        {element}
                                    </PermissionGate>
                                );
                            }

                            return <Fragment key={key}>{element}</Fragment>;
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
