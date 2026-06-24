import { useCallback, useState } from 'react';
import { getErrorMessage } from '@/utils/errorMessage';

export type FinanceAlertSeverity = 'success' | 'error' | 'warning' | 'info';

type AlertState = {
  visible: boolean;
  severity: FinanceAlertSeverity;
  title: string;
  message: string;
};

export function useFinanceAlertModal(initial?: Partial<Omit<AlertState, 'visible'>>) {
  const [state, setState] = useState<AlertState>({
    visible: false,
    severity: initial?.severity ?? 'error',
    title: initial?.title ?? '',
    message: initial?.message ?? '',
  });

  const closeAlert = useCallback(() => {
    setState((prev) => ({ ...prev, visible: false }));
  }, []);

  const open = useCallback((opts: { severity?: FinanceAlertSeverity; title: string; message: string }) => {
    setState({
      visible: true,
      severity: opts.severity ?? 'error',
      title: opts.title,
      message: opts.message,
    });
  }, []);

  const showError = useCallback(
    (title: string, message: string) => open({ severity: 'error', title, message }),
    [open],
  );
  const showWarning = useCallback(
    (title: string, message: string) => open({ severity: 'warning', title, message }),
    [open],
  );
  const showInfo = useCallback(
    (title: string, message: string) => open({ severity: 'info', title, message }),
    [open],
  );
  const showSuccess = useCallback(
    (title: string, message: string) => open({ severity: 'success', title, message }),
    [open],
  );

  const showErrorFrom = useCallback(
    (title: string, err: unknown, fallback?: string) =>
      open({ severity: 'error', title, message: getErrorMessage(err, fallback) }),
    [open],
  );

  return {
    alertVisible: state.visible,
    alertSeverity: state.severity,
    alertTitle: state.title,
    alertMessage: state.message,
    closeAlert,
    open,
    showError,
    showWarning,
    showInfo,
    showSuccess,
    showErrorFrom,
  };
}
