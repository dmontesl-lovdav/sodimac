declare module "@shared/components/ui/modal/GenericModal" {
    import { FC } from "react";

    interface Props {
        visible?: boolean;
        variant?: "loading" | "alert";
        message?: string;
        title?: string;
        severity?: "success" | "error" | "warning" | "info";
        buttonText?: string;
        onClose?: () => void;
    }

    const GenericModal: FC<Props>;
    export default GenericModal;
}
