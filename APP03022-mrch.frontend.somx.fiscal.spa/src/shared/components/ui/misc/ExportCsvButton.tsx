import type { ReactElement } from "react";
import { GenericButton } from "@/shared/components/ui";
import downloadIconUrl from "@assets/download.svg";
import "./ExportCsvButton.css";

type ExportCsvButtonProps = {
  onClick: () => void;
  disabled?: boolean;
};

export function ExportCsvButton({
  onClick,
  disabled = false,
}: ExportCsvButtonProps): ReactElement {
  return (
    <GenericButton variant="primary" onClick={onClick} disabled={disabled}>
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
