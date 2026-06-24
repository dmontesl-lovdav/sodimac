import { useCallback, useState } from 'react';
import GenericModal from '@shared/components/ui/modal/GenericModal';

export type AlertSeverity = 'success' | 'error' | 'info' | 'warning';

export interface ShowAlertOptions {
  title?: string;
  message: string;
  severity?: AlertSeverity;
}

/** Mensaje legible a partir de errores de fetch, Error u objetos típicos de API. */
export function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) return error.message;
  if (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof (error as { message: unknown }).message === 'string'
  ) {
    return (error as { message: string }).message;
  }
  return fallback;
}

/**
 * Modal de alerta tipo UtilContainer (variant alert + GenericModal).
 * Uso: `const { showAlert, alertModal } = useAlertModal();` y renderizar `{alertModal}` junto al árbol.
 */
export function useAlertModal() {
  const [visible, setVisible] = useState(false);
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [severity, setSeverity] = useState<AlertSeverity>('info');

  const showAlert = useCallback((opts: ShowAlertOptions) => {
    setTitle(opts.title ?? '');
    setMessage(opts.message);
    setSeverity(opts.severity ?? 'info');
    setVisible(true);
  }, []);

  const hideAlert = useCallback(() => {
    setVisible(false);
    setTitle('');
    setMessage('');
    setSeverity('info');
  }, []);

  const alertModal = (
    <GenericModal
      visible={visible}
      variant="alert"
      title={title}
      message={message}
      severity={severity}
      buttonText="Aceptar"
      onClose={hideAlert}
    />
  );

  return { showAlert, hideAlert, alertModal };
}
