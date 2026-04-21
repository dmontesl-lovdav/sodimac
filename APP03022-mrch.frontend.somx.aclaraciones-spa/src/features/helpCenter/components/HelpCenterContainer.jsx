import { useState } from 'react';
import { Link } from 'react-router-dom';
import worldImg from '@assets/world.png';
import cloudImg from '@assets/cloud.png';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';

import FeaturedCategories from './FeaturedCategories';
import AllCategories from './AllCategories';
import ActionsSection from './ActionsSection';

import './styles/HelpCenterContainer.css';

export default function HelpCenterContainer() {
    const [showAlert, setShowAlert] = useState(true);
    const devTestingRoutes = /^(localhost|127(?:\.\d+){3}|0\.0\.0\.0)$/i.test(
        window.location.hostname
    );

    return (
        <div className="hcc-root">
            <Breadcrumb items={[{ label: 'Inicio', to: '/' }, { label: 'Centro de ayuda' }]} />

            <div className="hcc-content">
                {/* ===== Header ===== */}
                <section className="hcc-hero">
                    <img src={worldImg} alt="" className="hcc-hero-world" />
                    <img src={cloudImg} alt="" className="hcc-hero-cloud hcc-hero-cloud-1" />
                    <img src={cloudImg} alt="" className="hcc-hero-cloud hcc-hero-cloud-2" />

                    <div className="hcc-hero-inner">
                        <div className="hcc-hero-text">
                            <h2 className="hcc-hero-subtitle">Conoce nuestro</h2>
                            <h1 className="hcc-hero-title">Centro de ayuda</h1>
                            <p className="hcc-hero-description">
                                Utiliza el buscador o explora las categorías para resolver tus
                                inquietudes. También puedes crear un caso o verificar su estado.
                            </p>
                        </div>
                    </div>
                </section>

                {/* Alert */}
                {/* Dont delete this functionality will be implemented in version 2 */}
                {/* {showAlert && (
                    <section className="hcc-alert">
                        <div className="hcc-alert-inner">
                            <div className="hcc-alert-main">
                                <svg
                                    viewBox="0 0 24 24"
                                    className="hcc-alert-icon"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                >
                                    <path d="M12 9v4m0 4h.01" />
                                    <path d="M10.29 3.86l-8.08 14.02A1.5 1.5 0 003.5 20h17a1.5 1.5 0 001.29-2.12L13.71 3.86a1.5 1.5 0 00-2.42 0z" />
                                </svg>
                                <p className="hcc-alert-text">
                                    <strong>Información importante:&nbsp;</strong>
                                    El portal Falabella Business Center se encontrará en mantención
                                    el día domingo 16 de marzo, entre las 00:00 y las 02:00 hrs.
                                    (GMT-4).
                                </p>
                            </div>

                            <button
                                aria-label="Cerrar aviso"
                                onClick={() => setShowAlert(false)}
                                className="hcc-alert-close"
                            >
                                ×
                            </button>
                        </div>
                    </section>
                )} */}

                {/* Contenido */}
                <main className="hcc-main">
                    <FeaturedCategories />
                    <AllCategories devTestingRoutes={devTestingRoutes} />
                    <div className="hcc-divider" />
                    <ActionsSection />
                </main>
            </div>
        </div>
    );
}
