import { Route, Routes } from 'react-router-dom';

import FinanzasContainer from './features/mainteners/components/FinanzasContainer';
import DiscountsContainer from './features/rebates/components/DiscountsContainer';
import PaymentsContainer from './features/payments/components/PaymentsContainer';
import PaymentDetail from './features/payments/components/PaymentDetail';
import ReceptionContainer from './features/orders/components/ReceptionContainer';
import ReceptionDetail from './features/orders/components/ReceptionDetail';
import ShippingGuideContainer from './features/shippingGuides/components/ShippingGuideContainer';

import ShippingGuideDetailView from './features/shippingGuides/components/ShippingGuideDetail';
import ShippingGuideStatusUpdate from './features/shippingGuides/components/ShippingGuideStatusUpdate';

import ReceptionInvoice from './features/orders/components/ReceptionInvoice';
import ReceptionCredits from './features/orders/components/ReceptionCredits';


export default function App() {
    return (
        <Routes>
            <Route path="/" element={<FinanzasContainer />} />
            <Route path="/finanzas/descuentos" element={<DiscountsContainer />} />
            <Route path="/finanzas/pagos" element={<PaymentsContainer />} />
            <Route path="/finanzas/pagos/:paymentNumber" element={<PaymentDetail />} />

            <Route path="/recepciones" element={<ReceptionContainer />} />
            <Route path="/recepciones/:uuid" element={<ReceptionDetail />} />
            <Route path="/recepciones/:uuid/editar" element={<ReceptionDetail editable />} />
            <Route path="/recepciones/:uuid/factura" element={<ReceptionInvoice />} />
            <Route path="/recepciones/:uuid/notas-credito" element={<ReceptionCredits />} />

            <Route path="/guias/" element={<ShippingGuideContainer />} />
            <Route path="/guias/:guideId" element={<ShippingGuideDetailView />} />
            <Route path="/guias/:guideId/estatus" element={<ShippingGuideStatusUpdate />} />


            <Route path="*" element={<FinanzasContainer />} />
        </Routes>
    );
}
