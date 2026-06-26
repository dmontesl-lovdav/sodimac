import { GenericButton } from '@shared/components/ui';
import { APP_EVENT, PermissionGate } from '@shared/security';

interface Props {
    onExportCsv: () => void;
    onExportXlsx: () => void;
    disabled: boolean;
}

export default function ThreeWayMatchToolbar({
    onExportCsv,
    onExportXlsx,
    disabled
}: Props) {

    return (
        <div className="twm-toolbar">
            

            <PermissionGate appEvent={APP_EVENT.THREE_WAY_MATCH.DOWNLOAD_XLS}>
                <GenericButton
                    variant="outline"
                    onClick={onExportXlsx}
                    disabled={disabled}
                >
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                            <path d="M6 2h9l5 5v15H6z" stroke="currentColor" strokeWidth="2" />
                            <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" />
                            <text x="6" y="20" fontSize="7" fill="currentColor">XLS</text>
                        </svg>
                        Exportar Excel
                    </span>
                </GenericButton>
            </PermissionGate>

            <PermissionGate appEvent={APP_EVENT.THREE_WAY_MATCH.DOWNLOAD_CSV}>
                <GenericButton
                    variant="primary"
                    onClick={onExportCsv}
                    disabled={disabled}
                >
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                            <path d="M6 2h9l5 5v15H6z" stroke="currentColor" strokeWidth="2" />
                            <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" />
                            <text x="7" y="20" fontSize="8" fill="currentColor">CSV</text>
                        </svg>
                        Exportar CSV
                    </span>
                </GenericButton>
            </PermissionGate>
        </div>
    );
}