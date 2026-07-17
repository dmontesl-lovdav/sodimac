/**
 * @jest-environment jsdom
 */
import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { createRoot } from "react-dom/client";
import { act } from "react-dom/test-utils";
import AttachmentUploader from "../AttachmentUploader";

describe("AttachmentUploader", () => {
  it("renderiza zona de carga", () => {
    const setFiles = jest.fn();
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);
    act(() => {
      root.render(
        React.createElement(AttachmentUploader, {
          files: [],
          setFiles,
          multiple: true,
        })
      );
    });
    expect(container.textContent).toContain("Arrastra y suelta");
    root.unmount();
    container.remove();
  });

  it("lista archivos y muestra error de extensión", () => {
    const bad = new File(["x"], "a.exe", { type: "application/octet-stream" });
    bad.err = 1;
    const setFiles = jest.fn();
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);
    act(() => {
      root.render(
        React.createElement(AttachmentUploader, {
          files: [bad],
          setFiles,
          multiple: true,
        })
      );
    });
    expect(container.textContent).toContain("a.exe");
    expect(container.textContent).toContain("Documento no soportado");
    root.unmount();
    container.remove();
  });
});
