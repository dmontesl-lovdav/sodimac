import { describe, it, expect } from "@jest/globals";
import { getErrorMessage } from "../errorMessage";

describe("getErrorMessage", () => {
  describe("entradas vacías / nulas", () => {
    it("devuelve el fallback cuando err es null", () => {
      expect(getErrorMessage(null)).toBe("Ocurrió un error inesperado.");
    });

    it("devuelve el fallback cuando err es undefined", () => {
      expect(getErrorMessage(undefined)).toBe("Ocurrió un error inesperado.");
    });

    it("acepta fallback personalizado con null", () => {
      expect(getErrorMessage(null, "Fallo personalizado")).toBe(
        "Fallo personalizado"
      );
    });

    it("devuelve el fallback cuando err es un número", () => {
      expect(getErrorMessage(42)).toBe("Ocurrió un error inesperado.");
    });
  });

  describe("err es string", () => {
    it("devuelve el string directamente si no está vacío", () => {
      expect(getErrorMessage("algo salió mal")).toBe("algo salió mal");
    });

    it("devuelve el string después de trim", () => {
      expect(getErrorMessage("  error con espacios  ")).toBe(
        "error con espacios"
      );
    });

    it("devuelve el fallback cuando el string es vacío", () => {
      expect(getErrorMessage("")).toBe("Ocurrió un error inesperado.");
    });

    it("devuelve el fallback cuando el string es solo espacios", () => {
      expect(getErrorMessage("   ")).toBe("Ocurrió un error inesperado.");
    });
  });

  describe("códigos de timeout", () => {
    it("identifica ECONNABORTED como timeout", () => {
      const err = { code: "ECONNABORTED" };
      expect(getErrorMessage(err)).toBe(
        "La solicitud tardó demasiado (tiempo de espera agotado). Intenta nuevamente."
      );
    });

    it("identifica ETIMEDOUT como timeout", () => {
      const err = { code: "ETIMEDOUT" };
      expect(getErrorMessage(err)).toBe(
        "La solicitud tardó demasiado (tiempo de espera agotado). Intenta nuevamente."
      );
    });
  });

  describe("Network Error", () => {
    it("devuelve mensaje de red cuando message es 'Network Error'", () => {
      const err = { message: "Network Error" };
      expect(getErrorMessage(err)).toBe(
        "No hay conexión con el servidor. Verifica tu red e intenta nuevamente."
      );
    });
  });

  describe("respuesta Axios con response.data string", () => {
    it("devuelve el texto del data cuando es string", () => {
      const err = { response: { data: "Error en el backend" } };
      expect(getErrorMessage(err)).toBe("Error en el backend");
    });

    it("ignora data vacío y cae al message", () => {
      const err = { response: { data: "   " }, message: "fallback message" };
      expect(getErrorMessage(err)).toBe("fallback message");
    });
  });

  describe("respuesta Axios con response.data objeto (API payload)", () => {
    it("extrae message del payload de la API", () => {
      const err = {
        response: {
          data: { message: "No se encontró el recurso" },
        },
      };
      expect(getErrorMessage(err)).toBe("No se encontró el recurso");
    });

    it("transforma ValidationError a mensaje amigable", () => {
      const err = {
        response: {
          data: { message: "ValidationError" },
        },
      };
      expect(getErrorMessage(err)).toBe(
        "Los datos enviados no son válidos. Revisa los campos e intenta de nuevo."
      );
    });

    it("extrae error del campo 'error'", () => {
      const err = {
        response: { data: { error: "Acceso denegado" } },
      };
      expect(getErrorMessage(err)).toBe("Acceso denegado");
    });

    it("extrae mensaje del campo 'detail'", () => {
      const err = {
        response: { data: { detail: "Detalle del error" } },
      };
      expect(getErrorMessage(err)).toBe("Detalle del error");
    });

    it("extrae primer string del array 'errors'", () => {
      const err = {
        response: { data: { errors: ["Campo requerido", "Formato inválido"] } },
      };
      expect(getErrorMessage(err)).toBe("Campo requerido");
    });
  });

  describe("detailError (formato Zod / validación)", () => {
    it("combina path y message de filas de detailError", () => {
      const err = {
        response: {
          data: {
            detailError: [
              { path: "nombre", message: "Es requerido" },
              { path: "rfc", message: "Formato inválido" },
            ],
          },
        },
      };
      expect(getErrorMessage(err)).toBe(
        "nombre: Es requerido\nrfc: Formato inválido"
      );
    });

    it("toma solo el message cuando no hay path", () => {
      const err = {
        response: {
          data: {
            detailError: [{ message: "Valor no permitido" }],
          },
        },
      };
      expect(getErrorMessage(err)).toBe("Valor no permitido");
    });

    it("acepta strings planos en detailError", () => {
      const err = {
        response: {
          data: {
            detailError: ["Error genérico de validación"],
          },
        },
      };
      expect(getErrorMessage(err)).toBe("Error genérico de validación");
    });

    it("toma solo el path cuando no hay message", () => {
      const err = {
        response: {
          data: {
            detailError: [{ path: "campoX" }],
          },
        },
      };
      expect(getErrorMessage(err)).toBe("campoX");
    });

    it("ignora detailError vacío y usa message", () => {
      const err = {
        response: {
          data: {
            detailError: [],
            message: "Otro mensaje",
          },
        },
      };
      expect(getErrorMessage(err)).toBe("Otro mensaje");
    });

    it("extrae errors como objeto anidado", () => {
      const err = {
        response: {
          data: {
            errors: [{ path: "a", message: "b" }],
          },
        },
      };
      expect(getErrorMessage(err)).toBe("a: b");
    });
  });

  describe("payload directo (sin response)", () => {
    it("extrae message de un objeto con httpStatus (API-like)", () => {
      const err = {
        message: "Error de negocio",
        httpStatus: 422,
        success: false,
      };
      expect(getErrorMessage(err)).toBe("Error de negocio");
    });

    it("extrae message de un objeto con detailError directo", () => {
      const err = {
        detailError: [{ path: "campo", message: "Requerido" }],
      };
      expect(getErrorMessage(err)).toBe("campo: Requerido");
    });
  });

  describe("status HTTP 504 / 408 (sin mensaje)", () => {
    it("devuelve mensaje de timeout cuando status es 504", () => {
      const err = { response: { status: 504 } };
      expect(getErrorMessage(err)).toBe(
        "Tiempo de espera agotado en el servidor. Intenta nuevamente."
      );
    });

    it("devuelve mensaje de timeout cuando status es 408", () => {
      const err = { response: { status: 408 } };
      expect(getErrorMessage(err)).toBe(
        "Tiempo de espera agotado en el servidor. Intenta nuevamente."
      );
    });
  });

  describe("message genérico del error", () => {
    it("devuelve el message del objeto cuando no hay data", () => {
      const err = { message: "Unexpected error occurred" };
      expect(getErrorMessage(err)).toBe("Unexpected error occurred");
    });
  });

  describe("objeto sin información útil", () => {
    it("devuelve el fallback cuando no hay campos reconocibles", () => {
      expect(getErrorMessage({ foo: "bar" })).toBe(
        "Ocurrió un error inesperado."
      );
    });

    it("devuelve fallback personalizado cuando no hay campos reconocibles", () => {
      expect(getErrorMessage({ foo: "bar" }, "Sin datos")).toBe("Sin datos");
    });
  });
});
