import { useState } from 'react';
import FiscalCard from './FiscalCard';
import { InvoiceIcon, DocIcon, SearchIcon, AddIcon } from './FiscalIcons';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { versionCatalogService } from '../../services/versionCatalogService';

type ModalVariant = 'loading' | 'message' | 'confirm';

export default function FiscalCardsList(): React.ReactElement {
    const [modalVisible, setModalVisible] = useState(false);
    const [modalVariant, setModalVariant] = useState<ModalVariant>('message');
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');

    const handleVersionCatalogClick = async (): Promise<void> => {
        setModalTitle('Version Catalog');
        setModalMessage('Validando conexión con el backend fiscal...');
        setModalVariant('loading');
        setModalVisible(true);

        const result = await versionCatalogService.checkConnection();

        setModalVariant('message');
        setModalMessage(result.message);
    };

    return (
        <>
            <div className="fiscal-cards-grid">
                <FiscalCard
                    title="Facturas"
                    description="Consulta facturas publicadas, descarga XML/PDF y gestiona documentos fiscales."
                    to="/fiscal/facturas"
                    Icon={InvoiceIcon}
                />

                <FiscalCard
                    title="Notas de Crédito"
                    description="Visualiza las notas de crédito registradas."
                    to="/fiscal/notas-credito"
                    Icon={DocIcon}
                />

                <FiscalCard
                    title="Consulta de Complementos"
                    description="Consulta el historial de complementos publicados y su estatus de validación."
                    to="/fiscal/consulta-complemento-pago"
                    Icon={SearchIcon}
                />

                <FiscalCard
                    title="Version Catalog"
                    description="Valida la conexión al catálogo de versiones CFDI del backend fiscal."
                    Icon={AddIcon}
                    onClick={handleVersionCatalogClick}
                />
            </div>

            <GenericModal
                visible={modalVisible}
                variant={modalVariant}
                title={modalTitle}
                message={modalMessage}
                buttonText="Aceptar"
                onClose={() => setModalVisible(false)}
                onConfirm={() => setModalVisible(false)}
            />
        </>
    );
}