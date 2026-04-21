import './GenericButton.css';

export default function GenericButton({
    children,
    variant = 'primary',
    className = '',
    style = {},
    disabled = false,
    ...props
}) {
    const variantClass = {
        primary: 'btn-primary',
        outline: 'btn-outline',
        link: 'btn-link',
        outlineFill: 'btn-outlineFill',
        text: 'btn-text',
    }[variant];

    return (
        <button
            {...props}
            disabled={disabled}
            style={style}
            className={[
                'generic-btn',
                variantClass,
                disabled ? 'disabled' : 'enabled',
                className
            ].join(' ')}
        >
            {children}
        </button>
    );
}
