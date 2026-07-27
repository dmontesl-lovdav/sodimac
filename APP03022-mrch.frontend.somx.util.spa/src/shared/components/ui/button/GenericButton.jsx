import PropTypes from 'prop-types';
import './GenericButton.css';

export default function GenericButton({
    children,
    type = 'button',
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
        cancel: 'btn-cancel',
        back: 'btn-back',
    }[variant];

    return (
        <button
            type={type}
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

GenericButton.propTypes = {
    children: PropTypes.node,
    type: PropTypes.string,
    variant: PropTypes.string,
    className: PropTypes.string,
    style: PropTypes.object,
    disabled: PropTypes.bool,
    onClick: PropTypes.func,
};
