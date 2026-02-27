
import stepperCaption from './StepperCaption.svg';

export function Step({ children }) {
  return <div>{children}</div>;
}

export function VerticalStepper({ children }) {
  if (!children || children.length === 0) return <></>;

  return (
    <div className="somx-stepper">
      {children.map((child, index) => (
        <div key={index} className="somx-stepper-row">
          <div className="somx-stepper-icon-wrapper">
            <div
              className="somx-stepper-icon"
              style={{ backgroundImage: `url(${stepperCaption})` }}
            >
              {index + 1}
            </div>
            {index < children.length - 1 && <div className="somx-stepper-border" />}
          </div>
          <div className="somx-stepper-content">{child}</div>
        </div>
      ))}
    </div>
  );
}
