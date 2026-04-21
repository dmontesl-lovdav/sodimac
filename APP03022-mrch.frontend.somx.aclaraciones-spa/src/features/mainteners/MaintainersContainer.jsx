import './styles/MaintainersContainer.css';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { useAppSelector } from '@/store/hooks/useAppSelector';

import iconAlertUp from '@assets/icons/alert-up.png';
import iconChat from '@assets/icons/chat.png';
import iconDoneCheck from '@assets/icons/done-check.png';
import iconEye from '@assets/icons/Group.png';
import iconSetting from '@assets/icons/setting.png';
import iconSupport from '@assets/icons/support.png';
import iconWarning from '@assets/icons/warning.png';

export default function MaintainersContainer() {
    const [showAlert, setShowAlert] = useState(true);
    const [myCasesCount, setMyCasesCount] = useState(null);
    const [casesLoading, setCasesLoading] = useState(true);

    const realmRoles =
        useAppSelector(
            (s) => s.authentication?.tokenDecoded?.realm_access?.roles
        ) || [];

    const isTechAdmin =
        Array.isArray(realmRoles) &&
        realmRoles.includes('FBC_TECH_ADMIN_USER');

    const roles =
        useAppSelector(
            (s) => s.authentication?.tokenDecoded?.resource_access?.['fbc-aclaraciones']?.roles
        ) || [];

    const userEmail =
        useAppSelector(
            (s) =>
                s.authentication?.tokenDecoded?.email ||
                s.authentication?.tokenDecoded?.preferred_username
        ) || '';

    const api = ConfigurationBuilder.client;

    const loadMyCases = useCallback(async () => {
        try {
            setCasesLoading(true);
            const api = ConfigurationBuilder.client;
            let cases = [];

            if (isTechAdmin ||
                roles.includes('ppsomx-admin') ||
                roles.includes('ppsomx-resolver')) {
                const details = await api.getResolverDetails(userEmail);

                if (details?.length) {
                    const moduleIds = details.map(d => d.moduleId);
                    const res = await api.getRequestsByModules(moduleIds, {
                        page: 1,
                        size: 9999
                    });
                    cases = Array.isArray(res?.data) ? res.data : [];
                }
            } else {
                const res = await api.getRequests({});
                cases = Array.isArray(res?.data) ? res.data : [];
            }

            setMyCasesCount(cases.length);
        } catch {
            setMyCasesCount(0);
        } finally {
            setCasesLoading(false);
        }
    }, [isTechAdmin, roles, userEmail]);

    useEffect(() => {
        loadMyCases();
    }, [loadMyCases]);

    useEffect(() => {
        const handler = () => {
            loadMyCases();
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, [loadMyCases]);

    const items = [
        {
            url: '/cases',
            key: 'mis-casos',
            title: 'Mis casos',
            desc: 'Revisa tus casos pendientes de atender y los casos resueltos.',
            accent: true,
            icon: iconSupport,
        },
        {
            url: '/faqs',
            key: 'faqs',
            title: 'Manuales y tutoriales (Preguntas Frecuentes)',
            desc: 'Administra las preguntas frecuentes que el proveedor podrá visualizar.',
            icon: iconChat,
        },
        {
            url: '/categories',
            key: 'temas',
            title: 'Categorías',
            desc: 'Configura las categorías sobre las cuales se asociarán preguntas frecuentes.',
            icon: iconAlertUp,
        },
        {
            url: '/relatedInformation',
            key: 'relacionada',
            title: 'Información relacionada',
            desc: 'Configura y vincula información en relación a las preguntas frecuentes.',
            icon: iconEye,
        },
        // Dont delete this functionality will be implemented in version 2 
        // {
        //     url: '/notices',
        //     key: 'seccion',
        //     title: 'Sección informativa',
        //     desc: 'Define información para banners de alerta.',
        //     icon: iconWarning,
        //     disabled: true,
        // },
        {
            url: '/feedback',
            key: 'feedback',
            title: 'Feedback',
            desc: 'Configura la opción de feedback.',
            icon: iconSupport,
        },
        {
            url: '/slas',
            key: 'sla',
            title: 'SLA',
            desc: 'Configuración de tiempos de respuesta.',
            icon: iconDoneCheck,
        },
        {
            url: '/moduleResolver',
            key: 'module-resolver',
            title: 'Resolutores por módulo',
            desc: 'Administra los resolutores asignados a cada módulo.',
            icon: iconSetting,
        },
    ];

    return (
        <div className="maintainers-root">
            <Breadcrumb
                items={[
                    { label: 'Inicio', to: '/' },
                    { label: 'Centro de ayuda', to: '/' },
                    { label: 'Mantenedor' },
                ]}
            />

            <main className="maintainers-main">
                {/* Dont delete this functionality will be implemented in version 2 */}

                {/* {showAlert && (
                    <section className="alert-box">
                        <div className="alert-content">
                            <div className="alert-left">
                                <svg
                                    viewBox="0 0 24 24"
                                    className="alert-icon"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                >
                                    <path d="M12 9v4m0 4h.01" />
                                    <path d="M10.29 3.86l-8.08 14.02A1.5 1.5 0 003.5 20h17a1.5 1.5 0 001.29-2.12L13.71 3.86a1.5 1.5 0 00-2.42 0z" />
                                </svg>

                                <p>
                                    <strong>Información importante: </strong>
                                    El portal Falabella Business Center estará en mantención el domingo 16 de marzo,
                                    entre 00:00 y 02:00 hrs (GMT-4).
                                </p>
                            </div>

                            <button
                                className="alert-close"
                                aria-label="Cerrar"
                                onClick={() => setShowAlert(false)}
                            >
                                ×
                            </button>
                        </div>
                    </section>
                )} */}

                <div className="divider" />

                <h1 className="maintainers-title">Mantenedores</h1>

                <section className="cards-grid">
                    {items.map((it) => {
                        const isMyCases = it.key === 'mis-casos';

                        const cardClasses = [
                            'card',
                            it.accent ? 'accent' : '',
                            it.disabled ? 'disabled' : '',
                        ].join(' ');

                        const inner = (
                            <div className="card-content">
                                <img src={it.icon} className="card-icon" alt={it.title} />

                                <div className="card-right">
                                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                        <h3 className="card-title">{it.title}</h3>

                                        {isMyCases && casesLoading && (
                                            <span className="badge-loading">
                                                <span className="badge-spinner" />
                                            </span>
                                        )}

                                        {isMyCases && !casesLoading && myCasesCount > 0 && (
                                            <span className="badge-count">{myCasesCount}</span>
                                        )}
                                    </div>

                                    <p className="card-desc">{it.desc}</p>
                                </div>
                            </div>
                        );

                        return it.disabled ? (
                            <div key={it.key} className={cardClasses} title="Deshabilitado">
                                {inner}
                            </div>
                        ) : (
                            <Link key={it.key} to={`${it.url}?from=mantenedor`} className={cardClasses}>
                                {inner}
                            </Link>
                        );
                    })}
                </section>
            </main>
        </div>
    );
}
