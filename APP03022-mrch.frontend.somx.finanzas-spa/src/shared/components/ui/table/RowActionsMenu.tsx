import { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import type { NavigateFunction } from "react-router-dom";
import "./styles/RowActionsMenu.css";

export type RowActionsMenuItem = {
  title: string;
  icon: string;
  disabled?: boolean;
  onClick: () => void;
};

export type RowActionLike<T> = {
  title: string;
  icon: string;
  onClick: (row: T, nav: NavigateFunction) => void;
  isDisabled?: (row: T) => boolean;
};

function KebabIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      width="20"
      height="20"
      aria-hidden="true"
      className="fin-row-actions-kebab-icon"
    >
      <circle cx="12" cy="5" r="1.75" fill="currentColor" />
      <circle cx="12" cy="12" r="1.75" fill="currentColor" />
      <circle cx="12" cy="19" r="1.75" fill="currentColor" />
    </svg>
  );
}

export function toRowActionMenuItems<T>(
  row: T,
  actions: RowActionLike<T>[],
  nav: NavigateFunction
): RowActionsMenuItem[] {
  return actions.map(({ title, icon, onClick, isDisabled }) => ({
    title,
    icon,
    disabled: isDisabled?.(row) ?? false,
    onClick: () => {
      void onClick(row, nav);
    },
  }));
}

export default function RowActionsMenu({ items }: { items: RowActionsMenuItem[] }) {
  const [open, setOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState({ top: 0, right: 0 });

  const updatePosition = useCallback(() => {
    const rect = triggerRef.current?.getBoundingClientRect();
    if (!rect) return;
    setPos({
      top: rect.bottom + 4,
      right: Math.max(8, window.innerWidth - rect.right),
    });
  }, []);

  const close = useCallback(() => setOpen(false), []);

  const toggle = () => {
    if (open) {
      close();
      return;
    }
    updatePosition();
    setOpen(true);
  };

  useEffect(() => {
    if (!open) return;

    const onPointerDown = (e: MouseEvent) => {
      const target = e.target as Node;
      if (menuRef.current?.contains(target) || triggerRef.current?.contains(target)) {
        return;
      }
      close();
    };

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
    };

    const onReposition = () => updatePosition();

    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    window.addEventListener("resize", onReposition);
    window.addEventListener("scroll", onReposition, true);

    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("resize", onReposition);
      window.removeEventListener("scroll", onReposition, true);
    };
  }, [open, close, updatePosition]);

  if (items.length === 0) return null;

  return (
    <div className="fin-row-actions">
      <button
        ref={triggerRef}
        type="button"
        title="Acciones"
        aria-label="Acciones"
        aria-haspopup="menu"
        aria-expanded={open}
        className="fin-row-actions-kebab-btn"
        onClick={toggle}
      >
        <KebabIcon />
      </button>

      {open &&
        createPortal(
          <div
            ref={menuRef}
            role="menu"
            className="fin-row-actions-menu"
            style={{ top: pos.top, right: pos.right }}
          >
            {items.map(({ title, icon, disabled, onClick }) => (
              <button
                key={title}
                type="button"
                role="menuitem"
                title={title}
                disabled={disabled}
                className="fin-row-actions-menu-item"
                onClick={() => {
                  if (disabled) return;
                  close();
                  onClick();
                }}
              >
                <img src={icon} className="fin-row-actions-menu-icon" alt="" />
                <span>{title}</span>
              </button>
            ))}
          </div>,
          document.body
        )}
    </div>
  );
}
