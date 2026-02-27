import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter, Route, Routes } from 'react-router-dom';
import singleSpaReact from 'single-spa-react';

import FiscalContainer from './features/home/components/FiscalContainer';
import ComplementPublishContainer from './features/complement/components/ComplementPublishContainer';
import InvoicesContainer from './features/invoices/components/InvoicesContainer';
import ComplementPaymentContainer from './features/complementPayment/ComplementPaymentContainer';
import './styles.css';
import CreditsContainer from './features/credits/components/CreditsContainer';

const appStyles = {
    app: {
        minHeight: '100vh',
        width: '100%',
        backgroundColor: '#ffffff',
    },
};

function AppRoutes() {
    return (
        <div style={appStyles.app}>
            <Routes>
                <Route path="/" element={<FiscalContainer />} />
                <Route path="/fiscal" element={<FiscalContainer />} />
                <Route path="/fiscal/facturas" element={<InvoicesContainer />} />
                <Route path="/fiscal/complemento/:paymentId" element={<ComplementPublishContainer />} />
                <Route path="/fiscal/consulta-complemento-pago" element={<ComplementPaymentContainer />} />
                
                <Route path="/fiscal/notas-credito" element={<CreditsContainer />} />
                
                <Route path="*" element={<FiscalContainer />} />
            </Routes>
        </div>
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
