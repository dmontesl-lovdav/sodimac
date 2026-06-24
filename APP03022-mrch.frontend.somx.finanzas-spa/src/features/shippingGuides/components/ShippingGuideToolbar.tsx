import { GenericButton } from "@shared/components/ui";

interface Props {
  onExportCsv: () => void;
  onExportXlsx: () => void;
  onCancelSelection?: () => void;
  disabled: boolean;
  cancelDisabled?: boolean;
}

/** Icono “cerrar / cancelar operación” para no cancelar la guía por error */
function CancelIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M18 6L6 18M6 6l12 12"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
}

export default function ShippingGuideToolbar({
  onExportCsv,
  onExportXlsx,
  onCancelSelection,
  disabled,
  cancelDisabled = true,
}: Props) {
  return (
    <div className="twm-toolbar twm-toolbar-cluster">
      {onCancelSelection && (
        <GenericButton
          type="button"
          variant="outlineFill"
          onClick={onCancelSelection}
          disabled={cancelDisabled}
        >
          <span
            style={{ display: "flex", alignItems: "center", gap: 8 }}
          >
            <CancelIcon />
            Cancelar
          </span>
        </GenericButton>
      )}

      <GenericButton
        type="button"
        variant="outlineFill"
        onClick={onExportXlsx}
        disabled={disabled}
      >
        <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path
              d="M6 2h9l5 5v15H6z"
              stroke="currentColor"
              strokeWidth="2"
            />
            <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" />
            <text x="6" y="20" fontSize="7" fill="currentColor">
              XLS
            </text>
          </svg>
          Exportar Excel
        </span>
      </GenericButton>

      <GenericButton
        type="button"
        variant="primary"
        onClick={onExportCsv}
        disabled={disabled}
      >
        <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path
              d="M6 2h9l5 5v15H6z"
              stroke="currentColor"
              strokeWidth="2"
            />
            <path d="M14 2v6h6" stroke="currentColor" strokeWidth="2" />
            <text x="7" y="20" fontSize="8" fill="currentColor">
              CSV
            </text>
          </svg>
          Exportar CSV
        </span>
      </GenericButton>
    </div>
  );
}
