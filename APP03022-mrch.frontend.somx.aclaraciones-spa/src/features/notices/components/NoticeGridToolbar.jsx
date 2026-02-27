import { GenericInput } from '@shared/components/ui';

export default function NoticeGridToolbar({ search, onSearchInput }) {
    return (
        <div
            style={{
                display: 'flex',
                flex: 1,
                alignItems: 'center',
                gap: '1rem',
                marginBottom: '1.5rem',
            }}
        >
            <div
                style={{
                    flex: 1,
                    maxWidth: '32rem', // equivalente a max-w-xl
                }}
            >
                <GenericInput
                    label="Buscar"
                    value={search}
                    onChange={(e) => onSearchInput(e)}
                    placeholder="Escribe para filtrar…"
                    maxLength={100}
                />
            </div>
        </div>
    );
}
