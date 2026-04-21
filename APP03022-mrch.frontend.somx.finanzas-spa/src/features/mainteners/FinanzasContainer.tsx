import { Link } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';

import iconDocument from '@assets/icons/document.svg';
import iconSupport from '@assets/icons/support.png';
import iconSetting from '@assets/icons/setting.png';
import iconReport from '@assets/icons/report.png';
import iconDoneCheck from '@assets/icons/done-check.png';
import iconWarning from '@assets/icons/warning.png';
import iconAudit from '@assets/icons/warning.png';

import './styles/FinanzasContainer.css';

interface FinanzasCard {
    title: string;
    description: string;
    link: string;
    icon?: any;
    disabled?: boolean;
}

export default function FinanzasContainer({ cards }: { cards?: FinanzasCard[] }) {
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
                                    {it.icon && <img src={it.icon} className="card-icon" alt={it.title} />}
                                    <div className="card-right">
                                        <h3 className="card-title">{it.title}</h3>
                                        <p className="card-desc">{it.description}</p>
                                    </div>
                                </div>
                            );

                            return it.disabled ? (
                                <div key={idx} className={cardClasses} title="Deshabilitado">
                                    {inner}
                                </div>
                            ) : (
                                <Link key={idx} to={it.link} className={cardClasses}>
                                    {inner}
                                </Link>
                            );
                        })}
                    </section>
                </section>
            </main>
        </div>
    );
}