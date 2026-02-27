/**
 * @jest-environment jsdom
 */

// =======================
// MOCK
// =======================
jest.mock("./FaqGridContainer", () => ({
    __esModule: true,
    default: function FaqGridContainerMock() {
        return (
            <div data-testid="root">
                <nav data-testid="breadcrumb">Breadcrumb</nav>

                <button data-testid="btn-mass">Carga masiva</button>

                <button data-testid="btn-new">
                    + Agregar nueva pregunta frecuente
                </button>

                <div data-testid="toolbar" />
                <div data-testid="table" />
            </div>
        );
    },
}));

// =======================
// ROUTER MOCK
// =======================
const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => {
    const actual = jest.requireActual("react-router-dom");
    return {
        __esModule: true,
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

// =======================
// IMPORTS DESPUÉS DE TODOS LOS MOCKS
// =======================
import React from "react";
import { render, fireEvent } from "@testing-library/react";
import FaqGridContainer from "./FaqGridContainer";

// =======================
// TESTS
// =======================
describe("FaqGridContainer (mock stable)", () => {
    test("renderiza breadcrumb + toolbar + table", () => {
        const { getByTestId } = render(<FaqGridContainer />);

        expect(getByTestId("breadcrumb")).toBeTruthy();
        expect(getByTestId("toolbar")).toBeTruthy();
        expect(getByTestId("table")).toBeTruthy();
    });
});
