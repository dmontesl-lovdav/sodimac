import React, { useEffect, useRef } from 'react';

export type MenuItem = { id: string; label: string };
type Props = {
    items: MenuItem[];
    activeId: string;
    onSelect: (id: string) => void;
};

export default function SideMenu({ items, activeId, onSelect }: Props) {
    const refs = useRef<Record<string, HTMLButtonElement | null>>({});

    // Focus active on change (UX/a11y)
    useEffect(() => {
        const btn = refs.current[activeId];
        if (btn) btn.focus({ preventScroll: true });
    }, [activeId]);

    const handleKey = (e: React.KeyboardEvent) => {
        const idx = items.findIndex((i) => i.id === activeId);
        if (idx < 0) return;
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            onSelect(items[Math.min(items.length - 1, idx + 1)].id);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            onSelect(items[Math.max(0, idx - 1)].id);
        } else if (e.key === 'Home') {
            e.preventDefault();
            onSelect(items[0].id);
        } else if (e.key === 'End') {
            e.preventDefault();
            onSelect(items[items.length - 1].id);
        }
    };

    return (
        <nav
            aria-label="UI Playground sections"
            className="sticky top-24 bg-white/90 backdrop-blur border border-slate-200 rounded-lg shadow-sm"
            onKeyDown={handleKey}
        >
            <ul className="p-2">
                {items.map((it) => {
                    const active = it.id === activeId;
                    return (
                        <li key={it.id}>
                            <button
                                ref={(el) => (refs.current[it.id] = el)}
                                onClick={() => onSelect(it.id)}
                                className={[
                                    'w-full text-left px-3 py-2 rounded-md text-sm transition-colors cursor-pointer',
                                    active
                                        ? 'bg-slate-900 text-white'
                                        : 'text-slate-700 hover:bg-slate-50 hover:text-slate-900',
                                    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-600'
                                ].join(' ')}
                                aria-current={active ? 'page' : undefined}
                            >
                                {it.label}
                            </button>
                        </li>
                    );
                })}
            </ul>
        </nav>
    );
}
