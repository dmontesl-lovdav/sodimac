//
// ────────────────────────────────────────────────────────────
// React y librerías base
// ────────────────────────────────────────────────────────────
import * as React from 'react';
import { useEffect } from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { HashRouter, Route, Routes, Navigate } from 'react-router-dom';
import './App.css';

//
// ────────────────────────────────────────────────────────────
// Páginas generales (Home / Help Center)
// ────────────────────────────────────────────────────────────
import HelpCenterContainer from './features/helpCenter/components/HelpCenterContainer';
import HelpCenterFaqByCategory from './features/helpCenter/HelpCenterFaqByCategory';
import HelpCenterFaqDetail from './features/helpCenter/faqs/detail/HelpCenterFaqDetail';
import HelpCenterSupportResources from './features/helpCenter/HelpCenterSupportResources';

//
// ────────────────────────────────────────────────────────────
// Casos (Requests)
// ────────────────────────────────────────────────────────────
import RequestContainer from './features/cases/components/RequestContainer';

//
// ────────────────────────────────────────────────────────────
// FAQ
// ────────────────────────────────────────────────────────────
import FaqGrid from './features/faq/FaqGridContainer';
import AddEditFaqForm from './features/faq/components/AddEditFaqForm';
import BulkFaqUpload from './features/faq/components/BulkFaqUpload';

//
// ────────────────────────────────────────────────────────────
// Categorías
// ────────────────────────────────────────────────────────────
import CategoryGridContainer from './features/categories/CategoryGridContainer';
import AddEditCategoryForm from './features/categories/components/AddEditCategoryForm';
import BulkCategoryUpload from './features/categories/components/BulkCategoryUpload';

//
// ────────────────────────────────────────────────────────────
// Información relacionada
// ────────────────────────────────────────────────────────────
import RelatedInformationContainer from './features/relatedInformation/RelatedInformationContainer';
import AddEditRelatedInformationForm from './features/relatedInformation/components/AddEditRelatedInformationForm';

//
// ────────────────────────────────────────────────────────────
// Feedback
// ────────────────────────────────────────────────────────────
import FeedbackContainer from './features/feedback/FeedbackContainer';
import AddEditFeedbackForm from './features/feedback/components/AddEditFeedbackForm';

//
// ────────────────────────────────────────────────────────────
/* SLAs */
// ────────────────────────────────────────────────────────────
import SlaGridContainer from './features/sla/SlaGridContainer';
import SlaAddEditForm from './features/sla/components/SlaAddEditForm';

//
// ────────────────────────────────────────────────────────────
/* Sección informativa (Notices) */
// ────────────────────────────────────────────────────────────
import NoticeGridContainer from './features/notices/NoticeGridContainer';
import NoticeAddEditForm from './features/notices/components/NoticeAddEditForm';

//
// ────────────────────────────────────────────────────────────
// Resolutores por módulo
// ────────────────────────────────────────────────────────────
import ModuleResolverContainer from './features/moduleResolver/ModuleResolverContainer';
import AddEditModuleResolverForm from './features/moduleResolver/components/AddEditModuleResolverForm';

//
// ────────────────────────────────────────────────────────────
/* Mantenedor */
// ────────────────────────────────────────────────────────────
import MaintainersContainer from './features/mainteners/MaintainersContainer';

//
// ────────────────────────────────────────────────────────────
/* Utils */
// ────────────────────────────────────────────────────────────
import RolesDebug from './shared/utils/rolesDebug';

//
// ────────────────────────────────────────────────────────────
// Single-SPA / Shell
// ────────────────────────────────────────────────────────────
import { mountRootParcel } from 'single-spa';
import singleSpaReact from 'single-spa-react';
import Parcel from 'single-spa-react/parcel';

//
// ────────────────────────────────────────────────────────────
// Config / Auth / Layout / Store
// ────────────────────────────────────────────────────────────
import PrivateRoute from './PrivateRoute';
import ConfigurationBuilder from './configuration/ConfigurationBuilder';
import { AuthConfigDefault } from './domain/authConfig';
import {
    handleSubscribeToGlobalAuthenticationChange,
    handleSubscribeToGlobalConfigurationChange,
} from './services/globalStateService';
import { Layout } from './shared/components/container/Layout';
import { localHomeStore } from './store/localStore';
import { useAppSelector } from './store/hooks/useAppSelector';

//
// ────────────────────────────────────────────────────────────
// Playground de UI
// ────────────────────────────────────────────────────────────
import UiPlayground from './features/playground/UiPlayground';

//
// ────────────────────────────────────────────────────────────
// Debug utilitario para observar el estado global del portal
// ────────────────────────────────────────────────────────────
import DebugGlobalRedux from "@/store/DebugGlobalRedux";

// ===================== Helpers de rol/auth =====================
function useAuthReady() {
    const tokenDecoded = useAppSelector(s => s.authentication?.tokenDecoded);
    return Boolean(tokenDecoded);
}

function useIsVendor() {
    const roles =
        useAppSelector(
            s => s.authentication?.tokenDecoded?.resource_access?.['fbc-aclaraciones']?.roles
        ) || [];
    return Array.isArray(roles) && roles.includes('ppsomx-vendor');
}

// ---------- Pantalla de espera mínima mientras carga el token ----------
function Gate({ children }) {
    const ready = useAuthReady();
    if (!ready) return null;
    return <>{children}</>;
}

