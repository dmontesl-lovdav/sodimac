// catalog.ts
export const CLAZZ_TO_COLUMN: Record<number, 'todo' | 'doing' | 'done' | 'block'> = {
    24: 'todo',     // To do
    25: 'doing',    // Doing
    26: 'done',     // Done
    27: 'block',    // Bloqueo
};

export const BOARD_COLUMNS = [
    { id: 'todo', title: 'To do' },
    { id: 'doing', title: 'Doing' },
    { id: 'done', title: 'Done' },
    { id: 'block', title: 'Bloqueo' },
];
