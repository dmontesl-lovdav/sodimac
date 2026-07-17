/**
 * @jest-environment jsdom
 */
import { describe, it, expect, jest } from "@jest/globals";
import React from "react";
import { createRoot } from "react-dom/client";
import { act } from "react-dom/test-utils";
import { usePaginatedData } from "../usePaginatedData";

function HookHost({
  fetchFn,
  enabled,
  onReady,
}: {
  fetchFn: any;
  enabled: boolean;
  onReady: (api: any) => void;
}) {
  const api = usePaginatedData({
    fetchFn,
    initialFilters: {},
    enabled,
  });
  React.useEffect(() => {
    onReady(api);
  }, [api, onReady]);
  return null;
}

describe("usePaginatedData", () => {
  it("no llama fetchFn cuando enabled=false", async () => {
    const fetchFn = jest.fn();
    const container = document.createElement("div");
    const root = createRoot(container);
    let latest: any;
    await act(async () => {
      root.render(
        React.createElement(HookHost, {
          fetchFn,
          enabled: false,
          onReady: (api) => {
            latest = api;
          },
        })
      );
    });
    expect(fetchFn).not.toHaveBeenCalled();
    expect(latest.rows).toEqual([]);
    root.unmount();
  });

  it("carga datos cuando enabled=true", async () => {
    const fetchFn = jest.fn().mockResolvedValue({
      content: [{ id: 1 }],
      totalElements: 1,
      totalPages: 1,
      page: 0,
      size: 10,
    });
    const container = document.createElement("div");
    const root = createRoot(container);
    let latest: any = { rows: [] };
    await act(async () => {
      root.render(
        React.createElement(HookHost, {
          fetchFn,
          enabled: true,
          onReady: (api) => {
            latest = api;
          },
        })
      );
    });
    await act(async () => {
      await Promise.resolve();
      await Promise.resolve();
    });
    expect(fetchFn).toHaveBeenCalled();
    expect(latest.rows).toEqual([{ id: 1 }]);
    root.unmount();
  });
});
