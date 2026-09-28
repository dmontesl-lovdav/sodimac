import { useEffect, useRef, useState } from 'react';
import { Breadcrumb } from '@shared/components/ui/navigation';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getCurrentUserKey, invalidateAccessContextCache } from '@shared/security';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { syncFiscalUser } from '@/services/fiscalUserSync';
import FiscalCardsList from './parts/FiscalCardsList';

export default function FiscalContainer(): React.ReactElement {
    const isLocal = ConfigurationBuilder.localDeployment;
    const homeSyncStarted = useRef(false);
    const redirectHomeOnClose = useRef(false);
    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');
    const [modalSeverity, setModalSeverity] = useState<'success' | 'error' | 'warning' | 'info'>(
        'info',
    );

    // STM-1577: el cruce macrorol ↔ catálogos corre al cargar esta tarjeta, no en cada opción.
    if (!isLocal && !homeSyncStarted.current) {
        homeSyncStarted.current = true;
        invalidateAccessContextCache(getCurrentUserKey() || undefined);
        void syncFiscalUser();
    }

    useEffect(() => {
        if (isLocal) return;

        let cancelled = false;

        async function runAccessSync() {
            const result = await syncFiscalUser();
            if (cancelled) return;
            if (result.status !== 'denied' && result.status !== 'error') return;

            redirectHomeOnClose.current =
                result.status === 'denied' && result.deniedKind === 'profile';

            setModalSeverity(result.status === 'denied' ? 'warning' : 'error');
            setModalTitle(result.status === 'denied' ? 'Alerta' : 'Error');
            setModalMessage(result.message);
            setModalVisible(true);
        }

        runAccessSync().catch(() => undefined);

        return () => {
            cancelled = true;
        };
    }, [isLocal]);

    const handleCloseModal = () => {
        setModalVisible(false);
        if (redirectHomeOnClose.current) {
            redirectHomeOnClose.current = false;
            window.location.assign(process.env.FBC_HOME?.trim() || '/');
        }
    };

    return (
        <div className="fiscal-container">
            <Breadcrumb items={[{ label: 'Fiscal' }]} />
            <h3 className="fiscal-section-title">Gestión de documentación fiscal</h3>
            <FiscalCardsList />
            <GenericModal
                visible={modalVisible}
                variant="alert"
                severity={modalSeverity}
                title={modalTitle}
                message={modalMessage}
                buttonText="Aceptar"
                onClose={handleCloseModal}
                onConfirm={handleCloseModal}
            />
        </div>
    );
}
