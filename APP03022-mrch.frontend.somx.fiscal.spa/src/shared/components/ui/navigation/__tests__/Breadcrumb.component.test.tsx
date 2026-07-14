/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect } from "@jest/globals";

jest.mock("react-router-dom", () => ({
  Link: ({ children, to }: { children: any; to: string }) => ({
    type: "a",
    props: { href: to, children },
  }),
}));

import { renderCrumb, buildDisplayItems } from "../Breadcrumb";
import Breadcrumb from "../Breadcrumb";

describe("renderCrumb", () => {
  it("renderiza anchor externo", () => {
    const node = renderCrumb({ label: "Inicio", externalHref: "https://home" }, false) as any;
    expect(node.props.href).toBe("https://home");
  });

  it("renderiza span cuando es último o sin to", () => {
    const last = renderCrumb({ label: "Actual" }, true) as any;
    expect(last.props.className).toContain("current");
    const noTo = renderCrumb({ label: "Sin link" }, false) as any;
    expect(noTo.type).toBe("span");
  });

  it("renderiza Link cuando tiene to y no es último", () => {
    const node = renderCrumb({ label: "Facturas", to: "/facturas" }, false) as any;
    expect(node.props.to || node.props.href).toBeTruthy();
  });
});

describe("Breadcrumb component", () => {
  it("puede invocarse como función y genera items con separador", () => {
    const el = Breadcrumb({
      items: [{ label: "Facturas", to: "/f" }, { label: "Detalle" }],
      className: "extra",
    }) as any;
    expect(el.props.className).toContain("extra");
    expect(el.props.children.length).toBe(3);
  });

  it("buildDisplayItems alimenta el nav", () => {
    expect(buildDisplayItems([{ label: "X" }])[0].label).toBe("Inicio");
  });
});
