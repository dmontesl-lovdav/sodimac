import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import categoryImg from '@assets/category.png';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

import './styles/ActionsSection.css';

export default function ActionsSection() {
    const [myCasesCount, setMyCasesCount] = useState(0);
    const [casesLoading, setCasesLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        (async () => {
            try {
                const api = ConfigurationBuilder.client;
                const res = await api.getRequests({});

                const count =
                    res?.total ??
                    (Array.isArray(res?.data) ? res.data.length : 0);

                if (mounted) setMyCasesCount(count);
            } catch {
                if (mounted) setMyCasesCount(0);
            } finally {
                if (mounted) setCasesLoading(false);
            }
        })();

        return () => {
            mounted = false;
        };
    }, []);

    return (
        <section className="hca-section">
            <h3 className="hca-title">Cuéntanos, ¿Qué necesitas?</h3>

            <div className="hca-grid">
                {/* Manuales */}
                <div className="hca-card hca-card-manual">
                    <div className="hca-card-manual-text">
                        <h4 className="hca-card-title">Manuales y tutoriales</h4>
                        <p className="hca-card-desc">
                            Revisa y descarga los manuales y controles de los módulos de FBC.
                        </p>
                    </div>
                    <Link to="/help-center/resources" className="hca-link-inline">
                        <button type="button" className="hca-btn-outline">
                            Ver manuales
                        </button>
                    </Link>
                    <img src={categoryImg} alt="" className="hca-card-manual-img" />
                </div>

                {/* Casos */}
                <div className="hca-card hca-card-cases">
                    <div>
                        <h4 className="hca-card-title hca-card-title-row">
                            Consulta tus casos creados
                            {casesLoading ? (
                                <span className="hca-spinner" />
                            ) : (
                                myCasesCount > 0 && (
                                    <span className="hca-badge">{myCasesCount}</span>
                                )
                            )}
                        </h4>
                        <p className="hca-card-desc">
                            Consulta fácilmente el estado de tus solicitudes ingresadas y mantente
                            al tanto de su progreso.
                        </p>
                    </div>
                    <Link to="/cases" className="hca-link-inline">
                        <button type="button" className="hca-btn-outline">
                            Consultar estado de caso
                        </button>
                    </Link>
                </div>

                {/* Crear caso */}
                <div className="hca-card hca-card-new">
                    <div>
                        <h4 className="hca-card-title">Crear un caso</h4>
                        <p className="hca-card-desc">
                            Para poder ayudarte, ingresa una solicitud o requerimiento y cuéntanos
                            qué problema tuviste.
                        </p>
                    </div>
                    <Link to="/cases/new" className="hca-link-full">
                        <button type="button" className="hca-btn-primary">
                            Quiero crear un caso
                        </button>
                    </Link>
                </div>
            </div>
        </section>
    );
}
