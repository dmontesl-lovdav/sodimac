import { useEffect, useId, useMemo, useRef, useState } from 'react';
import './styles/GenericSelectFloating.css';

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
    requiredMarkClassName = 'gs-required',
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
            if (buttonRef.current?.contains(e.target)) return;
            if (menuRef.current?.contains(e.target)) return;
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
                return rest && Object.prototype.hasOwnProperty.call(rest, attr)
                    ? rest[attr]
                    : null;
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
                    setFocusIndex(currentIndex);
                } else {
                    setFocusIndex(i => Math.min(max, i + 1));
                }
                break;

            case 'ArrowUp':
                e.preventDefault();
                if (!open) {
                    setOpen(true);
                    setFocusIndex(currentIndex);
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
        }
    };

    const baseNavy = '#0f2a3d';
    const idleGray = '#94a3b8';
    const caretColor = disabled ? '#cbd5e1' : (open ? baseNavy : idleGray);

    const caretStyle = open
        ? { borderBottom: `6px solid ${caretColor}` }
        : { borderTop: `6px solid ${caretColor}` };

    return (
        <div className={`gs-wrapper ${fullWidth ? 'gs-full' : 'gs-fixed'} ${className}`}>

            {/* BTN */}
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
                position={position}
                refreshdetails={refreshDetails}
                {...rest}
                className={`gs-button 
                    ${disabled ? 'gs-disabled' : ''} 
                    ${error ? 'gs-error' : ''}`}
                onClick={() => !disabled && setOpen(o => !o)}
                onKeyDown={handleKeyDown}
            >
                <span className={hasValue ? 'gs-value' : 'gs-placeholder'}>
                    {hasValue
                        ? (items.find(i => i.value === String(value))?.label ?? '')
                        : ''}
                </span>
            </button>

            {/* Floating Label */}
            <label
                id={labelId}
                className={`gs-label 
                    ${hasValue || open ? 'gs-label-floating' : ''} 
                    ${error ? 'gs-label-error' : open ? 'gs-label-open' : ''}`}
                style={{ left: `${leftPad}px` }}
                title={label}
            >
                {label}
                {required && <span className={`${requiredMarkClassName}`}> *</span>}
            </label>

            {/* Caret */}
            <span
                aria-hidden
                className="gs-caret"
                style={caretStyle}
            />

            {/* Dropdown */}
            {open && (
                <div
                    ref={menuRef}
                    role="listbox"
                    id={listId}
                    tabIndex={-1}
                    aria-activedescendant={`${id}-opt-${focusIndex}`}
                    className="gs-dropdown"
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
                                className={`gs-option 
                                    ${selected ? 'gs-option-selected' : ''} 
                                    ${focused ? 'gs-option-focused' : ''}`}
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
