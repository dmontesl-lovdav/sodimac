import React from 'react';
import stepperCaption from './StepperCaption.svg';
import './VerticalStepper.css';

export function Step({ children }: { children?: React.ReactNode }): React.ReactElement {
  return <div className="fiscal-stepper-content">{children}</div>;
}

export interface VerticalStepperProps {
  children?: React.ReactNode[];
  className?: string;
}

export function VerticalStepper({ children, className = '' }: VerticalStepperProps): React.ReactElement {
  if (!children || children.length === 0) {
    return <></>;
  }

  const list = Array.isArray(children) ? children : [children];

  return (
    <div className={`fiscal-stepper ${className}`.trim()}>
      {list.map((child, index) => (
        <div className="fiscal-stepper-row" key={index}>
          <div className="fiscal-stepper-col-icon">
            <div
              className="fiscal-stepper-icon"
              style={{ backgroundImage: `url(${stepperCaption})` }}
            >
              {index + 1}
            </div>
            {index < list.length - 1 ? <div className="fiscal-stepper-line" /> : null}
          </div>
          <div className="fiscal-stepper-content">{child}</div>
        </div>
      ))}
    </div>
  );
}
