// src/shared/components/ui/verticalStepper/VerticalStepper.jsx
import stepperCaption from './StepperCaption.svg';
import './VerticalStepper.css';

export function Step({ children }) {
    return <div className="vs-step-content">{children}</div>;
}

export function VerticalStepper({ children }) {
    if (!children || children.length === 0) return null;

    return (
        <div className="vs-wrapper">
            {children.map((child, index) => (
                <div className="vs-row" key={index}>
                    {/* ícono numérico */}
                    <div className="vs-left">
                        <div
                            className="vs-number"
                            style={{ backgroundImage: `url(${stepperCaption})` }}
                        >
                            {index + 1}
                        </div>

                        {/* línea vertical */}
                        {index < children.length - 1 && <div className="vs-border" />}
                    </div>

                    {/* contenido */}
                    <div className="vs-right">{child}</div>
                </div>
            ))}
        </div>
    );
}
