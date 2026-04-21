import { ReactElement } from "react";
import { GenericButton } from "@shared/components/ui";
import "./ConfirmDialog.css";

interface ConfirmDialogProps {
  visible: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  variant?: "primary" | "danger" | "warning";
}

export default function ConfirmDialog({
  visible,
  title,
  message,
  confirmText = "Aceptar",
  cancelText = "Cancelar",
  onConfirm,
  onCancel
}: ConfirmDialogProps): ReactElement | null {
  if (!visible) return null;

  return (
    <div className="confirm-dialog-backdrop">
      <div className="confirm-dialog-modal">
        <h3 className="confirm-dialog-title">{title}</h3>
        <p className="confirm-dialog-message">{message}</p>
        <div className="confirm-dialog-footer">
          <GenericButton
            variant="outline"
            className="somx-h-11"
            onClick={onCancel}
          >
            {cancelText}
          </GenericButton>
          <GenericButton
            variant="primary"
            className="somx-h-11"
            onClick={onConfirm}
          >
            {confirmText}
          </GenericButton>
        </div>
      </div>
    </div>
  );
}
