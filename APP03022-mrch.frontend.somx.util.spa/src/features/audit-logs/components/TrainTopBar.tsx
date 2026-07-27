export default function TrainTopBar({
    title,
    onBack,
}: Readonly<{
    title: string;
    onBack: () => void;
}>) {
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