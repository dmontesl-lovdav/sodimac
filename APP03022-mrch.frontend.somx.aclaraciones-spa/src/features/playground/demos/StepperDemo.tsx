import DemoCard from '../components/DemoCard';
import { Step, VerticalStepper, GenericButton } from '@shared/components/ui';

export default function StepperDemo() {
    return (
        <DemoCard
            title="VerticalStepper"
            desc="Stepper vertical sencillo con numeración y línea."
        >
            <div className="max-w-2xl space-y-4">
                <VerticalStepper>
                    <Step>
                        <div className="p-3 rounded-md border">
                            <h4 className="font-medium">Paso 1: Datos básicos</h4>
                            <p className="text-sm text-slate-600">Nombre, correo y país.</p>
                        </div>
                    </Step>
                    <Step>
                        <div className="p-3 rounded-md border">
                            <h4 className="font-medium">Paso 2: Detalles</h4>
                            <p className="text-sm text-slate-600">Preferencias y opciones.</p>
                        </div>
                    </Step>
                    <Step>
                        <div className="p-3 rounded-md border">
                            <h4 className="font-medium">Paso 3: Confirmación</h4>
                            <GenericButton variant="primary">Confirmar</GenericButton>
                        </div>
                    </Step>
                </VerticalStepper>
            </div>
        </DemoCard>
    );
}
