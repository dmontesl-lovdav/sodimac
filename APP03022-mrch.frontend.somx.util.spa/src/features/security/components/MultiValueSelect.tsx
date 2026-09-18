import { useEffect, useMemo, useRef, useState } from 'react';
import type { AttributeValueOption } from '../types';

interface MultiValueSelectProps {
  options: AttributeValueOption[];
  selected: number[];
  onChange: (values: number[]) => void;
  placeholder?: string;
}

const normalize = (value: string): string =>
  value
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase();

export function MultiValueSelect({
  options,
  selected,
  onChange,
  placeholder = 'Selecciona valores',
}: MultiValueSelectProps) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return undefined;
    const onPointerDown = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false);
    };
    document.addEventListener('mousedown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [open]);

  const filteredOptions = useMemo(() => {
    const q = normalize(query.trim());
    if (!q) return options;
    return options.filter(
      (option) => normalize(`${option.catalogKey} ${option.name}`).includes(q),
    );
  }, [options, query]);

  const allSelected = options.length > 0 && selected.length === options.length;

  const toggleAll = () => onChange(allSelected ? [] : options.map((option) => option.id));

  const toggleValue = (id: number) =>
    onChange(selected.includes(id) ? selected.filter((value) => value !== id) : [...selected, id]);

  let triggerLabel = placeholder;
  if (allSelected) {
    triggerLabel = 'Todos seleccionados';
  } else if (selected.length === 1) {
    const one = options.find((option) => option.id === selected[0]);
    triggerLabel = one ? `${one.catalogKey} - ${one.name}` : '1 seleccionado';
  } else if (selected.length > 1) {
    triggerLabel = `${selected.length} seleccionados`;
  }

  return (
    <div className="mvs" ref={ref}>
      <button
        type="button"
        className={`mvs__trigger${open ? ' mvs__trigger--open' : ''}`}
        onClick={() => setOpen((value) => !value)}
        aria-haspopup="listbox"
        aria-expanded={open}
      >
        <span className={`mvs__trigger-label${selected.length === 0 ? ' mvs__trigger-label--placeholder' : ''}`}>
          {triggerLabel}
        </span>
        <span className="mvs__chevron" aria-hidden="true">▾</span>
      </button>

      {open && (
        <div className="mvs__menu" role="listbox" aria-multiselectable="true">
          <div className="mvs__search">
            <input
              type="text"
              className="mvs__search-input"
              placeholder="Buscar..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              autoFocus
            />
          </div>

          <label className="mvs__item mvs__item--all">
            <input type="checkbox" className="mvs__checkbox" checked={allSelected} onChange={toggleAll} />
            <span className="mvs__item-text">Todos</span>
          </label>

          <div className="mvs__list">
            {filteredOptions.length === 0 && (
              <div className="mvs__empty">Sin coincidencias</div>
            )}
            {filteredOptions.map((option) => (
              <label key={option.id} className="mvs__item">
                <input
                  type="checkbox"
                  className="mvs__checkbox"
                  checked={selected.includes(option.id)}
                  onChange={() => toggleValue(option.id)}
                />
                <span className="mvs__item-text">{`${option.catalogKey} - ${option.name}`}</span>
              </label>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
