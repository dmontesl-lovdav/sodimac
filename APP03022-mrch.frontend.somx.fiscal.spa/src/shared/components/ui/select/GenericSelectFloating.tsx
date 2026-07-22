import { useEffect, useId, useMemo, useRef, useState } from 'react';
import './Select.css';

export interface GenericSelectFloatingOption {
  value: string | number;
  label: string;
}

export interface GenericSelectFloatingProps {
  label?: string;
  value?: string | number | null;
  onChange?: (e: { target: { value: string; name?: string; getAttribute: (a: string) => string | null } }) => void;
  onValueChange?: (e: { target: { value: string } }) => void;
  options?: GenericSelectFloatingOption[];
  fullWidth?: boolean;
  className?: string;
  disabled?: boolean;
  error?: boolean;
  name?: string;
  position?: unknown;
  refreshDetails?: unknown;
  required?: boolean;
  [key: string]: unknown;
}

export function hasFloatingSelectValue(value: string | number | null | undefined): boolean {
  return value !== '' && value !== undefined && value !== null;
}

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
  ...rest
}: GenericSelectFloatingProps): React.ReactElement {
  const id = useId();
  const listId = `${id}-listbox`;
  const labelId = `${id}-label`;
  const buttonRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  const items = useMemo(
    () => options.map((o) => ({ value: String(o.value), label: String(o.label) })),
    [options]
  );

  const currentIndex = Math.max(0, items.findIndex((i) => i.value === String(value)));
  const [open, setOpen] = useState(false);
  const [focusIndex, setFocusIndex] = useState(currentIndex);
  const hasValue = hasFloatingSelectValue(value);
  const [leftPad, setLeftPad] = useState(16);

  useEffect(() => {
    const el = buttonRef.current;
    if (!el) return;
    const update = (): void => {
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

  useEffect(() => {
    if (!open) return;
    const onDocClick = (e: MouseEvent): void => {
      if (!buttonRef.current || !menuRef.current) return;
      if (buttonRef.current.contains(e.target as Node)) return;
      if (menuRef.current.contains(e.target as Node)) return;
      setOpen(false);
    };
    const onEsc = (e: KeyboardEvent): void => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('mousedown', onDocClick);
    document.addEventListener('keydown', onEsc);
    return () => {
      document.removeEventListener('mousedown', onDocClick);
      document.removeEventListener('keydown', onEsc);
    };
  }, [open]);

  useEffect(() => {
    setFocusIndex(Math.max(0, items.findIndex((i) => i.value === String(value))));
  }, [value, items]);

  useEffect(() => {
    if (!open) return;
    const el = menuRef.current?.querySelector(`[data-idx="${focusIndex}"]`);
    (el as HTMLElement)?.scrollIntoView?.({ block: 'nearest' });
  }, [open, focusIndex]);

  function emitChange(v: string): void {
    const syntheticTarget = {
      value: v,
      name,
      position,
      refreshDetails,
      getAttribute: (attr: string): string | null => {
        if (attr === 'name') return name ?? null;
        if (attr === 'position') return String(position ?? null);
        if (attr === 'refreshDetails') return String(refreshDetails ?? null);
        return rest && Object.prototype.hasOwnProperty.call(rest, attr) ? String((rest as Record<string, unknown>)[attr]) : null;
      },
    };
    const evt = { target: syntheticTarget };
    onChange?.(evt as Parameters<NonNullable<typeof onChange>>[0]);
    onValueChange?.({ target: { value: v } });
  }

  const selectValue = (v: string): void => {
    emitChange(v);
    setOpen(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent): void => {
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
        } else setFocusIndex((i) => Math.min(max, i + 1));
        break;
      case 'ArrowUp':
        e.preventDefault();
        if (!open) {
          setOpen(true);
          setFocusIndex(Math.max(0, currentIndex));
        } else setFocusIndex((i) => Math.max(0, i - 1));
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

  const rootClass = `fiscal-select-floating-root ${fullWidth ? 'fiscal-select-floating-root-fullWidth' : 'fiscal-select-floating-root-fixed'} ${className}`.trim();
  const buttonClass = `fiscal-select-floating-button ${open ? 'fiscal-select-floating-button-open' : ''} ${error ? 'fiscal-select-floating-button-error' : ''}`.trim();
  const labelPositionClass = hasValue || open ? 'fiscal-select-floating-label-floating' : 'fiscal-select-floating-label-resting';
  const openOrDefaultClass = open ? 'fiscal-select-floating-label-focused' : 'fiscal-select-floating-label-default';
  const labelColorClass = error ? 'fiscal-select-floating-label-error' : openOrDefaultClass;
  const labelClass = `fiscal-select-floating-label ${labelPositionClass} ${labelColorClass}`.trim();

  return (
    <div
      className={rootClass}
      style={{ ['--fiscal-select-floating-label-left' as string]: `${leftPad}px` } as React.CSSProperties}
    >
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
        name={name as string}
        className={buttonClass}
        {...(rest as React.ButtonHTMLAttributes<HTMLButtonElement>)}
        onClick={() => !disabled && setOpen((o) => !o)}
        onKeyDown={handleKeyDown}
      >
        <span className={hasValue ? 'fiscal-select-floating-value' : 'fiscal-select-floating-placeholder'}>
          {hasValue ? items.find((i) => i.value === String(value))?.label ?? '' : ''}
        </span>
      </button>

      {label ? (
        <label id={labelId} className={labelClass} title={label}>
          {label}
          {required ? <span className="fiscal-select-floating-required" aria-hidden> *</span> : null}
        </label>
      ) : null}

      <span
        aria-hidden
        className={`fiscal-select-floating-caret ${open ? 'fiscal-select-floating-caret-open' : 'fiscal-select-floating-caret-closed'} ${disabled ? 'fiscal-select-floating-caret-disabled' : ''}`.trim()}
      />

      {open ? (
        <div
          ref={menuRef}
          role="listbox"
          id={listId}
          tabIndex={-1}
          aria-activedescendant={`${id}-opt-${focusIndex}`}
          className="fiscal-select-floating-dropdown"
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
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => selectValue(it.value)}
                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); selectValue(it.value); } }}
                className={`fiscal-select-floating-option ${focused ? 'fiscal-select-floating-option-focus' : ''} ${selected ? 'fiscal-select-floating-option-selected' : ''}`.trim()}
              >
                {it.label}
              </div>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
