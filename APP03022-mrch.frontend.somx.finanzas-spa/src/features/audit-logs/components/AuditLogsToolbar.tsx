// src/features/audit-logs/components/AuditLogsToolbar.tsx
import { GenericButton } from '@shared/components/ui';
import '../styles/AuditLogsToolbar.css';

interface Props {
    onExportCsv: () => void;
    disabled: boolean;
}

export default function AuditLogsToolbar({ onExportCsv, disabled }: Props) {
    return (
        <div className="al-toolbar">
            <GenericButton onClick={onExportCsv} disabled={disabled}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                        <path d="M6 2h9l5 5v15H6z" stroke="currentColor" strokeWidth="2" />
                        <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" />
                        <text x="7" y="20" fontSize="8" fill="currentColor">CSV</text>
                    </svg>
                    Export CSV
                </span>
            </GenericButton>
        </div>
    );
}