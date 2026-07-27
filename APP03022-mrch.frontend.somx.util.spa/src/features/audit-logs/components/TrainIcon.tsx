type TrainKind = 'success' | 'error' | 'info' | 'alerta';

const GLYPH_BY_KIND: Record<TrainKind, string> = {
    success: '✓',
    error: '✕',
    alerta: '!',
    info: 'i',
};

const CLASS_BY_KIND: Record<TrainKind, string> = {
    success: 'alt-node alt-node-success',
    error: 'alt-node alt-node-error',
    alerta: 'alt-node alt-node-alerta',
    info: 'alt-node alt-node-info',
};

export default function TrainIcon({ kind }: Readonly<{ kind: TrainKind }>) {
    return (
        <span className={CLASS_BY_KIND[kind]} aria-hidden="true">
            <span className="alt-nodeInner">{GLYPH_BY_KIND[kind]}</span>
        </span>
    );
}
