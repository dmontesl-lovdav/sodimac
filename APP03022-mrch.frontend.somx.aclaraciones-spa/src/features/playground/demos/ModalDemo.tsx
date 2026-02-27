import { useState } from 'react';
import { GenericButton, GenericModal } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

export default function ModalDemo() {
    const [showLoading, setShowLoading] = useState(false);
    const [alert, setAlert] = useState<{
        visible: boolean;
        severity: 'success' | 'error' | 'info' | 'warning';
        title?: string;
        message?: string;
    }>({ visible: false, severity: 'info', title: '', message: '' });

    return (
        <DemoCard
            title="GenericModal"
            desc="Modal genérico reutilizable para loading y alertas."
        >
            <div className="flex flex-wrap gap-3">
                <GenericButton
                    variant="primary"
                    onClick={() => {
                        setShowLoading(true);
                        setTimeout(() => setShowLoading(false), 1500);
                    }}
                >
                    Mostrar loading
                </GenericButton>

                <GenericButton
                    variant="outline"
                    onClick={() =>
                        setAlert({
                            visible: true,
                            severity: 'success',
                            title: 'Operación exitosa',
                            message: 'El registro fue guardado correctamente.',
                        })
                    }
                >
                    Alert success
                </GenericButton>

                <GenericButton
                    variant="outline"
                    onClick={() =>
                        setAlert({
                            visible: true,
                            severity: 'error',
                            title: 'Ocurrió un error',
                            message: 'No fue posible completar la operación.',
                        })
                    }
                >
                    Alert error
                </GenericButton>

                <GenericButton
                    variant="outline"
                    onClick={() =>
                        setAlert({
                            visible: true,
                            severity: 'warning',
                            title: 'Atención',
                            message: 'Revisa los datos ingresados.',
                        })
                    }
                >
                    Alert warning
                </GenericButton>

                <GenericButton
                    variant="outline"
                    onClick={() =>
                        setAlert({
                            visible: true,
                            severity: 'info',
                            title: 'Información',
                            message: 'Este es un mensaje informativo.',
                        })
                    }
                >
                    Alert info
                </GenericButton>
            </div>

            <GenericModal
                visible={showLoading}
                variant="loading"
                message="Procesando…"
                onClose={() => { }}
            />

            <GenericModal
                visible={alert.visible}
                variant="alert"
                severity={alert.severity}
                title={alert.title}
                message={alert.message}
                buttonText="Aceptar"
                onClose={() => setAlert({ ...alert, visible: false })}
            />
        </DemoCard>
    );
}
