import { HashRouter, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import singleSpaReact from 'single-spa-react';
import { createRoot } from 'react-dom/client';
import { catalogosStore } from './store/store';
import CatalogosContainer from './features/home/components/CatalogosContainer';
import SuppliersContainer from './features/suppliers/components/SuppliersContainer';
import SupplierForm from './features/suppliers/components/SupplierForm';
import SupplierBlocksContainer from './features/supplierBlocks/components/SupplierBlocksContainer';
import SupplierBlockForm from './features/supplierBlocks/components/SupplierBlockForm';
import CatalogsContainer from './features/catalogs/components/CatalogsContainer';
import CatalogForm from './features/catalogs/components/CatalogForm';
import CatalogElementsContainer from './features/catalogs/components/CatalogElementsContainer';
import ImportElementsContainer from './features/catalogs/components/ImportElementsContainer';
import ElementForm from './features/catalogs/components/ElementForm';
import ConversionsContainer from './features/catalogs/components/ConversionsContainer';
import NewConversionForm from './features/catalogs/components/NewConversionForm';
import EditConversionForm from './features/catalogs/components/EditConversionForm';

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/catalogos/proveedores/crear" element={<SupplierForm />} />
      <Route path="/catalogos/proveedores/editar/:id" element={<SupplierForm />} />
      <Route path="/catalogos/proveedores/:id/bloquear" element={<SupplierBlockForm />} />
      <Route path="/catalogos/proveedores" element={<SuppliersContainer />} />
      <Route path="/catalogos/bloqueos/crear" element={<SupplierBlockForm />} />
      <Route path="/catalogos/bloqueos/editar/:id" element={<SupplierBlockForm />} />
      <Route path="/catalogos/bloqueos" element={<SupplierBlocksContainer />} />
      <Route path="/catalogos/catalogs/crear" element={<CatalogForm />} />
      <Route path="/catalogos/catalogs/editar/:id" element={<CatalogForm />} />
      <Route path="/catalogos/catalogs/:id/elementos/importar" element={<ImportElementsContainer />} />
      <Route path="/catalogos/catalogs/:id/elementos/nuevo" element={<ElementForm />} />
      <Route path="/catalogos/catalogs/:id/elementos/editar/:elementId" element={<ElementForm />} />
      <Route path="/catalogos/catalogs/:id/elementos" element={<CatalogElementsContainer />} />
      <Route path="/catalogos/elementos/:elementId/conversiones/nueva" element={<NewConversionForm />} />
      <Route path="/catalogos/elementos/:elementId/conversiones/editar/:conversionId" element={<EditConversionForm />} />
      <Route path="/catalogos/elementos/:elementId/conversiones" element={<ConversionsContainer />} />
      <Route path="/catalogos/catalogs" element={<CatalogsContainer />} />
      <Route path="/catalogos" element={<CatalogosContainer />} />
      <Route path="*" element={<CatalogosContainer />} />
    </Routes>
  );
};

const App = () => {
  return (
    <Provider store={catalogosStore}>
      <HashRouter>
        <AppRoutes />
      </HashRouter>
    </Provider>
  );
};

const lifecycles = singleSpaReact({
  React: require('react'),
  ReactDOM: require('react-dom'),
  rootComponent: App,
  renderType: 'createRoot',
});

export const { bootstrap, mount, unmount } = lifecycles;
export default App;

