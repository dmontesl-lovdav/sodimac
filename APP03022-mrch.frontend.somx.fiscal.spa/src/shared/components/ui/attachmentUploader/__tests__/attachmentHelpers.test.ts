import { describe, it, expect } from "@jest/globals";
import { getFileError, ERR_INVALID_TYPE, ERR_INVALID_SIZE, ERR_TOO_LONG_FILENAME } from "../attachmentHelpers";

const VALID_EXTS = ["pdf", "xml", "jpg", "png"];
const MAX_SIZE = 4_194_304; // 4MB
const MAX_NAME_LEN = 64;

function makeFile(name: string, size = 1000): File {
  return new File(["x".repeat(Math.min(size, 1))], name, { type: "application/octet-stream" });
}

describe("getFileError", () => {
  it("retorna 0 para archivo válido", () => {
    const file = makeFile("documento.pdf");
    expect(getFileError(file, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(0);
  });

  it("retorna ERR_INVALID_TYPE para extensión no permitida", () => {
    const file = makeFile("documento.exe");
    expect(getFileError(file, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(ERR_INVALID_TYPE);
  });

  it("retorna ERR_INVALID_TYPE para archivo sin extensión", () => {
    const file = makeFile("documento");
    expect(getFileError(file, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(ERR_INVALID_TYPE);
  });

  it("retorna ERR_INVALID_SIZE cuando el archivo excede el tamaño", () => {
    const largeFile = { name: "big.pdf", size: MAX_SIZE + 1 } as File;
    expect(getFileError(largeFile, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(ERR_INVALID_SIZE);
  });

  it("retorna ERR_TOO_LONG_FILENAME cuando el nombre es demasiado largo", () => {
    const longName = "a".repeat(65) + ".pdf";
    const file = { name: longName, size: 100 } as File;
    expect(getFileError(file, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(ERR_TOO_LONG_FILENAME);
  });

  it("es case-insensitive para la extensión", () => {
    const file = { name: "DOCUMENTO.PDF", size: 100 } as File;
    expect(getFileError(file, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(0);
  });

  it("valida XML correctamente", () => {
    const file = makeFile("factura.xml");
    expect(getFileError(file, VALID_EXTS, MAX_SIZE, MAX_NAME_LEN)).toBe(0);
  });

  it("retorna ERR_INVALID_TYPE cuando la lista de extensiones está vacía", () => {
    const file = makeFile("doc.pdf");
    expect(getFileError(file, [], MAX_SIZE, MAX_NAME_LEN)).toBe(ERR_INVALID_TYPE);
  });
});
