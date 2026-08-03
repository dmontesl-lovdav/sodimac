import { useEffect, useMemo, useState, type ReactNode } from 'react';
import GenericButton from '@shared/components/ui/button/GenericButton';
import type { AssignableItem } from '../types';
import '../styles/DualTransferList.css';

interface Props {
  title: string;
  leftTitle: string;
  rightTitle: string;
  leftItems: AssignableItem[];
  rightItems: AssignableItem[];
  onSave: (selectedIds: number[]) => Promise<boolean | void>;
  onBack: () => void;
  onAssignedItemsChange?: (items: AssignableItem[]) => void;
  renderAvailableItemExtra?: (item: AssignableItem) => ReactNode;
  renderAssignedItemExtra?: (item: AssignableItem) => ReactNode;
}

export function DualTransferList({
  title,
  leftTitle,
  rightTitle,
  leftItems,
  rightItems,
  onSave,
  onBack,
  onAssignedItemsChange,
  renderAvailableItemExtra,
  renderAssignedItemExtra,
}: Readonly<Props>) {
  const [query, setQuery] = useState('');
  const [available, setAvailable] = useState<AssignableItem[]>(leftItems);
  const [assigned, setAssigned] = useState<AssignableItem[]>(rightItems);
  const [leftSelected, setLeftSelected] = useState<Set<number>>(new Set());
  const [rightSelected, setRightSelected] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setAvailable(leftItems);
    setAssigned(rightItems);
    setLeftSelected(new Set());
    setRightSelected(new Set());
  }, [leftItems, rightItems]);

  useEffect(() => {
    onAssignedItemsChange?.(assigned);
  }, [assigned, onAssignedItemsChange]);

  const filteredAvailable = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return available;
    return available.filter((item) =>
      `${item.title} ${item.subtitle ?? ''}`.toLowerCase().includes(normalized),
    );
  }, [available, query]);

  const moveRight = (ids: number[]) => {
    if (!ids.length) return;
    const selected = available.filter((item) => ids.includes(item.id));
    setAssigned((prev) => [...prev, ...selected].sort((a, b) => a.title.localeCompare(b.title)));
    setAvailable((prev) => prev.filter((item) => !ids.includes(item.id)));
    setLeftSelected(new Set());
  };

  const moveLeft = (ids: number[]) => {
    if (!ids.length) return;
    const selected = assigned.filter((item) => ids.includes(item.id));
    setAvailable((prev) => [...prev, ...selected].sort((a, b) => a.title.localeCompare(b.title)));
    setAssigned((prev) => prev.filter((item) => !ids.includes(item.id)));
    setRightSelected(new Set());
  };

  const submit = async () => {
    setLoading(true);
    try {
      const shouldClose = await onSave(assigned.map((item) => item.id));
      if (shouldClose !== false) {
        onBack();
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dual-layout">
      <div className="dual-header">
        <h2>{title}</h2>
        <GenericButton variant="primary" onClick={submit} disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar'}
        </GenericButton>
      </div>

      <div className="dual-grid">
        <div className="dual-column" aria-label={leftTitle}>
          <header>{leftTitle} ({filteredAvailable.length})</header>
          <div className="dual-search">
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              className="security-input"
              placeholder="Buscar..."
            />
            <label>
              <input
                type="checkbox"
                checked={filteredAvailable.length > 0 && filteredAvailable.every((item) => leftSelected.has(item.id))}
                onChange={(event) => setLeftSelected(event.target.checked ? new Set(filteredAvailable.map((item) => item.id)) : new Set())}
              />
              {' '}
              Seleccionar visibles
            </label>
          </div>
          <div className="dual-items">
            {filteredAvailable.map((item) => (
              <div
                key={item.id}
                className="dual-item"
              >
                <input
                  type="checkbox"
                  checked={leftSelected.has(item.id)}
                  onChange={() => setLeftSelected((prev) => {
                    const next = new Set(prev);
                    if (next.has(item.id)) next.delete(item.id);
                    else next.add(item.id);
                    return next;
                  })}
                />
                <div>
                  <strong>{item.tags?.map((tag) => <span key={tag} className="tag">{tag}</span>)}</strong>
                  {item.title}
                  {renderAssignedItemExtra?.(item)}
                </div>
              </div>
            ))}
          </div>
        </div>

        <section className="dual-controls">
          <GenericButton variant="outlineFill" onClick={() => moveRight(Array.from(leftSelected))}>&gt;&gt;</GenericButton>
     
          <GenericButton variant="outlineFill" onClick={() => moveLeft(Array.from(rightSelected))}>&lt;&lt;</GenericButton>
        </section>

        <div className="dual-column" aria-label={rightTitle}>
          <header>{rightTitle} ({assigned.length})</header>
          <div className="dual-items">
            {assigned.map((item) => (
              <div
                key={item.id}
                className="dual-item"
              >
                <input
                  type="checkbox"
                  checked={rightSelected.has(item.id)}
                  onChange={() => setRightSelected((prev) => {
                    const next = new Set(prev);
                    if (next.has(item.id)) next.delete(item.id);
                    else next.add(item.id);
                    return next;
                  })}
                />
                <div>
                  <strong>{item.tags?.map((tag) => <span key={tag} className="tag">{tag}</span>)}</strong>
                  {item.title}
                  {renderAvailableItemExtra?.(item)}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="dual-back">
        <GenericButton variant="back" onClick={onBack}>
          Volver
        </GenericButton>
      </div>
    </div>
  );
}

