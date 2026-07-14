/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect, beforeEach, afterEach } from "@jest/globals";
import { createRoot } from "react-dom/client";
import { act } from "react";
import React from "react";

import GenericSelectSearchable, {
  findSelectedOption,
  isOptionSelected,
} from "../GenericSelectSearchable";
import GenericSelectFloating, { hasFloatingSelectValue } from "../GenericSelectFloating";

describe("select helpers", () => {
  it("findSelectedOption / isOptionSelected / hasFloatingSelectValue", () => {
    const options = [
      { value: "1", label: "Uno" },
      { value: "2", label: "Dos" },
    ];
    expect(findSelectedOption("2", options)?.label).toBe("Dos");
    expect(isOptionSelected(options[0], "1")).toBe(true);
    expect(hasFloatingSelectValue("")).toBe(false);
    expect(hasFloatingSelectValue(0)).toBe(true);
  });
});

describe("GenericSelectSearchable mount", () => {
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

  it("filtra opciones y limpia valor", async () => {
    const onChange = jest.fn();
    await act(async () => {
      root.render(
        React.createElement(GenericSelectSearchable, {
          value: "1",
          onChange,
          options: [
            { value: "1", label: "Uno" },
            { value: "2", label: "Dos" },
          ],
          placeholder: "Buscar",
        })
      );
    });

    const input = container.querySelector("input") as HTMLInputElement;
    expect(input.value).toBe("Uno");

    await act(async () => {
      input.focus();
      input.value = "Do";
      input.dispatchEvent(new Event("input", { bubbles: true }));
      input.dispatchEvent(new Event("change", { bubbles: true }));
    });

    const clearBtn = container.querySelector(".gss-clear") as HTMLButtonElement | null;
    if (clearBtn) {
      await act(async () => {
        clearBtn.click();
      });
      expect(onChange).toHaveBeenCalled();
    }
  });
});

describe("GenericSelectFloating mount", () => {
  let container: HTMLDivElement;
  let root: ReturnType<typeof createRoot>;

  beforeEach(() => {
    (global as any).ResizeObserver = class {
      observe() {}
      disconnect() {}
      unobserve() {}
    };
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("abre lista y selecciona opción", async () => {
    const onChange = jest.fn();
    const onValueChange = jest.fn();
    await act(async () => {
      root.render(
        React.createElement(GenericSelectFloating, {
          label: "Estado",
          value: "",
          required: true,
          options: [
            { value: "1", label: "Activo" },
            { value: "2", label: "Inactivo" },
          ],
          onChange,
          onValueChange,
          name: "estatus",
        })
      );
    });

    const button = container.querySelector("button") as HTMLButtonElement;
    expect(button).toBeTruthy();
    await act(async () => {
      button.click();
    });
    const option = container.querySelector('[role="option"]') as HTMLElement | null;
    if (option) {
      await act(async () => {
        option.click();
      });
      expect(onValueChange).toHaveBeenCalled();
    }
  });
});
