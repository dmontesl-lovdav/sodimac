import '@/App.css';
import React, { useEffect } from 'react';
import * as ReactDOMClient from 'react-dom/client';
import { Provider } from 'react-redux';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HashRouter, Route, Routes } from 'react-router-dom';

import { localHomeStore } from './store/localStore';
import {
  handleSubscribeToGlobalAuthenticationChange,
  handleSubscribeToGlobalConfigurationChange,
} from './services/globalStateService';

import { Layout } from '@shared/components/container/Layout';
import UtilContainer from '@/features/maintainers/UtilContainer';
import { ParameterConfigPage } from '@/features/parameters/ParameterConfigPage';
import {
  SecurityContainer,
  ProfileUserPage,
  ProfileModulePage,
  ProfileModuleProcessPage,
  ApplicationEventPage,
  UserRolePage,
  RolePermissionPage,
  UserAttributePage,
} from '@/features/security';
import { UserCatalogPage } from './features/security/UserCatalogPage';
import { UserCatalogDetailPage } from './features/security/UserCatalogDetailPage';
import { UserCatalogApplicationEventsPage } from './features/security/UserCatalogApplicationEventsPage';
import CatalogosHubPage from '@/features/catalogos/CatalogosHubPage';
import SuppliersContainer from '@/features/catalogos/components/suppliers/SuppliersContainer';
import SupplierForm from '@/features/catalogos/components/suppliers/SupplierForm';
import SupplierBlocksContainer from '@/features/catalogos/components/supplierBlocks/SupplierBlocksContainer';
import SupplierBlockForm from '@/features/catalogos/components/supplierBlocks/SupplierBlockForm';
import CatalogsContainer from '@/features/catalogos/components/catalogs/CatalogsContainer';
import CatalogForm from '@/features/catalogos/components/catalogs/CatalogForm';
import CatalogElementsContainer from '@/features/catalogos/components/catalogs/CatalogElementsContainer';
import ImportElementsContainer from '@/features/catalogos/components/catalogs/ImportElementsContainer';
import ElementForm from '@/features/catalogos/components/catalogs/ElementForm';
import ConversionsContainer from '@/features/catalogos/components/catalogs/ConversionsContainer';
import NewConversionForm from '@/features/catalogos/components/catalogs/NewConversionForm';
import EditConversionForm from '@/features/catalogos/components/catalogs/EditConversionForm';

import AuditLogsContainer from '@/features/audit-logs/AuditLogsContainer';
import AuditLogsTrainContainer from '@/features/audit-logs/AuditLogsTrainContainer';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      refetchOnMount: false,
      refetchOnReconnect: false,
      retry: 1,
      staleTime: 30 * 1000,
    },
  },
});

const getTokenPreview = (token?: string | null) => {
  if (!token) return null;

  return `${token.substring(0, 12)}...${token.substring(token.length - 8)}`;
};

const logUtilAuthState = (label: string) => {
  const state = localHomeStore.getState() as any;
  const token = state?.authentication?.token;

  console.log(label, {
    hostname: window.location.hostname,
    isLocal:
      window.location.hostname === 'localhost' ||
      window.location.hostname === '127.0.0.1',
    isUat: window.location.hostname.includes('uat'),
    hasAuthentication: !!state?.authentication,
    hasToken: !!token,
    tokenLength: token?.length ?? 0,
    tokenPreview: getTokenPreview(token),
    authentication: state?.authentication,
  });
};

