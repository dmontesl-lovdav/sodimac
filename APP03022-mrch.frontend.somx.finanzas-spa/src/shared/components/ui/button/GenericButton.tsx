import React from 'react';
import './styles/GenericButton.css';

type Variant =
  | 'primary'
  | 'outline'
  | 'link'
  | 'outlineFill'
  | 'text'
  | 'cancel';

interface GenericButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
}

export default function GenericButton({
  children,
  variant = 'primary',
  className = '',
  style,
  disabled = false,
  ...props
}: GenericButtonProps) {

  const variantClass: Record<Variant, string> = {
    primary: 'btn-primary',
    outline: 'btn-outline',
    link: 'btn-link',
    outlineFill: 'btn-outlineFill',
    text: 'btn-text',
    cancel: 'btn-cancel',
  };

  return (
    <button
      {...props}
      disabled={disabled}
      style={style}
      className={[
        'generic-btn',
        variantClass[variant],
        disabled ? 'disabled' : 'enabled',
        className
      ].join(' ')}
    >
      {children}
    </button>
  );
}