// ---------- Landing por rol (solo decide a dónde ir) ----------
function Landing() {
    const isVendor = useIsVendor();
    return isVendor ? <Navigate to="/home" replace /> : <Navigate to="/mantenedor" replace />;
}

// ---------- Bloqueo de /mantenedor para vendors ----------
function RequireNonVendor({ children }) {
    const isVendor = useIsVendor();
    const ready = useAuthReady();
    if (!ready) return null;
    if (isVendor) return <Navigate to="/home" replace />;
    return <>{children}</>;
}

// ===================== Rutas =====================
function AppRoutes() {
    return (
        <Layout>
            <DebugGlobalRedux />
            <Routes>

                {/* Landing por rol */}
                <Route path="/" element={<Landing />} />

                {/* Help Center */}
                <Route path="/home" element={<HelpCenterContainer />} />
                <Route path="/help-center/resources" element={<HelpCenterSupportResources />} />
                <Route
                    path="/help-center/faqs/category"
                    element={<HelpCenterFaqByCategory />}
                />
                <Route path="/help-center/faqs/detail" element={<HelpCenterFaqDetail />} />

                {/* Casos */}
                <Route path="/cases" element={<RequestContainer initialState={3} />} />
                <Route path="/cases/new" element={<RequestContainer initialState={2} />} />
                <Route path="/cases/:id" element={<RequestContainer initialState={4} />} />

                {/* FAQ */}
                <Route path="/faqs" element={<FaqGrid />} />
                <Route path="/faq/new" element={<AddEditFaqForm />} />
                <Route path="/faq/:id/edit" element={<AddEditFaqForm />} />
                <Route path="/faq/bulk-upload" element={<BulkFaqUpload />} />

                {/* Categorías */}
                <Route path="/categories" element={<CategoryGridContainer />} />
                <Route path="/categories/new" element={<AddEditCategoryForm />} />
                <Route path="/categories/:id" element={<AddEditCategoryForm />} />
                <Route path="/categories/bulk-upload" element={<BulkCategoryUpload />} />

                {/* Información relacionada */}
                <Route path="/relatedInformation" element={<RelatedInformationContainer />} />
                <Route path="/relatedInformation/new" element={<AddEditRelatedInformationForm />} />
                <Route path="/relatedInformation/:id/edit" element={<AddEditRelatedInformationForm />} />

                {/* SLAs */}
                <Route path="/slas" element={<SlaGridContainer />} />
                <Route path="/slas/new" element={<SlaAddEditForm />} />
                <Route path="/slas/:id" element={<SlaAddEditForm />} />

                {/* Sección informativa */}
                <Route path="/notices" element={<NoticeGridContainer />} />
                <Route path="/notices/new" element={<NoticeAddEditForm />} />
                <Route path="/notices/:id" element={<NoticeAddEditForm />} />

                {/* Feedback */}
                <Route path="/feedback" element={<FeedbackContainer />} />
                <Route path="/feedback/new" element={<AddEditFeedbackForm />} />
                <Route path="/feedback/:id" element={<AddEditFeedbackForm />} />

                {/* Resolutores por Módulo */}
                <Route
                    path="/moduleResolver"
                    element={
                        <RequireNonVendor>
                            <ModuleResolverContainer />
                        </RequireNonVendor>
                    }
                />

                <Route
                    path="/moduleResolver/new"
                    element={
                        <RequireNonVendor>
                            <AddEditModuleResolverForm />
                        </RequireNonVendor>
                    }
                />

                <Route
                    path="/moduleResolver/:id/edit"
                    element={
                        <RequireNonVendor>
                            <AddEditModuleResolverForm />
                        </RequireNonVendor>
                    }
                />


                {/* 🔥 UI Playground */}
                <Route path="/playground/ui" element={<UiPlayground />} />

                {/* Roles debug */}
                <Route path="/debug/roles" element={<RolesDebug />} />

                {/* Mantenedor (solo no-vendors) */}
                <Route
                    path="/mantenedor"
                    element={
                        <RequireNonVendor>
                            <MaintainersContainer />
                        </RequireNonVendor>
                    }
                />

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Layout>
    );
}

const ErrorBoundary = ({ children }) => {
    try {
        return children;
    } catch (e) {
        console.error('Auth parcel failed:', e);
        return null;
    }
};

const App = () => {
    useEffect(() => handleSubscribeToGlobalAuthenticationChange(), []);
    useEffect(() => handleSubscribeToGlobalConfigurationChange(), []);

    return (
        <HashRouter>
            {ConfigurationBuilder.localDeployment ? null : (
                <ErrorBoundary>
                    <Parcel
                        config={() => import('authentication/App')}
                        mountParcel={mountRootParcel}
                        authConfig={AuthConfigDefault}
                    />
                </ErrorBoundary>
            )}

            <Provider store={localHomeStore}>
                <PrivateRoute>
                    <Gate>
                        <AppRoutes />
                    </Gate>
                </PrivateRoute>
            </Provider>
        </HashRouter>
    );
};

export const { bootstrap, mount, unmount } = singleSpaReact({
    React,
    ReactDOM,
    rootComponent: App,
    errorBoundary(err, errInfo, props) {
        console.error('Error in single-spa-react [aclaraciones] App:', err, errInfo, props);
        return <div>error</div>;
    },
});

export default App;
