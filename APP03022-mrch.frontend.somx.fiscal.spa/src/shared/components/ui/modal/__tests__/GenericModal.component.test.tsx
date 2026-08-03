/**
 * @jest-environment jsdom
 */
import { describe, it, expect, beforeEach, afterEach, jest } from "@jest/globals";
import { createRoot } from "react-dom/client";
import { act } from "react";
import React from "react";

import GenericModal from "../GenericModal";

describe("GenericModal", () => {
  let container: HTMLDivElement;
  let root: ReturnType<typeof createRoot>;

  beforeEach(() => {
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("no renderiza cuando visible=false", async () => {
    await act(async () => {
      root.render(React.createElement(GenericModal, { visible: false }));
    });
    expect(container.querySelector(".fiscal-modal-overlay")).toBeNull();
  });

  it("renderiza loading con mensaje", async () => {
    await act(async () => {
      root.render(
        React.createElement(GenericModal, {
          visible: true,
          variant: "loading",
          message: "Cargando…",
        })
      );
    });
    expect(container.querySelector(".fiscal-modal-spinner")).toBeTruthy();
    expect(container.textContent).toContain("Cargando…");
  });

  it("renderiza confirm y dispara onConfirm/onCancel", async () => {
    const onConfirm = jest.fn();
    const onCancel = jest.fn();
    await act(async () => {
      root.render(
        React.createElement(GenericModal, {
          visible: true,
          variant: "confirm",
          title: "Confirmar",
          message: "¿Continuar?",
          onConfirm,
          onCancel,
        })
      );
    });
    expect(container.querySelector(".fiscal-modal-title")?.textContent).toBe("Confirmar");
    const buttons = container.querySelectorAll("button");
    expect(buttons.length).toBe(2);
    await act(async () => {
      buttons[0].dispatchEvent(new MouseEvent("click", { bubbles: true }));
      buttons[1].dispatchEvent(new MouseEvent("click", { bubbles: true }));
    });
    expect(onCancel).toHaveBeenCalled();
    expect(onConfirm).toHaveBeenCalled();
  });

  it("renderiza alert success y usa onConfirm al aceptar", async () => {
    const onConfirm = jest.fn();
    await act(async () => {
      root.render(
        React.createElement(GenericModal, {
          visible: true,
          variant: "alert",
          severity: "success",
          title: "Listo",
          message: "OK",
          buttonText: "Aceptar",
          onConfirm,
        })
      );
    });
    expect(container.textContent).toContain("Listo");
    expect(container.textContent).toContain("OK");
    const button = container.querySelector("button");
    expect(button).toBeTruthy();
    await act(async () => {
      button!.dispatchEvent(new MouseEvent("click", { bubbles: true }));
    });
    expect(onConfirm).toHaveBeenCalled();
  });

  it.each(["error", "warning", "info"] as const)(
    "renderiza alert severity=%s",
    async (severity) => {
      await act(async () => {
        root.render(
          React.createElement(GenericModal, {
            visible: true,
            variant: "alert",
            severity,
            message: `msg-${severity}`,
            onClose: jest.fn(),
          })
        );
      });
      expect(container.textContent).toContain(`msg-${severity}`);
      expect(
        container.querySelector(`.fiscal-modal-icon-container-${severity}`)
      ).toBeTruthy();
    }
  );
});
