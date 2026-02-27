import { useMemo, useState } from 'react';

/**
 * GenericSelectUnderline — select con estilo subrayado + label flotante (Tailwind only)
 *
 * Props:
 * - label?: string                // ← si lo pasas, el label flota dentro
 * - value: string|number|null|undefined
 * - onChange: (e) => void
 * - options: { value: string|number, label: string }[]
 * - placeholder?: string          // default: 'Selecciona…' (como <option disabled>)
 * - includePlaceholder?: boolean  // default: true
 * - fullWidth?: boolean           // default: true
 * - className?: string
 */
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

    // evita duplicar opción vacía si ya viene en options
    const hasEmpty = useMemo(
        () => options.some(o => String(o?.value ?? '') === ''),
        [options]
    );
    const showPlaceholder = includePlaceholder && !hasEmpty && Boolean(placeholder);

    const filled = focused || normValue !== '';

    return (
        <div className={`relative ${fullWidth ? 'w-full' : 'w-60'} ${className}`}>
            {/* reserva espacio para el label flotante */}
            <div className="relative pt-3">
                {label && (
                    <span
                        className={[
                            // base
                            'absolute left-0 top-0',
                            'text-gray-500 pointer-events-none',
                            'transition-all duration-150 ease-out',
                            // tamaños y posición
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
                        'text-gray-900 text-sm px-0 pr-6', // pr-6 deja espacio al caret
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

                {/* caret */}
                <span className="absolute right-1 top-1/2 -translate-y-1/2 text-gray-600 pointer-events-none">
                    ▾
                </span>
            </div>
        </div>
    );
}
