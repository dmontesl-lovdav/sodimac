import { useMemo, useState } from 'react';
import PropTypes from 'prop-types';

export default function GenericSelectUnderline({
    label,
    value,
    onChange,
    options = [],
    placeholder = 'Selecciona…',
    includePlaceholder = true,
    fullWidth = true,
    className = '',
    ...props
}) {
    const [focused, setFocused] = useState(false);
    const normValue = value == null ? '' : String(value);

    const hasEmpty = useMemo(
        () => options.some(o => String(o?.value ?? '') === ''),
        [options]
    );
    const showPlaceholder = includePlaceholder && !hasEmpty && Boolean(placeholder);

    const filled = focused || normValue !== '';

    return (
        <div className={`relative ${fullWidth ? 'w-full' : 'w-60'} ${className}`}>
            <div className="relative pt-3">
                {label && (
                    <span
                        className={[
                            'absolute left-0 top-0',
                            'text-gray-500 pointer-events-none',
                            'transition-all duration-150 ease-out',
                            filled ? '-translate-y-2 text-xs text-sky-600' : 'translate-y-1 text-sm',
                        ].join(' ')}
                    >
                        {label}
                    </span>
                )}

                <select
                    value={normValue}
                    onChange={(e) => onChange?.(e)}
                    onFocus={() => setFocused(true)}
                    onBlur={() => setFocused(false)}
                    className={[
                        'block w-full appearance-none bg-transparent',
                        'text-gray-900 text-sm px-0 pr-6', 
                        'border-0 border-b-2 border-gray-300',
                        'focus:border-sky-600 focus:ring-0',
                        'h-11',
                    ].join(' ')}
                    {...props}
                >
                    {showPlaceholder && (
                        <option value="" disabled>
                            {placeholder}
                        </option>
                    )}
                    {options.map(({ value: v, label: lbl }) => {
                        const val = String(v ?? '');
                        return (
                            <option key={`opt-${val}`} value={val}>
                                {lbl}
                            </option>
                        );
                    })}
                </select>

                <span className="absolute right-1 top-1/2 -translate-y-1/2 text-gray-600 pointer-events-none">
                    ▾
                </span>
            </div>
        </div>
    );
}

GenericSelectUnderline.propTypes = {
    label: PropTypes.node,
    value: PropTypes.any,
    onChange: PropTypes.func,
    options: PropTypes.array,
    placeholder: PropTypes.string,
    includePlaceholder: PropTypes.bool,
    fullWidth: PropTypes.bool,
    className: PropTypes.string,
};
