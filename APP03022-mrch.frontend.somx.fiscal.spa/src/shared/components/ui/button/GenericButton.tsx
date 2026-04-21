import React from 'react';
import './Button.css';

export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'link' | 'outlineFill';

export interface GenericButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children?: React.ReactNode;
  variant?: ButtonVariant;
  className?: string;
  disabled?: boolean;
}

export default function GenericButton({
  children,
  variant = 'primary',
  className = '',
  disabled = false,
  ...props
}: GenericButtonProps): React.ReactElement {
  const rootClass = `fiscal-button fiscal-button-${variant} ${className}`.trim();
  return (
    <button
      {...props}
      type={props.type ?? 'button'}
      disabled={disabled}
      className={rootClass}
    >
      {children}
    </button>
  );
}
