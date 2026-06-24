export default function TrainIcon({
    kind,
}: {
    kind: 'success' | 'error' | 'info' | 'alerta';
}) {
    const glyph =
        kind === 'success' ? '✓' : kind === 'error' ? '✕' : kind === 'alerta' ? '!' : 'i';

    return (
        <span
            className={
                kind === 'success'
                    ? 'alt-node alt-node-success'
                    : kind === 'error'
                        ? 'alt-node alt-node-error'
                        : kind === 'alerta'
                            ? 'alt-node alt-node-alerta'
                            : 'alt-node alt-node-info'
            }
            aria-hidden="true"
        >
            <span className="alt-nodeInner">{glyph}</span>
        </span>
    );
}