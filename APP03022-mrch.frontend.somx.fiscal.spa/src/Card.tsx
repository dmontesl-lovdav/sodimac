import React from "react";
import ReactDOMClient from "react-dom/client";
import singleSpaReact from "single-spa-react";
import { navigateToUrl } from "single-spa";

interface CardProps {
    onClick?: () => void;
    title?: string;
}

const FiscalIcon: React.FC = () => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        style={{
            width: "54px",
            height: "54px",
        }}
    >
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14,2 14,8 20,8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
        <polyline points="10,9 9,9 8,9" />
    </svg>
);

const Card: React.FC<CardProps> = ({
    onClick,
    title = "Fiscal",
}) => {
    const handleClick = () => {
        if (onClick) {
            onClick();
            return;
        }

        navigateToUrl("/fiscal");
    };

    return (
        <div
            onClick={handleClick}
            style={{
                width: "255px",
                height: "246px",
                minWidth: "255px",
                minHeight: "246px",
                border: "1px solid rgba(0, 0, 0, 0.12)",
                backgroundColor: "#FFFFFF",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
                boxSizing: "border-box",
                transition: "box-shadow 0.2s ease, border-color 0.2s ease, transform 0.2s ease",
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.boxShadow = "0 2px 8px rgba(0, 0, 0, 0.12)";
                e.currentTarget.style.borderColor = "#D0D0D0";
                e.currentTarget.style.transform = "translateY(-1px)";
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.boxShadow = "none";
                e.currentTarget.style.borderColor = "rgba(0, 0, 0, 0.12)";
                e.currentTarget.style.transform = "translateY(0)";
            }}
        >
            <div
                style={{
                    width: "100%",
                    height: "100%",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    textAlign: "center",
                    boxSizing: "border-box",
                }}
            >
                <div
                    style={{
                        width: "110px",
                        height: "110px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <div
                        style={{
                            width: "78px",
                            height: "78px",
                            borderRadius: "50%",
                            backgroundColor: "#FAFAFC",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            color: "#003865",
                            flexShrink: 0,
                        }}
                    >
                        <FiscalIcon />
                    </div>
                </div>

                <h2
                    style={{
                        margin: 0,
                        fontSize: "18px",
                        fontWeight: 500,
                        color: "#000000",
                        lineHeight: "27px",
                        fontFamily: "inherit",
                    }}
                >
                    {title}
                </h2>
            </div>
        </div>
    );
};

const lifecycles = singleSpaReact({
    React,
    ReactDOMClient,
    rootComponent: Card,
    errorBoundary() {
        return <div>Error al cargar el módulo fiscal</div>;
    },
});

export const { bootstrap, mount, unmount } = lifecycles;
export default Card;