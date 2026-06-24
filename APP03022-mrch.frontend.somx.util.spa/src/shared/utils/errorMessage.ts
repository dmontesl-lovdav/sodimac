import axios from 'axios';

interface FriendlyMessageOptions {
  fallback?: string;
}

const DEFAULT_FALLBACK =
  'No fue posible completar la operación. Por favor intenta de nuevo más tarde.';

const STATUS_FRIENDLY: Record<number, string> = {
  400: 'La información ingresada no es válida. Revisa los campos del formulario e inténtalo nuevamente.',
  401: 'Tu sesión expiró o no tienes acceso a esta operación. Vuelve a iniciar sesión.',
  403: 'No cuentas con permisos para realizar esta acción.',
  404: 'No encontramos el registro solicitado. Es posible que haya sido eliminado.',
  409: 'Ya existe un registro con la misma información.',
  413: 'El archivo es demasiado grande. Reduce su tamaño e inténtalo de nuevo.',
  422: 'La información ingresada no cumple con las validaciones del sistema.',
  500: 'Ocurrió un error en el servidor. Intenta nuevamente en unos minutos.',
  502: 'El servicio no responde por el momento. Intenta nuevamente en unos minutos.',
  503: 'El servicio no está disponible por el momento. Intenta nuevamente en unos minutos.',
  504: 'El servicio tardó demasiado en responder. Intenta nuevamente en unos minutos.',
};

const looksTechnical = (msg: string): boolean => {
  if (!msg) return true;
  const lower = msg.toLowerCase();
  return (
    lower.includes('request failed with status code') ||
    lower.includes('network error') ||
    lower.includes('timeout') ||
    lower.includes('axios') ||
    lower.includes('econn') ||
    lower.includes('undefined') ||
    lower.includes('null') ||
    /^error:/i.test(msg) ||
    msg.length > 220
  );
};

export const extractApiErrorMessage = (
  error: unknown,
  options: FriendlyMessageOptions = {},
): string => {
  const fallback = options.fallback ?? DEFAULT_FALLBACK;

  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const data = error.response?.data as
      | { message?: string; error?: string; detail?: string; errors?: unknown }
      | undefined;

    const backendMsg =
      (typeof data?.message === 'string' && data.message) ||
      (typeof data?.error === 'string' && data.error) ||
      (typeof data?.detail === 'string' && data.detail) ||
      '';

    if (backendMsg && !looksTechnical(backendMsg)) {
      return backendMsg;
    }

    if (status && STATUS_FRIENDLY[status]) {
      return STATUS_FRIENDLY[status];
    }

    if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
      return 'No fue posible conectar con el servicio. Verifica tu conexión e inténtalo nuevamente.';
    }

    return fallback;
  }

  if (error instanceof Error) {
    if (!looksTechnical(error.message)) return error.message;
    return fallback;
  }

  if (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof (error as { message: unknown }).message === 'string'
  ) {
    const msg = (error as { message: string }).message;
    if (!looksTechnical(msg)) return msg;
  }

  return fallback;
};
