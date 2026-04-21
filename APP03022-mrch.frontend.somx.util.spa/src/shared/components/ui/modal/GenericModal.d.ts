import { FC } from 'react';

type Variant = 'loading' | 'alert' | 'confirm';
type Severity = 'success' | 'error' | 'warning' | 'info';

interface GenericModalProps {
    visible?: boolean;
    variant?: Variant;
    message?: string;
    title?: string;
    messageConfirm?: string;
    severity?: Severity;
    buttonText?: string;
    confirmText?: string;
    cancelText?: string;
    onClose?: () => void;
    onConfirm?: () => void;
    onCancel?: () => void;
}

declare const GenericModal: FC<GenericModalProps>;
export default GenericModal;
