import { AxiosError, AxiosHeaders } from 'axios';
import { extractApiErrorMessage } from './errorMessage';

const buildAxiosError = (
  status?: number,
  data?: unknown,
  code?: string,
): AxiosError => {
  const err = new AxiosError(
    `Request failed with status code ${status ?? 0}`,
    code,
    { headers: new AxiosHeaders() } as any,
    null,
    status
      ? ({
          status,
          statusText: '',
          data,
          headers: {},
          config: { headers: new AxiosHeaders() } as any,
        } as any)
      : undefined,
  );
  return err;
};

describe('extractApiErrorMessage', () => {
  it('devuelve el mensaje friendly cuando el backend manda un message limpio', () => {
    const err = buildAxiosError(400, { message: 'El RUT ingresado ya existe' });
    expect(extractApiErrorMessage(err)).toBe('El RUT ingresado ya existe');
  });

  it('usa el mensaje por status cuando el backend manda un mensaje técnico', () => {
    const err = buildAxiosError(401, { message: 'Request failed with status code 401' });
    expect(extractApiErrorMessage(err)).toContain('sesión');
  });

  it('mapea status conocidos a mensajes friendly', () => {
    expect(extractApiErrorMessage(buildAxiosError(403))).toContain('permisos');
    expect(extractApiErrorMessage(buildAxiosError(404))).toContain('No encontramos');
    expect(extractApiErrorMessage(buildAxiosError(500))).toContain('servidor');
    expect(extractApiErrorMessage(buildAxiosError(503))).toContain('disponible');
  });

  it('maneja errores de red (ECONNABORTED / ERR_NETWORK)', () => {
    const err = buildAxiosError(undefined, undefined, 'ERR_NETWORK');
    expect(extractApiErrorMessage(err)).toContain('conectar');
  });

  it('cae al fallback para errores axios sin status conocido', () => {
    const err = buildAxiosError(418);
    expect(extractApiErrorMessage(err)).toContain('completar la operación');
  });

  it('permite override del fallback', () => {
    const err = buildAxiosError(418);
    expect(extractApiErrorMessage(err, { fallback: 'custom' })).toBe('custom');
  });

  it('devuelve el message de un Error nativo si no es técnico', () => {
    expect(extractApiErrorMessage(new Error('Validación falló'))).toBe('Validación falló');
  });

  it('cae al fallback si el Error nativo es técnico', () => {
    expect(extractApiErrorMessage(new Error('axios timeout'))).toContain(
      'completar la operación',
    );
  });

  it('lee message de objetos sueltos con message string', () => {
    expect(extractApiErrorMessage({ message: 'algo falló de forma legible' })).toBe(
      'algo falló de forma legible',
    );
  });

  it('cae al fallback para valores desconocidos (string, number, null)', () => {
    expect(extractApiErrorMessage(null)).toContain('completar la operación');
    expect(extractApiErrorMessage(42)).toContain('completar la operación');
    expect(extractApiErrorMessage('texto suelto')).toContain('completar la operación');
  });
});
