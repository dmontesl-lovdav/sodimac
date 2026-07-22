import { GenericModal } from ".";

interface ModalMsgProps {
    msg: string;
    title?: string;
    severity?: "info" | "warning" | "error" | "success";
    visible: boolean;
    onClose: () => void;
}

export const ModalMsg = ({ msg, title, severity, visible, onClose }: ModalMsgProps) => {
  return (
    <GenericModal
          visible={visible}
          variant="alert"
          title={title ?? "Atención"}
          message={msg}
          buttonText="Cerrar"
          onClose={onClose}
          severity={severity ?? "info"}
        />
  );
};