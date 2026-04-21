import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter, Route, Routes } from 'react-router-dom';
import singleSpaReact from 'single-spa-react';

import FiscalContainer from './features/home/components/FiscalContainer';
import InvoicesContainer from './features/invoice/InvoicesContainer';
import ComplementContainer from './features/complement/ComplementContainer';
import AddComplement from './features/complement/AddComplement';
import CreditsContainer from './features/creditNote/CreditsContainer';
import ComplementRelatedInvoices from './features/complement/ComplementRelatedInvoices';
import { Layout } from './shared/components/container/Layout';
import './App.css';
import PublishCreditNote from './features/creditNote/PublishCreditNote';

function AppRoutes() {
    return (
        <Layout>
            <Routes>
                <Route path="/" element={<FiscalContainer />} />
                <Route path="/fiscal" element={<FiscalContainer />} />
                <Route path="/fiscal/facturas" element={<InvoicesContainer />} />
                <Route path="/fiscal/consulta-complemento-pago" element={<ComplementContainer />} />
                <Route path="/fiscal/publicar-complemento" element={<AddComplement />} />
                <Route path="/fiscal/complemento/:uuid" element={<ComplementRelatedInvoices />} />
                <Route path="/fiscal/notas-credito" element={<CreditsContainer />} />
                <Route path="/fiscal/publicar-nota-credito" element={<PublishCreditNote />} />
                <Route path="*" element={<FiscalContainer />} />
            </Routes>
        </Layout>
    );
}

const App: React.FC = () => {
    return (
        <HashRouter>
            <AppRoutes />
        </HashRouter>
    );
};

export const { bootstrap, mount, unmount } = singleSpaReact({
    React,
    ReactDOM,
    rootComponent: App,
    errorBoundary(err, errInfo, props) {
        console.error('Error in single-spa-react [fiscal] App:', err, errInfo, props);
        return <div>Error cargando módulo fiscal</div>;
    },
});

export default App;
