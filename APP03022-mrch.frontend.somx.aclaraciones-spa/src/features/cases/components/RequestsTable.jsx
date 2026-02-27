// src/components/pages/requests/RequestsTable.jsx
import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import deleteIcon from '@assets/delete.svg';
import eyeIcon from '@assets/eye-show.svg';
import { useAppSelector } from '@/store/hooks/useAppSelector';

export default function RequestsTable({
    rows,
    categories,
    emptyLabel,
    perPage,
    page,
    totalPages,
    onChangePerPage,
    onChangePage,
    onShow,
    onDelete,
}) {
    const navigate = useNavigate();

    // ===== Roles desde el token (fuente de la verdad) =====
    const roles =
        useAppSelector(
            (s) =>
                s.authentication?.tokenDecoded?.resource_access?.['fbc-aclaraciones']?.roles
        ) || [];

    // Solo admin puede eliminar
    const canDelete = Array.isArray(roles) && roles.includes('ppsomx-admin');

    const categoryOf = (id) =>
        Array.isArray(categories) ? categories.find((c) => c.id === id) : null;

    // === Helpers de paginación ===
    const clampedPage = Math.min(
        Math.max(1, Number(page || 1)),
        Math.max(1, Number(totalPages || 1))
    );
    const canPrev = clampedPage > 1;
    const canNext = clampedPage < totalPages;

    const pagesToRender = useMemo(() => {
        const tp = Math.max(1, Number(totalPages || 1));
        const current = Math.min(Math.max(1, Number(page || 1)), tp);

        // Ventana de 5 páginas (… 3 4 [5] 6 7 …)
        const windowSize = 5;
        let start = Math.max(1, current - Math.floor(windowSize / 2));
        let end = start + windowSize - 1;
        if (end > tp) {
            end = tp;
            start = Math.max(1, end - windowSize + 1);
        }
        const arr = [];
        for (let p = start; p <= end; p++) arr.push(p);
        return arr;
    }, [page, totalPages]);

    const goto = (p) => {
        const tp = Math.max(1, Number(totalPages || 1));
        const np = Math.min(Math.max(1, Number(p || 1)), tp);
        onChangePage?.(np);
    };

    return (
        <div className="overflow-x-auto mt-8">
            <table className="min-w-full text-sm">
                <thead className="bg-[#eaf5fc] text-[#002d4c] font-medium">
                    <tr>
                        <th className="py-3 px-4 text-left">Orden</th>
                        <th className="py-3 px-4 text-left">Fecha creación</th>
                        <th className="py-3 px-4 text-left">Proveedor</th>
                        <th className="py-3 px-4 text-left">Categoría</th>
                        <th className="py-3 px-4 text-left">Estado</th>
                        <th className="py-3 px-4 text-center">Acciones</th>
                    </tr>
                </thead>

                <tbody>
                    {rows.length === 0 && (
                        <tr>
                            <td colSpan={6} className="py-6 text-center text-gray-500">
                                {emptyLabel}
                            </td>
                        </tr>
                    )}

                    {rows.map((r) => {
                        const cat = categoryOf(r.reasonId);
                        return (
                            <tr key={r.id} className="border-t border-gray-200 hover:bg-gray-50">
                                <td className="py-3 px-4">{r.orderId}</td>
                                <td className="py-3 px-4">{r.creationTime}</td>
                                <td className="py-3 px-4">{r.requester}</td>
                                <td className="py-3 px-4">{cat?.description || r.reason || '—'}</td>
                                <td
                                    className="py-3 px-4"
                                    dangerouslySetInnerHTML={{ __html: r.statusPill }}
                                />
                                <td className="py-3 px-4">
                                    <div className="flex justify-center gap-4 text-[#002d4c]">
                                        <button
                                            title="Ver"
                                            aria-label={`Ver solicitud ${r.orderId}`}
                                            onClick={() => onShow(r.id)}
                                            className="cursor-pointer hover:opacity-80"
                                        >
                                            <img src={eyeIcon} alt="Ver" className="w-5 h-5" />
                                        </button>

                                        {canDelete && (
                                            <button
                                                title="Eliminar"
                                                aria-label={`Eliminar solicitud ${r.orderId}`}
                                                onClick={() => onDelete(r.id)}
                                                className="cursor-pointer hover:opacity-80"
                                            >
                                                <img src={deleteIcon} alt="Eliminar" className="w-5 h-5" />
                                            </button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>

            {/* === Paginación === */}
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 py-4 text-sm">
                {/* Per Page */}
                <div className="flex items-center gap-2">
                    <span className="text-gray-600">Filas por página:</span>
                    <select
                        value={perPage}
                        onChange={(e) => onChangePerPage?.(Number(e.target.value))}
                        className="border border-gray-300 rounded px-2 py-1 cursor-pointer"
                    >
                        {[5, 10, 20, 50].map((n) => (
                            <option key={n} value={n}>
                                {n}
                            </option>
                        ))}
                    </select>
                    <span className="text-gray-500 hidden sm:inline">
                        Página <strong>{clampedPage}</strong> de{' '}
                        <strong>{Math.max(1, totalPages)}</strong>
                    </span>
                </div>

                {/* Pager */}
                <div className="flex items-center gap-1">
                    <button
                        className="px-2 py-1 border border-gray-300 rounded disabled:opacity-50 cursor-pointer"
                        onClick={() => goto(1)}
                        disabled={!canPrev}
                        aria-label="Primera página"
                        title="Primera página"
                    >
                        «
                    </button>
                    <button
                        className="px-2 py-1 border border-gray-300 rounded disabled:opacity-50 cursor-pointer"
                        onClick={() => goto(clampedPage - 1)}
                        disabled={!canPrev}
                        aria-label="Página anterior"
                        title="Anterior"
                    >
                        ‹
                    </button>

                    {pagesToRender[0] > 1 && (
                        <button
                            className="px-3 py-1 border border-gray-300 rounded cursor-pointer"
                            onClick={() => goto(pagesToRender[0] - 1)}
                            title={`Ir a ${pagesToRender[0] - 1}`}
                        >
                            …
                        </button>
                    )}

                    {pagesToRender.map((p) => (
                        <button
                            key={p}
                            onClick={() => goto(p)}
                            className={`px-3 py-1 border rounded cursor-pointer ${p === clampedPage
                                ? 'bg-[#003865] text-white border-[#003865]'
                                : 'border-gray-300 hover:bg-gray-100'
                                }`}
                            aria-current={p === clampedPage ? 'page' : undefined}
                            title={`Ir a ${p}`}
                        >
                            {p}
                        </button>
                    ))}

                    {pagesToRender[pagesToRender.length - 1] < Math.max(1, totalPages) && (
                        <button
                            className="px-3 py-1 border border-gray-300 rounded cursor-pointer"
                            onClick={() => goto(pagesToRender[pagesToRender.length - 1] + 1)}
                            title={`Ir a ${pagesToRender[pagesToRender.length - 1] + 1
                                }`}
                        >
                            …
                        </button>
                    )}

                    <button
                        className="px-2 py-1 border border-gray-300 rounded disabled:opacity-50 cursor-pointer"
                        onClick={() => goto(clampedPage + 1)}
                        disabled={!canNext}
                        aria-label="Página siguiente"
                        title="Siguiente"
                    >
                        ›
                    </button>
                    <button
                        className="px-2 py-1 border border-gray-300 rounded disabled:opacity-50 cursor-pointer"
                        onClick={() => goto(totalPages)}
                        disabled={!canNext}
                        aria-label="Última página"
                        title="Última página"
                    >
                        »
                    </button>
                </div>
            </div>

            <div className="border-t border-gray-200" />
        </div>
    );
}