const App: React.FC = () => {
  useEffect(() => {
    console.log('🧩 UTIL App mounted');

    logUtilAuthState('🔎 UTIL auth state BEFORE global subscriptions');

    const authSubscriptionResult = handleSubscribeToGlobalAuthenticationChange?.();
    const configSubscriptionResult = handleSubscribeToGlobalConfigurationChange?.();

    console.log('✅ UTIL global subscriptions initialized', {
      hasAuthSubscriptionResult: !!authSubscriptionResult,
      hasConfigSubscriptionResult: !!configSubscriptionResult,
    });

    logUtilAuthState('🔎 UTIL auth state AFTER global subscriptions');

    const unsubscribeStore = localHomeStore.subscribe(() => {
      logUtilAuthState('🔐 UTIL Redux store changed');
    });

    const timeoutId = window.setTimeout(() => {
      logUtilAuthState('⏱️ UTIL auth state AFTER 500ms');
    }, 500);

    return () => {
      window.clearTimeout(timeoutId);
      unsubscribeStore();

      if (typeof authSubscriptionResult === 'function') {
        authSubscriptionResult();
      }

      if (typeof configSubscriptionResult === 'function') {
        configSubscriptionResult();
      }
    };
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <Provider store={localHomeStore}>
        <HashRouter>
          <Layout>
            <Routes>
              <Route path="/" element={<UtilContainer />} />
              <Route path="/util" element={<UtilContainer />} />
              <Route path="/util/parametros" element={<ParameterConfigPage />} />

              <Route path="/seguridad" element={<SecurityContainer />} />
              <Route path="/seguridad/perfil-usuario" element={<ProfileUserPage />} />
              <Route path="/seguridad/perfil-aplicativo" element={<ProfileModulePage />} />
              <Route path="/seguridad/perfil-evento" element={<ProfileModuleProcessPage />} />
              <Route path="/seguridad/aplicativo-evento" element={<ApplicationEventPage />} />
              <Route path="/seguridad/gestion-usuarios" element={<UserCatalogPage />} />
              <Route path="/seguridad/gestion-usuarios/:userId" element={<UserCatalogDetailPage />} />
              <Route
                path="/seguridad/gestion-usuarios/:userId/app/:moduleId/eventos"
                element={<UserCatalogApplicationEventsPage />}
              />
              <Route path="/seguridad/rol-usuario" element={<UserRolePage />} />
              <Route path="/seguridad/rol-permiso" element={<RolePermissionPage />} />
              <Route path="/seguridad/usuario-atributo" element={<UserAttributePage />} />

              <Route path="/util/catalogos" element={<CatalogosHubPage />} />
              <Route path="/util/catalogos/proveedores" element={<SuppliersContainer />} />
              <Route path="/util/catalogos/proveedores/crear" element={<SupplierForm />} />
              <Route path="/util/catalogos/proveedores/editar/:id" element={<SupplierForm />} />
              <Route path="/util/catalogos/proveedores/:id/bloquear" element={<SupplierBlockForm />} />

              <Route path="/util/catalogos/bloqueos" element={<SupplierBlocksContainer />} />
              <Route path="/util/catalogos/bloqueos/crear" element={<SupplierBlockForm />} />
              <Route path="/util/catalogos/bloqueos/editar/:id" element={<SupplierBlockForm />} />

              <Route path="/util/catalogos/catalogs" element={<CatalogsContainer />} />
              <Route path="/util/catalogos/catalogs/crear" element={<CatalogForm />} />
              <Route path="/util/catalogos/catalogs/editar/:id" element={<CatalogForm />} />
              <Route path="/util/catalogos/catalogs/:id/elementos" element={<CatalogElementsContainer />} />
              <Route path="/util/catalogos/catalogs/:id/elementos/importar" element={<ImportElementsContainer />} />
              <Route path="/util/catalogos/catalogs/:id/elementos/nuevo" element={<ElementForm />} />
              <Route path="/util/catalogos/catalogs/:id/elementos/editar/:elementId" element={<ElementForm />} />

              <Route path="/util/catalogos/elementos/:elementId/conversiones" element={<ConversionsContainer />} />
              <Route path="/util/catalogos/elementos/:elementId/conversiones/nueva" element={<NewConversionForm />} />
              <Route path="/util/catalogos/elementos/:elementId/conversiones/editar/:conversionId" element={<EditConversionForm />} />

              <Route path="/util/auditoria/bitacora-actividades" element={<AuditLogsContainer />} />
              <Route path="/util/auditoria/bitacora-actividades/tren/:traceId" element={<AuditLogsTrainContainer />} />
            </Routes>
          </Layout>
        </HashRouter>
      </Provider>
    </QueryClientProvider>
  );
};

const isLocalStandalone =
  typeof document !== 'undefined' && !!document.getElementById('root');

let bootstrap: any = async () => { };
let mount: any = async () => { };
let unmount: any = async () => { };

if (!isLocalStandalone) {
  const singleSpaReact = require('single-spa-react').default;

  const lifecycles = singleSpaReact({
    React,
    ReactDOMClient,
    rootComponent: App,
    errorBoundary(err: unknown, errInfo: unknown, props: unknown) {
      console.error('Error in single-spa-react [util] App:', err, errInfo, props);
      return <div>Error loading util module</div>;
    },
  });

  bootstrap = lifecycles.bootstrap;
  mount = lifecycles.mount;
  unmount = lifecycles.unmount;
}

export { bootstrap, mount, unmount };
export default App;