import { useEffect, useRef, useState } from 'react';
import type { AttributeValueOption } from '../types';

interface MultiValueSelectProps {
  options: AttributeValueOption[];
  selected: number[];
  onChange: (values: number[]) => void;
  placeholder?: string;
}

const styles: Record<string, React.CSSProperties> = {
  wrap: { position: 'relative', width: '100%' },
  trigger: {
    width: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '0.5rem',
    padding: '0.4rem 0.6rem',
    border: '1px solid #cbd5e1',
    borderRadius: '6px',
    background: '#fff',
    cursor: 'pointer',
    fontSize: '0.85rem',
    color: '#0f172a',
  },
  menu: {
    position: 'absolute',
    zIndex: 20,
    top: 'calc(100% + 4px)',
    left: 0,
    right: 0,
    maxHeight: '220px',
    overflowY: 'auto',
    background: '#fff',
    border: '1px solid #cbd5e1',
    borderRadius: '6px',
    boxShadow: '0 6px 18px rgba(15, 23, 42, 0.12)',
    padding: '0.25rem',
  },
  item: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.35rem 0.5rem',
    fontSize: '0.85rem',
    color: '#0f172a',
    cursor: 'pointer',
    borderRadius: '4px',
  },
  separator: { height: '1px', background: '#e2e8f0', margin: '0.25rem 0' },
};

export function MultiValueSelect({ options, selected, onChange, placeholder = 'Selecciona valores' }: MultiValueSelectProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const onPointerDown = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', onPointerDown);
    return () => document.removeEventListener('mousedown', onPointerDown);
  }, [open]);

  const allSelected = options.length > 0 && selected.length === options.length;

  const toggleAll = () => onChange(allSelected ? [] : options.map((option) => option.id));

  const toggleValue = (id: number) =>
    onChange(selected.includes(id) ? selected.filter((value) => value !== id) : [...selected, id]);

  let label = placeholder;
  if (allSelected) {
    label = 'Todos';
  } else if (selected.length > 0) {
    label = `${selected.length} seleccionado(s)`;
  }

  return (
    <div style={styles.wrap} ref={ref}>
      <button type="button" style={styles.trigger} onClick={() => setOpen((value) => !value)}>
        <span>{label}</span>
        <span aria-hidden="true">▾</span>
      </button>
      {open && (
        <div style={styles.menu} role="listbox" aria-multiselectable="true">
          <label style={styles.item}>
            <input type="checkbox" checked={allSelected} onChange={toggleAll} />
            <span>Todos</span>
          </label>
          <div style={styles.separator} />
          {options.map((option) => (
            <label key={option.id} style={styles.item}>
              <input
                type="checkbox"
                checked={selected.includes(option.id)}
                onChange={() => toggleValue(option.id)}
              />
              <span>{`${option.catalogKey} - ${option.name}`}</span>
            </label>
          ))}
        </div>
      )}
    </div>
  );
}
