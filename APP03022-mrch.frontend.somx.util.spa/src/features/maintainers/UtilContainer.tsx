import { useState } from 'react';
import { Link } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';

import iconSetting from '@assets/icons/setting.png';
import iconSupport from '@assets/icons/support.png';
import iconAudit from '@assets/icons/warning.png';

import GenericModal from '@shared/components/ui/modal/GenericModal';
import { itemsService } from './services/itemsService';

import './styles/UtilContainer.css';

interface UtilCard {
    title: string;
    description: string;
    link?: string;
    icon?: string;
    disabled?: boolean;
    action?: 'healthCheck';
}

export default function UtilContainer({ cards }: { cards?: UtilCard[] }) {
    const [modalVisible, setModalVisible] = useState(false);
    const [modalVariant, setModalVariant] = useState<'loading' | 'alert' | 'confirm'>('alert');
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');
    const [modalSeverity, setModalSeverity] = useState<'success' | 'error' | 'info' | 'warning'>('info');

    const DEFAULT_CARDS: UtilCard[] = [
        {
            title: 'Configuración de Parámetros',
            description: 'Consulta y gestiona la configuración de parámetros del sistema.',
            link: '/util/parametros',
            icon: iconSetting,
        },
        {
            title: 'Catálogos',
            description: 'Consulta y administra los catálogos disponibles del sistema.',
            link: '/util/catalogos',
            icon: iconSupport,
        },
        {
            title: 'Auditoría',
            description: 'Consulta la bitácora de actividades, eventos y errores del sistema.',
            link: '/util/auditoria',
            icon: iconAudit,
            disabled: true,
        },
        {
            title: 'Seguridad',
            description: 'Consulta y gestiona la seguridad del sistema.',
            link: '/seguridad',
            icon: iconSetting
        },
        {
            title: 'Estado de conexión',
            description: 'Verifica si la conexión con el backend se encuentra disponible.',
            icon: iconSupport,
            action: 'healthCheck',
        },
    ];

    const finalCards = cards ?? DEFAULT_CARDS;

    const handleCloseModal = () => {
        setModalVisible(false);
        setModalTitle('');
        setModalMessage('');
        setModalVariant('alert');
        setModalSeverity('info');
    };

    const handleHealthCheck = async () => {
        setModalVisible(true);
        setModalVariant('loading');
        setModalSeverity('info');
        setModalTitle('');
        setModalMessage('Validando conexión con el backend...');

        const result = await itemsService.checkConnection();

        setModalVariant('alert');

        if (result.online) {
            setModalSeverity('success');
            setModalTitle('Servicio disponible');
            setModalMessage(result.message);
            return;
        }

        setModalSeverity('error');
        setModalTitle('Servicio no disponible');
        setModalMessage(result.message);
    };

    const handleCardClick = async (card: UtilCard) => {
        if (card.disabled) return;

        if (card.action === 'healthCheck') {
            await handleHealthCheck();
        }
    };

    return (
        <div className="util-root">
            <Breadcrumb
                items={[
                    { label: 'Inicio', to: '/' },
                    { label: 'Herramientas y Utilerías' },
                ]}
            />

            <main className="util-main">
                <section className="util-box">
                    <h1 className="maintainers-title">Operaciones</h1>

                    <section className="cards-grid">
                        {finalCards.map((it, idx) => {
                            const cardClasses = ['card', it.disabled ? 'disabled' : ''].join(' ').trim();

                            const inner = (
                                <div className="card-content">
                                    {it.icon && <img src={it.icon} className="card-icon" alt={it.title} />}
                                    <div className="card-right">
                                        <h3 className="card-title">{it.title}</h3>
                                        <p className="card-desc">{it.description}</p>
                                    </div>
                                </div>
                            );

                            if (it.disabled) {
                                return (
                                    <div key={idx} className={cardClasses} title="Deshabilitado">
                                        {inner}
                                    </div>
                                );
                            }

                            if (it.action) {
                                return (
                                    <div
                                        key={idx}
                                        className={cardClasses}
                                        onClick={() => handleCardClick(it)}
                                        onKeyDown={(e) => {
                                            if (e.key === 'Enter' || e.key === ' ') {
                                                handleCardClick(it);
                                            }
                                        }}
                                        role="button"
                                        tabIndex={0}
                                    >
                                        {inner}
                                    </div>
                                );
                            }

                            return (
                                <Link key={idx} to={it.link || '#'} className={cardClasses}>
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
                title={modalTitle}
                message={modalMessage}
                severity={modalSeverity}
                buttonText="Aceptar"
                onClose={handleCloseModal}
            />
        </div>
    );
}