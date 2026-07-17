// src/shared/components/ui/select/GenericSelectFloating.jsx
import { useEffect, useId, useMemo, useRef, useState } from 'react';

export default function GenericSelectFloating({
    label,
    value,
    onChange,                 // consumer may use this
    onValueChange,            // ...or this (FbcSelect-style)
    options = [],             // [{ value, label }]
    fullWidth = true,
    className = '',
    disabled = false,
    error = false,
    name,
    position,
    refreshDetails,
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

    // left padding for label alignment
    const [leftPad, setLeftPad] = useState(16);
    useEffect(() => {
        const el = buttonRef.current;
        if (!el) return;
        const update = () => {
            const cs = window.getComputedStyle(el);
            const pl = parseFloat(cs.paddingLeft ?? '16');
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

    // outside click / esc
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

    // sync focus with value
    useEffect(() => {
        setFocusIndex(Math.max(0, items.findIndex(i => i.value === String(value))));
    }, [value, items]);

    // keep focused option in view
    useEffect(() => {
        if (!open) return;
        const el = menuRef.current?.querySelector(`[data-idx="${focusIndex}"]`);
        el?.scrollIntoView?.({ block: 'nearest' });
    }, [open, focusIndex]);

    function emitChange(v) {
        // Synthetic event compatible with updateField()
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

        // Fire both, to cubrir ambos casos de uso
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

    // caret
    const baseNavy = '#0f2a3d';
    const idleGray = '#94a3b8';
    let caretColor = idleGray;
    if (disabled) caretColor = '#cbd5e1';
    else if (open) caretColor = baseNavy;

    const caretStyle = open
        ? { width: 0, height: 0, borderLeft: '6px solid transparent', borderRight: '6px solid transparent', borderBottom: `6px solid ${caretColor}` }
        : { width: 0, height: 0, borderLeft: '6px solid transparent', borderRight: '6px solid transparent', borderTop: `6px solid ${caretColor}` };

    let labelTone = 'text-slate-500 peer-focus:text-sky-700';
    if (error) labelTone = 'text-rose-600';
    else if (open) labelTone = 'text-sky-700';

    const labelPosition = hasValue || open ? 'top-1 text-xs' : 'top-[14px] text-sm';
    const borderTone = error
        ? 'border-rose-400 focus:border-rose-500'
        : 'border-slate-300/70 focus:border-sky-600/80';

    return (
        <div className={`relative ${fullWidth ? 'w-full' : 'w-60'} ${className}`}>
            {/* field */}
            <button
                ref={buttonRef}
                id={id}
                type="button"
                disabled={disabled}
                aria-haspopup="listbox"
                aria-expanded={open}
                aria-controls={listId}
                aria-labelledby={labelId}
                // expose attributes (por si alguien los lee del DOM directamente)
                name={name}
                position={position}
                refreshdetails={refreshDetails}
                {...rest}
                className={`
          peer block w-full bg-transparent text-left
          h-14
          text-[15px] text-slate-800 leading-[1.85rem]
          pt-7 pb-2 pr-9 pl-4
          border-0 border-b
          ${borderTone}
          focus:outline-none focus:ring-0
          transition-[border-color] duration-200
          rounded-none tracking-[0.01em]
          ${disabled ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'}
        `}
                onClick={() => !disabled && setOpen(o => !o)}
                onKeyDown={handleKeyDown}
            >
                <span className={hasValue ? 'text-slate-900' : 'text-slate-400'}>
                    {hasValue ? (items.find(i => i.value === String(value))?.label ?? '') : ''}
                </span>
            </button>

            {/* label */}
            <label
                id={labelId}
                className={`
          pointer-events-none absolute
          transition-all duration-200
          ${labelPosition}
          ${labelTone}
          font-medium
        `}
                style={{ left: `${leftPad}px` }}
            >
                {label}
            </label>

            {/* caret */}
            <span
                aria-hidden
                className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2"
                style={caretStyle}
            />

            {/* dropdown */}
            {open && (
                <div
                    ref={menuRef}
                    role="listbox"
                    id={listId}
                    tabIndex={-1}
                    aria-activedescendant={`${id}-opt-${focusIndex}`}
                    className={`
            absolute z-50 left-0 right-0 top-full
            mt-[1px]
            bg-white
            rounded-b-lg rounded-t-none
            shadow-[0_12px_28px_-8px_rgba(2,6,23,0.18)]
            border border-slate-200/80 border-t-0
            overflow-auto max-h-60
          `}
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
                                tabIndex={0}
                                aria-selected={selected}
                                onMouseEnter={() => setFocusIndex(idx)}
                                onMouseDown={(e) => e.preventDefault()} // keep button focus
                                onClick={() => selectValue(it.value)}
                                className={`
                  px-4 py-2.5 text-[15px]
                  ${selected ? 'text-sky-800 font-medium' : 'text-slate-800'}
                  ${focused ? 'bg-sky-50' : 'bg-white'}
                  hover:bg-sky-50 cursor-pointer transition-colors
                `}
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
