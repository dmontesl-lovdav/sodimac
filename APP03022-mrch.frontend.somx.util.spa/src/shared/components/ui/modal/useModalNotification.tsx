import { useCallback, useState, useMemo } from 'react';
import GenericModal from './GenericModal.jsx';

type Severity = 'info' | 'success' | 'warning' | 'error';

interface AlertState {
  visible: boolean;
  title?: string;
  message: string;
  severity: Severity;
  buttonText?: string;
  items?: string[];
  onClose?: () => void;
}

interface ConfirmState {
  visible: boolean;
  title?: string;
  message: string;
  severity: Severity;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
}

const DEFAULT_ALERT: AlertState = {
  visible: false,
  message: '',
  severity: 'info',
};

const DEFAULT_CONFIRM: ConfirmState = {
  visible: false,
  message: '',
  severity: 'info',
};

export interface AlertOptions {
  title?: string;
  message: string;
  severity?: Severity;
  buttonText?: string;
  items?: string[];
  onClose?: () => void;
}

export interface ErrorListOptions {
  title?: string;
  message?: string;
  items: string[];
  buttonText?: string;
  onClose?: () => void;
}

export interface ConfirmOptions {
  title?: string;
  message: string;
  severity?: Severity;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
}

export function useModalNotification() {
  const [alertState, setAlertState] = useState<AlertState>(DEFAULT_ALERT);
  const [confirmState, setConfirmState] = useState<ConfirmState>(DEFAULT_CONFIRM);

  const showAlert = useCallback((opts: AlertOptions) => {
    setAlertState({
      visible: true,
      title: opts.title,
      message: opts.message,
      severity: opts.severity ?? 'info',
      buttonText: opts.buttonText ?? 'Aceptar',
      items: opts.items,
      onClose: opts.onClose,
    });
  }, []);

  const showErrorList = useCallback((opts: ErrorListOptions) => {
    const count = opts.items?.length ?? 0;
    setAlertState({
      visible: true,
      title: opts.title ?? 'Errores en el archivo',
      message:
        opts.message ??
        (count === 1
          ? 'Se encontró 1 error. Corrige el archivo e intenta nuevamente.'
          : `Se encontraron ${count} errores. Corrige el archivo e intenta nuevamente.`),
      severity: 'error',
      buttonText: opts.buttonText ?? 'Aceptar',
      items: opts.items,
      onClose: opts.onClose,
    });
  }, []);

  const showSuccess = useCallback((message: string, title?: string, onClose?: () => void) => {
    setAlertState({
      visible: true,
      title: title ?? 'Operación exitosa',
      message,
      severity: 'success',
      buttonText: 'Aceptar',
      onClose,
    });
  }, []);

  const showError = useCallback((message: string, title?: string, onClose?: () => void) => {
    setAlertState({
      visible: true,
      title: title ?? 'Error',
      message,
      severity: 'error',
      buttonText: 'Aceptar',
      onClose,
    });
  }, []);

  const showWarning = useCallback((message: string, title?: string, onClose?: () => void) => {
    setAlertState({
      visible: true,
      title: title ?? 'Advertencia',
      message,
      severity: 'warning',
      buttonText: 'Aceptar',
      onClose,
    });
  }, []);

  const showConfirm = useCallback((opts: ConfirmOptions) => {
    setConfirmState({
      visible: true,
      title: opts.title,
      message: opts.message,
      severity: opts.severity ?? 'info',
      confirmText: opts.confirmText ?? 'Confirmar',
      cancelText: opts.cancelText ?? 'Cancelar',
      onConfirm: opts.onConfirm,
    });
  }, []);

  const closeAlert = useCallback(() => {
    const cb = alertState.onClose;
    setAlertState((s) => ({ ...s, visible: false, items: undefined, onClose: undefined }));
    if (cb) cb();
  }, [alertState.onClose]);

  const closeConfirm = useCallback(() => {
    setConfirmState((s) => ({ ...s, visible: false, onConfirm: undefined }));
  }, []);

  const handleConfirm = useCallback(() => {
    const cb = confirmState.onConfirm;
    closeConfirm();
    if (cb) cb();
  }, [confirmState.onConfirm, closeConfirm]);

  const ModalNode = useMemo(
    () => (
      <>
        <GenericModal
          visible={alertState.visible}
          variant="alert"
          severity={alertState.severity}
          title={alertState.title}
          message={alertState.message}
          items={alertState.items}
          buttonText={alertState.buttonText}
          onClose={closeAlert}
        />
        <GenericModal
          visible={confirmState.visible}
          variant="confirm"
          severity={confirmState.severity}
          title={confirmState.title}
          message={confirmState.message}
          confirmText={confirmState.confirmText}
          cancelText={confirmState.cancelText}
          onCancel={closeConfirm}
          onConfirm={handleConfirm}
        />
      </>
    ),
    [alertState, confirmState, closeAlert, closeConfirm, handleConfirm],
  );

  return {
    showAlert,
    showSuccess,
    showError,
    showWarning,
    showConfirm,
    showErrorList,
    ModalNode,
  };
}
