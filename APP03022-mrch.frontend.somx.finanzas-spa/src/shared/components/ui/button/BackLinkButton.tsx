import { useState, type ButtonHTMLAttributes, type ReactElement, type ReactNode } from 'react';

interface BackLinkButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children?: ReactNode;
}

const BASE_COLOR = '#003865';
const HOVER_COLOR = '#002d4c';

export default function BackLinkButton({
    children = 'Volver',
    disabled = false,
    style,
    ...rest
}: BackLinkButtonProps): ReactElement {
    const [hovered, setHovered] = useState(false);

    const computedStyle: React.CSSProperties = {
        height: 'auto',
        minHeight: '44px',
        padding: '0 12px',
        background: 'transparent',
        border: 'none',
        fontSize: '14px',
        fontWeight: 500,
        color: hovered && !disabled ? HOVER_COLOR : BASE_COLOR,
        textDecoration: 'underline',
        textUnderlineOffset: '3px',
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.6 : 1,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontFamily: 'inherit',
        ...style,
    };

    return (
        <button
            {...rest}
            disabled={disabled}
            style={computedStyle}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
            {children}
        </button>
    );
}
