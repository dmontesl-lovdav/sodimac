import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { GenericTable, Switch, GenericButton } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

type Row = { id: number; name: string; active: boolean; country: string };

export default function TableDemo() {
    const nav = useNavigate();
    const [rows, setRows] = useState<Row[]>(
        Array.from({ length: 37 }, (_, i) => ({
            id: i + 1,
            name: `Usuario ${i + 1}`,
            active: i % 3 !== 0,
            country: ['MX', 'CL', 'PE', 'CO'][i % 4],
        }))
    );

    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);

    const paged = useMemo(() => {
        const start = (page - 1) * perPage;
        return rows.slice(start, start + perPage);
    }, [rows, page, perPage]);

    const totalPages = Math.max(1, Math.ceil(rows.length / perPage));

    const columns = [
        { header: 'ID', align: 'right' as const, render: (r: Row) => r.id },
        { header: 'Name', render: (r: Row) => r.name },
        {
            header: 'Active',
            align: 'center' as const,
            render: (r: Row) => (
                <Switch
                    on={r.active}
                    onClick={() =>
                        setRows(prev => prev.map(p => (p.id === r.id ? { ...p, active: !p.active } : p)))
                    }
                />
            ),
        },
        { header: 'Country', align: 'center' as const, render: (r: Row) => r.country },
    ];

    const actions = [
        {
            title: 'View',
            icon:
                'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" stroke="%23002d4c" fill="none" viewBox="0 0 24 24"><circle cx="12" cy="12" r="3" stroke-width="2"/><path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z" stroke-width="2"/></svg>',
            onClick: (row: Row) => nav(`/users/${row.id}`),
        },
    ];

    // 👇 Tip: Cast the component to avoid fighting with generic types.
    const GT: any = GenericTable;

    return (
        <DemoCard
            title="GenericTable"
            desc="Tabla con acciones y paginación simple."
            right={
                <GenericButton variant="link" onClick={() => setRows(r => r.slice().reverse())}>
                    Reverse data
                </GenericButton>
            }
        >
            <GT
                rows={paged as any}
                columns={columns as any}
                actions={actions as any}
                emptyLabel="No results"
                perPage={perPage as any}
                page={page as any}
                totalPages={totalPages as any}
                onChangePerPage={(n: number) => {
                    setPerPage(n);
                    setPage(1);
                }}
                onChangePage={(p: number) => setPage(Math.min(Math.max(1, p), totalPages))}
            />
        </DemoCard>
    );
}
