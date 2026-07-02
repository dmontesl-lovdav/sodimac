import { useState } from 'react';
import FiscalCard from './FiscalCard';
import { InvoiceIcon, DocIcon, SearchIcon, AddIcon } from './FiscalIcons';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { versionCatalogService } from '../../services/versionCatalogService';
import { APP_KEYS, PermissionGate } from '@shared/security';

type ModalVariant = 'loading' | 'alert' | 'confirm';

export default function FiscalCardsList(): React.ReactElement {
    const [modalVisible, setModalVisible] = useState(false);
    const [modalVariant, setModalVariant] = useState<ModalVariant>('alert');
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');

    const handleVersionCatalogClick = async (): Promise<void> => {
        setModalTitle('Version Catalog');
        setModalMessage('Validando conexión con el backend fiscal...');
        setModalVariant('loading');
        setModalVisible(true);

        const result = await versionCatalogService.checkConnection();

        setModalVariant('alert');
        setModalMessage(result.message);
    };

    return (
        <>
            <div className="fiscal-cards-grid">
                <PermissionGate app={APP_KEYS.INVOICES}>
                    <FiscalCard
                        title="Facturas"
                        description="Consulta facturas publicadas, descarga XML/PDF y gestiona documentos fiscales."
                        to="/fiscal/facturas"
                        Icon={InvoiceIcon}
                    />
                </PermissionGate>

                <PermissionGate app={APP_KEYS.CREDIT_NOTES}>
                    <FiscalCard
                        title="Notas de Crédito"
                        description="Visualiza las notas de crédito registradas."
                        to="/fiscal/notas-credito"
                        Icon={DocIcon}
                    />
                </PermissionGate>

                <PermissionGate app={APP_KEYS.PAYMENT_COMPLEMENTS}>
                    <FiscalCard
                        title="Consulta de Complementos"
                        description="Consulta el historial de complementos publicados y su estatus de validación."
                        to="/fiscal/consulta-complemento-pago"
                        Icon={SearchIcon}
                    />
                </PermissionGate>

                <FiscalCard
                    title="Version Catalog"
                    description="Valida la conexión al catálogo de versiones CFDI del backend fiscal."
                    Icon={AddIcon}
                    onClick={() => { handleVersionCatalogClick(); }}
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