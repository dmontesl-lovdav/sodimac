import type { ReactElement } from "react";
import { GenericButton } from "@/shared/components/ui";
import downloadIconUrl from "@assets/download.svg";
import "./ExportCsvButton.css";

type ExportCsvButtonProps = {
  onClick: () => void;
  disabled?: boolean;
  variant?: "primary" | "secondary" | "outline" | "link" ;
};

export function ExportCsvButton({
  onClick,
  disabled = false,
  variant = "primary",
}: ExportCsvButtonProps): ReactElement {
  return (
    <GenericButton variant={variant} onClick={onClick} disabled={disabled}>
      <span className="fiscal-export-csv-content">
        <span
          className="fiscal-export-csv-icon"
          aria-hidden="true"
          style={{
            WebkitMaskImage: `url(${downloadIconUrl})`,
            maskImage: `url(${downloadIconUrl})`,
          }}
        />
        Exportar CSV
      </span>
    </GenericButton>
  );
}
