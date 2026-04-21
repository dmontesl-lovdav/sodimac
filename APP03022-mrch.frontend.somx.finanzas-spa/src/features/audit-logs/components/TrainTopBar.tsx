// ✅ FILE: src/features/audit-logs/components/TrainTopBar.tsx
import { GenericButton } from '@shared/components/ui';

export default function TrainTopBar({
    title,
    onBack,
}: {
    title: string;
    onBack: () => void;
}) {
    return (
        <div className="alt-topbar">
            <div className="alt-titleWrap">
                <h3 className="alt-title">{title}</h3>
            </div>

            {/* <div className="alt-actions">
                <GenericButton variant="outline" onClick={onBack}>
                    Volver
                </GenericButton>
            </div> */}
        </div>
    );
}