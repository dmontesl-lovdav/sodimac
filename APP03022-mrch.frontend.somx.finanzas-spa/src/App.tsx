// ────────────────────────────────────────────────────────────
// React / Router / Redux / Single-SPA
// ────────────────────────────────────────────────────────────
import React, { useEffect } from 'react';
import ReactDOM from 'react-dom';
import { HashRouter, Route, Routes } from 'react-router-dom';
import { Provider } from 'react-redux';
import ErrorBoundary from '@/shared/components/error-boundary/ErrorBoundary';
import singleSpaReact from 'single-spa-react';
import { mountRootParcel } from 'single-spa';
import Parcel from 'single-spa-react/parcel';

// ────────────────────────────────────────────────────────────
// Layout + estilos globales del microfrontend
// ────────────────────────────────────────────────────────────
import { Layout } from './shared/components/container/Layout';
import './App.css';

// ────────────────────────────────────────────────────────────
// Store local (microfrontend finanzas)
// ────────────────────────────────────────────────────────────
import { localHomeStore } from './store/localStore';
import { useAppSelector } from './store/hooks/useAppSelector';

// ────────────────────────────────────────────────────────────
// Suscripción a estado global (auth / configuration)
// ────────────────────────────────────────────────────────────
import {
    handleSubscribeToGlobalAuthenticationChange,
    handleSubscribeToGlobalConfigurationChange,
} from './services/globalStateService';

// ────────────────────────────────────────────────────────────
// Auth / Config
// ────────────────────────────────────────────────────────────
import ConfigurationBuilder from './configuration/ConfigurationBuilder';
import { AuthConfigDefault } from './domain/authConfig';
import PrivateRoute from './PrivateRoute';

// ────────────────────────────────────────────────────────────
// Containers / pages (Finanzas)
// ────────────────────────────────────────────────────────────
import FinanzasContainer from './features/mainteners/FinanzasContainer';

// Pagos
import PaymentsContainer from './features/payments/PaymentsContainer';
import PaymentDetail from './features/payments/components/PaymentDetail';

// Recepciones
import ReceptionContainer from './features/orders/ReceptionContainer';
import ReceptionDetail from './features/orders/components/ReceptionDetail';
import ReceptionInvoice from './features/orders/components/ReceptionInvoice';
import ReceptionCredits from './features/orders/components/ReceptionCredits';

// Guías de embarque
import ShippingGuideContainer from './features/shippingGuides/ShippingGuideContainer';
import ShippingGuideDetailView from './features/shippingGuides/components/ShippingGuideDetail';
import ShippingGuideStatusUpdate from './features/shippingGuides/components/ShippingGuideStatusUpdate';

// Descuentos / Rebates
import DiscountsContainer from './features/discounts/DiscountsContainer';
import Rebates from './features/rebates/components/DiscountsContainer';

// Estado de cuenta
import AccountStatementContainer from './features/accountStatement/AccountStatementContainer';

// Three Way Match
import ThreeWayMatchContainer from './features/three-way-match/ThreeWayMatchContainer';

// Auditoría (Bitácora)
import AuditLogsContainer from './features/audit-logs/AuditLogsContainer';
import AuditLogsTrainContainer from './features/audit-logs/AuditLogsTrainContainer';

// MIGO
import MigoContainer from './features/migo/MigoContainer';
import MigoUploadLayout from './features/migo/MigoUploadLayout';
import MigoReceptions from './features/migo/MigoReceptions';
import MigoArticles from './features/migo/MigoArticles';

// ────────────────────────────────────────────────────────────
// Gate para esperar token cargado en store local
// ────────────────────────────────────────────────────────────
import DebugGlobalRedux from '@/store/DebugGlobalRedux';

function useAuthReady() {
    const tokenDecoded = useAppSelector((s) => s.authentication?.tokenDecoded);
    return Boolean(tokenDecoded && Object.keys(tokenDecoded).length > 0);
}

function Gate({ children }: { children: React.ReactNode }) {
    const ready = useAuthReady();
    if (!ready) return null;
    return <>{children}</>;
}

