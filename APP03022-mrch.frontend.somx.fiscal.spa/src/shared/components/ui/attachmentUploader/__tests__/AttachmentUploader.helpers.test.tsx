/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect, beforeEach, afterEach } from "@jest/globals";
import { createRoot } from "react-dom/client";
import { act } from "react";
import React from "react";

import AttachmentUploader, { buildErr, formatAttachmentSize } from "../AttachmentUploader";
import { ERR_INVALID_TYPE, ERR_INVALID_SIZE, ERR_TOO_LONG_FILENAME } from "../attachmentHelpers";

describe("buildErr / formatAttachmentSize", () => {
  it("cubre mensajes de error y tamaños", () => {
    expect(buildErr(ERR_INVALID_TYPE)).toBeTruthy();
    expect(buildErr(ERR_INVALID_SIZE)).toBeTruthy();
    expect(buildErr(ERR_TOO_LONG_FILENAME)).toBeTruthy();
    expect(buildErr(undefined)).toBeNull();
    expect(formatAttachmentSize(0)).toContain("B");
    expect(formatAttachmentSize(1024)).toContain("KB");
    expect(formatAttachmentSize(Number.NaN)).toBe("");
  });
});

describe("AttachmentUploader mount", () => {
  let container: HTMLDivElement;
  let root: ReturnType<typeof createRoot>;

  beforeEach(() => {
    Object.defineProperty(URL, "createObjectURL", {
      value: jest.fn(() => "blob:mock"),
      writable: true,
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      value: jest.fn(),
      writable: true,
    });
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("renderiza zona de carga y lista de archivos", async () => {
    const setFiles = jest.fn();
    const file = new File(["x"], "doc.pdf", { type: "application/pdf" });
    await act(async () => {
      root.render(
        React.createElement(AttachmentUploader, {
          files: [file as any],
          setFiles,
          multiple: true,
          fileExtensions: ["pdf", "xml"],
        })
      );
    });
    expect(container.textContent).toContain("Arrastra");
    expect(container.textContent).toContain("doc.pdf");
  });

  it("deshabilita acciones cuando multiple=false y ya hay archivo", async () => {
    const file = new File(["x"], "a.pdf", { type: "application/pdf" });
    await act(async () => {
      root.render(
        React.createElement(AttachmentUploader, {
          files: [file as any],
          setFiles: jest.fn(),
          multiple: false,
          fileExtensions: ["pdf"],
        })
      );
    });
    const input = container.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input.disabled).toBe(true);
  });
});
