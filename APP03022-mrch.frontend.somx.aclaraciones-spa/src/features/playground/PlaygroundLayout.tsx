import React from 'react';
import type { MenuItem } from './components/SideMenu';
import SideMenu from './components/SideMenu';

type Props = {
    items: MenuItem[];
    activeId: string;
    onSelect: (id: string) => void;
    children: React.ReactNode;
    title?: string;
    subtitle?: string;
};

export default function PlaygroundLayout({
    items,
    activeId,
    onSelect,
    children,
    title = 'UI Playground',
    subtitle = 'Explora los componentes genéricos como en un Storybook',
}: Props) {
    return (
        <div className="min-h-[calc(100vh-64px)]">
            <header className="sticky top-0 z-40 bg-white/80 backdrop-blur border-b border-slate-200">
                <div className="max-w-6xl mx-auto px-4 py-4">
                    <h1 className="text-2xl font-semibold text-slate-900">{title}</h1>
                    <p className="text-sm text-slate-600">{subtitle}</p>
                </div>
            </header>

            <div className="max-w-6xl mx-auto px-4 py-6 grid grid-cols-12 gap-6">
                <aside className="col-span-12 md:col-span-3">
                    <SideMenu items={items} activeId={activeId} onSelect={onSelect} />
                </aside>

                <main className="col-span-12 md:col-span-9 space-y-8">
                    {children}
                </main>
            </div>
        </div>
    );
}
