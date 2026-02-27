import { useEffect, useId, useMemo, useRef, useState } from 'react';

const styles = {
    wrapper: {
        position: 'relative',
    },
    button: {
        display: 'block',
        width: '100%',
        backgroundColor: 'transparent',
        textAlign: 'left',
        height: '3.5rem',
        fontSize: '15px',
        color: '#1e293b',
        lineHeight: '1.85rem',
        padding: '1.75rem 2.25rem 0.5rem 1rem',
        border: 'none',
        borderBottom: '1px solid rgba(203, 213, 225, 0.7)',
        outline: 'none',
        transition: 'border-color 200ms ease',
        borderRadius: 0,
        letterSpacing: '0.01em',
        cursor: 'pointer',
    },
    buttonDisabled: {
        opacity: 0.6,
        cursor: 'not-allowed',
    },
    buttonFocused: {
        borderBottomColor: 'rgba(2, 132, 199, 0.8)',
    },
    buttonError: {
        borderBottomColor: '#fb7185',
    },
    label: {
        position: 'absolute',
        pointerEvents: 'none',
        transition: 'all 200ms ease',
        fontWeight: '500',
    },
    labelFloating: {
        top: '0.25rem',
        fontSize: '0.75rem',
    },
    labelResting: {
        top: '0.875rem',
        fontSize: '0.875rem',
    },
    labelDefault: {
        color: '#64748b',
    },
    labelFocused: {
        color: '#0369a1',
    },
    labelError: {
        color: '#e11d48',
    },
    requiredMark: {
        marginLeft: '0.125rem',
        color: '#e11d48',
    },
    dropdown: {
        position: 'absolute',
        zIndex: 50,
        left: 0,
        right: 0,
        top: '100%',
        marginTop: '1px',
        backgroundColor: '#ffffff',
        borderBottomLeftRadius: '0.5rem',
        borderBottomRightRadius: '0.5rem',
        boxShadow: '0 12px 28px -8px rgba(2, 6, 23, 0.18)',
        border: '1px solid rgba(226, 232, 240, 0.8)',
        borderTop: 'none',
        overflow: 'auto',
        maxHeight: '15rem',
    },
    option: {
        padding: '0.625rem 1rem',
        fontSize: '15px',
        cursor: 'pointer',
        transition: 'background-color 150ms',
    },
    optionHover: {
        backgroundColor: '#f0f9ff',
    },
    optionSelected: {
        color: '#075985',
        fontWeight: '500',
    },
};

