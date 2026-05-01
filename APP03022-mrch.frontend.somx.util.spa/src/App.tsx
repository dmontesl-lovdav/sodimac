import '@/App.css';
import React from 'react';
import * as ReactDOMClient from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HashRouter, Route, Routes } from 'react-router-dom';

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

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
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
          </Routes>
        </Layout>
      </HashRouter>
    </QueryClientProvider>
  );
};

const isLocalStandalone =
  typeof document !== 'undefined' && !!document.getElementById('root');

let bootstrap = async () => { };
let mount = async () => { };
let unmount = async () => { };

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
