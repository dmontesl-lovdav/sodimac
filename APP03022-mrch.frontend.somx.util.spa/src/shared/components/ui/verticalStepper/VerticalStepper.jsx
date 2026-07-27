import PropTypes from 'prop-types';
import stepperCaption from './StepperCaption.svg';
import './VerticalStepper.css';

export function Step({ children }) {
    return <div className="vs-step-content">{children}</div>;
}

Step.propTypes = {
    children: PropTypes.node,
};

export function VerticalStepper({ children }) {
    if (!children || children.length === 0) return null;

    return (
        <div className="vs-wrapper">
            {children.map((child, index) => (
                <div className="vs-row" key={`step-${index + 1}`}>
                    <div className="vs-left">
                        <div
                            className="vs-number"
                            style={{ backgroundImage: `url(${stepperCaption})` }}
                        >
                            {index + 1}
                        </div>

                        {index < children.length - 1 && <div className="vs-border" />}
                    </div>

                    <div className="vs-right">{child}</div>
                </div>
            ))}
        </div>
    );
}

VerticalStepper.propTypes = {
    children: PropTypes.node,
};