export default function GenericSelectFloating({
    label,
    value,
    onChange,
    onValueChange,
    options = [],
    fullWidth = true,
    className = '',
    disabled = false,
    error = false,
    name,
    position,
    refreshDetails,
    required = false,
    requiredMarkClassName = '',
    ...rest
}) {
    const id = useId();
    const listId = `${id}-listbox`;
    const labelId = `${id}-label`;
    const buttonRef = useRef(null);
    const menuRef = useRef(null);

    const items = useMemo(
        () => options.map(o => ({ value: String(o.value), label: String(o.label) })),
        [options]
    );

    const currentIndex = Math.max(0, items.findIndex(i => i.value === String(value)));
    const [open, setOpen] = useState(false);
    const [focusIndex, setFocusIndex] = useState(currentIndex);
    const hasValue = value !== '' && value !== undefined && value !== null;

    const [leftPad, setLeftPad] = useState(16);

    useEffect(() => {
        const el = buttonRef.current;
        if (!el) return;
        const update = () => {
            const cs = window.getComputedStyle(el);
            const pl = parseFloat(cs.paddingLeft || '16');
            setLeftPad(Number.isFinite(pl) ? pl : 16);
        };
        update();
        const ro = new ResizeObserver(update);
        ro.observe(el);
        window.addEventListener('resize', update);
        return () => {
            ro.disconnect();
            window.removeEventListener('resize', update);
        };
    }, []);

    useEffect(() => {
        if (!open) return;
        const onDocClick = (e) => {
            if (!buttonRef.current || !menuRef.current) return;
            if (buttonRef.current.contains(e.target)) return;
            if (menuRef.current.contains(e.target)) return;
            setOpen(false);
        };
        const onEsc = (e) => e.key === 'Escape' && setOpen(false);
        document.addEventListener('mousedown', onDocClick);
        document.addEventListener('keydown', onEsc);
        return () => {
            document.removeEventListener('mousedown', onDocClick);
            document.removeEventListener('keydown', onEsc);
        };
    }, [open]);

    useEffect(() => {
        setFocusIndex(Math.max(0, items.findIndex(i => i.value === String(value))));
    }, [value, items]);

    useEffect(() => {
        if (!open) return;
        const el = menuRef.current?.querySelector(`[data-idx="${focusIndex}"]`);
        el?.scrollIntoView?.({ block: 'nearest' });
    }, [open, focusIndex]);

    function emitChange(v) {
        const syntheticTarget = {
            value: v,
            name,
            position,
            refreshDetails,
            getAttribute: (attr) => {
                if (attr === 'name') return name ?? null;
                if (attr === 'position') return position ?? null;
                if (attr === 'refreshDetails') return refreshDetails ?? null;
                return rest && Object.prototype.hasOwnProperty.call(rest, attr) ? rest[attr] : null;
            },
        };
        const evt = { target: syntheticTarget };
        onChange?.(evt);
        onValueChange?.(evt);
    }

    const selectValue = (v) => {
        emitChange(v);
        setOpen(false);
    };

    const handleKeyDown = (e) => {
        if (disabled) return;
        const max = items.length - 1;

        switch (e.key) {
            case ' ':
            case 'Enter':
                e.preventDefault();
                if (!open) setOpen(true);
                else if (items[focusIndex]) selectValue(items[focusIndex].value);
                break;
            case 'ArrowDown':
                e.preventDefault();
                if (!open) {
                    setOpen(true);
                    setFocusIndex(Math.max(0, currentIndex));
                } else {
                    setFocusIndex(i => Math.min(max, i + 1));
                }
                break;
            case 'ArrowUp':
                e.preventDefault();
                if (!open) {
                    setOpen(true);
                    setFocusIndex(Math.max(0, currentIndex));
                } else {
                    setFocusIndex(i => Math.max(0, i - 1));
                }
                break;
            case 'Home':
                e.preventDefault();
                setOpen(true);
                setFocusIndex(0);
                break;
            case 'End':
                e.preventDefault();
                setOpen(true);
                setFocusIndex(max);
                break;
            default:
                break;
        }
    };

    const baseNavy = '#0f2a3d';
    const idleGray = '#94a3b8';
    const caretColor = disabled ? '#cbd5e1' : (open ? baseNavy : idleGray);
    const caretStyle = open
        ? { width: 0, height: 0, borderLeft: '6px solid transparent', borderRight: '6px solid transparent', borderBottom: `6px solid ${caretColor}` }
        : { width: 0, height: 0, borderLeft: '6px solid transparent', borderRight: '6px solid transparent', borderTop: `6px solid ${caretColor}` };

    const getButtonStyle = () => {
        let style = { ...styles.button };
        if (disabled) style = { ...style, ...styles.buttonDisabled };
        if (open) style = { ...style, ...styles.buttonFocused };
        if (error) style = { ...style, ...styles.buttonError };
        return style;
    };

    const getLabelStyle = () => {
        let style = {
            ...styles.label,
            ...(hasValue || open ? styles.labelFloating : styles.labelResting),
            left: `${leftPad}px`,
        };
        if (error) style = { ...style, ...styles.labelError };
        else if (open) style = { ...style, ...styles.labelFocused };
        else style = { ...style, ...styles.labelDefault };
        return style;
    };

    return (
        <div style={{ ...styles.wrapper, width: fullWidth ? '100%' : '15rem' }} className={className}>
            <button
                ref={buttonRef}
                id={id}
                type="button"
                disabled={disabled}
                aria-haspopup="listbox"
                aria-expanded={open}
                aria-controls={listId}
                aria-labelledby={labelId}
                aria-required={required}
                name={name}
                {...rest}
                style={getButtonStyle()}
                onClick={() => !disabled && setOpen(o => !o)}
                onKeyDown={handleKeyDown}
            >
                <span style={{ color: hasValue ? '#0f172a' : '#94a3b8' }}>
                    {hasValue ? (items.find(i => i.value === String(value))?.label ?? '') : ''}
                </span>
            </button>

            <label id={labelId} style={getLabelStyle()} title={label}>
                {label}
                {required && <span style={styles.requiredMark} aria-hidden> *</span>}
            </label>

            <span
                aria-hidden
                style={{
                    pointerEvents: 'none',
                    position: 'absolute',
                    right: '0.75rem',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    ...caretStyle,
                }}
            />

            {open && (
                <div
                    ref={menuRef}
                    role="listbox"
                    id={listId}
                    tabIndex={-1}
                    aria-activedescendant={`${id}-opt-${focusIndex}`}
                    style={styles.dropdown}
                >
                    {items.map((it, idx) => {
                        const selected = String(value) === it.value;
                        const focused = idx === focusIndex;
                        return (
                            <div
                                key={it.value}
                                id={`${id}-opt-${idx}`}
                                data-idx={idx}
                                role="option"
                                aria-selected={selected}
                                onMouseEnter={() => setFocusIndex(idx)}
                                onMouseDown={(e) => e.preventDefault()}
                                onClick={() => selectValue(it.value)}
                                style={{
                                    ...styles.option,
                                    ...(focused ? styles.optionHover : {}),
                                    ...(selected ? styles.optionSelected : {}),
                                }}
                            >
                                {it.label}
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
