import { Suspense, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Breadcrumb } from '@shared/components/ui';

import PlaygroundLayout from './PlaygroundLayout';
import type { MenuItem } from './components/SideMenu';
import { DEMOS, MENU, DemoId } from './demos';

type Crumb = { label: string; to?: string };

function useDemoSelection(defaultId: DemoId): [DemoId, (id: DemoId) => void] {
    const nav = useNavigate();
    const loc = useLocation();
    const params = new URLSearchParams(loc.search);
    const initial = (params.get('demo') as DemoId) || defaultId;

    const [active, setActive] = useState<DemoId>(
        (MENU as MenuItem[]).some((m) => m.id === initial) ? initial : defaultId
    );

    useEffect(() => {
        const p = new URLSearchParams(loc.search);
        if (p.get('demo') !== active) {
            p.set('demo', active);
            nav({ search: `?${p.toString()}` }, { replace: true });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [active]);

    useEffect(() => {
        const p = new URLSearchParams(loc.search);
        const d = (p.get('demo') as DemoId) || defaultId;
        if ((MENU as MenuItem[]).some((m) => m.id === d) && d !== active) setActive(d);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [loc.search]);

    return [active, (id: DemoId) => setActive(id)];
}

export default function UiPlayground() {
    const nav = useNavigate();
    const location = useLocation();
    const [active, setActive] = useDemoSelection('buttons');

    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const activeLabel =
        (MENU as MenuItem[]).find((m) => m.id === active)?.label ?? 'UI Playground';

    const breadcrumbItems: Crumb[] = fromMaintainer
        ? [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: activeLabel },
        ]
        : [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: activeLabel },
        ];

    const ActiveDemo = DEMOS[active];

    return (
        <>
            <div className="px-4 md:px-6 lg:px-8 pt-2 pb-4">
                <div className="-ml-4 md:-ml-6 lg:-ml-8 -mt-1">
                    <Breadcrumb items={breadcrumbItems as any} />
                </div>
            </div>

            <PlaygroundLayout
                items={MENU}
                activeId={active}
                onSelect={(id) => setActive(id as DemoId)}
            >
                <Suspense fallback={<div className="text-sm text-slate-500">Loading…</div>}>
                    <ActiveDemo />
                </Suspense>

                <div className="flex justify-end border-t border-gray-200 pt-4 mt-6">
                    {fromMaintainer ? (
                        <button
                            onClick={() => nav('/mantenedor')}
                            className="text-sm text-[#003865] hover:underline cursor-pointer"
                        >
                            ← Volver al Mantenedor
                        </button>
                    ) : (
                        <button
                            onClick={() => nav(-1)}
                            className="text-sm text-[#003865] hover:underline cursor-pointer"
                        >
                            Volver
                        </button>
                    )}
                </div>
            </PlaygroundLayout>
        </>
    );
}