// ────────────────────────────────────────────────────────────
// Rutas del microfrontend
// ────────────────────────────────────────────────────────────
function AppRoutes() {
    return (
        <Layout>
            <DebugGlobalRedux />
            <Routes>
                <Route path="/" element={<FinanzasContainer />} />
                <Route path="/finanzas" element={<FinanzasContainer />} />

                <Route path="/finanzas/pagos" element={<PaymentsContainer />} />
                <Route path="/finanzas/pagos/detalle" element={<PaymentDetail />} />

                <Route path="/finanzas/recepciones" element={<ReceptionContainer />} />
                <Route path="/finanzas/recepciones/:uuid" element={<ReceptionDetail />} />
                <Route
                    path="/finanzas/recepciones/:uuid/editar"
                    element={<ReceptionDetail editable />}
                />
                <Route
                    path="/finanzas/recepciones/:uuid/factura"
                    element={<ReceptionInvoice />}
                />
                <Route
                    path="/finanzas/recepciones/:uuid/notas-credito"
                    element={<ReceptionCredits />}
                />

                <Route path="/finanzas/guias" element={<ShippingGuideContainer />} />
                <Route path="/finanzas/guias/:guideId" element={<ShippingGuideDetailView />} />
                <Route
                    path="/finanzas/guias/:guideId/estatus"
                    element={<ShippingGuideStatusUpdate />}
                />

                <Route
                    path="/finanzas/descuentos-comerciales"
                    element={<DiscountsContainer />}
                />
                <Route path="/finanzas/rebates" element={<Rebates />} />

                <Route
                    path="/finanzas/estado-cuenta"
                    element={<AccountStatementContainer />}
                />

                <Route
                    path="/finanzas/three-way-match"
                    element={<ThreeWayMatchContainer />}
                />

                <Route path="/finanzas/migo" element={<MigoContainer />} />
                <Route path="/finanzas/migo/publicar" element={<MigoUploadLayout />} />
                <Route path="/finanzas/migo/:id/recepciones" element={<MigoReceptions />} />
                <Route
                    path="/finanzas/migo/:id/recepciones/:nroOc/:nroRecepcion/articulos"
                    element={<MigoArticles />}
                />

                <Route
                    path="/auditoria/bitacora-actividades"
                    element={<AuditLogsContainer />}
                />
                <Route
                    path="/auditoria/bitacora-actividades/tren/:traceId"
                    element={<AuditLogsTrainContainer />}
                />
            </Routes>
        </Layout>
    );
}

// ────────────────────────────────────────────────────────────
// App root del microfrontend (Redux + Router)
// ────────────────────────────────────────────────────────────
const App: React.FC = () => {
    useEffect(() => {
        if (ConfigurationBuilder.localDeployment) return;

        const unsubscribeAuth = handleSubscribeToGlobalAuthenticationChange();
        const unsubscribeConfig = handleSubscribeToGlobalConfigurationChange();

        return () => {
            if (typeof unsubscribeAuth === 'function') unsubscribeAuth();
            if (typeof unsubscribeConfig === 'function') unsubscribeConfig();
        };
    }, []);

    const isLocal = ConfigurationBuilder.localDeployment;

    return (
        <HashRouter>
            {!isLocal && (
                <ErrorBoundary>
                    <Parcel
                        config={() => import('authentication/App')}
                        mountParcel={mountRootParcel}
                        authConfig={AuthConfigDefault}
                    />
                </ErrorBoundary>
            )}

            <Provider store={localHomeStore}>
                <ErrorBoundary>
                    {isLocal ? (
                        <AppRoutes />
                    ) : (
                        <PrivateRoute>
                            <Gate>
                                <AppRoutes />
                            </Gate>
                        </PrivateRoute>
                    )}
                </ErrorBoundary>
            </Provider>
        </HashRouter>
    );
};

// ────────────────────────────────────────────────────────────
// Single-SPA lifecycle
// ────────────────────────────────────────────────────────────
export const { bootstrap, mount, unmount } = singleSpaReact({
    React,
    ReactDOM,
    rootComponent: App,
    errorBoundary(err, errInfo, props) {
        console.error('Error in single-spa-react [finanzas] App:', err, errInfo, props);
        return <div>Error cargando módulo finanzas</div>;
    },
});

export default App;