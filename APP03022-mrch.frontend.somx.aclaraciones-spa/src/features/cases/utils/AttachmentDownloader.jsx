// src/features/cases/components/AttachmentDownloader.jsx
import { useState, useEffect } from "react";
import ConfigurationBuilder from "@/configuration/ConfigurationBuilder";
import { GenericButton } from "@shared/components/ui";

export default function AttachmentDownloader({ requestId, attachmentId, name }) {
    const STATUS_WAITING = 0;
    const STATUS_DOWNLOADING = 1;

    const [state, setState] = useState(STATUS_WAITING);

    async function waitForAttachment() {
        setState(STATUS_DOWNLOADING);

        try {
            const attachment = await ConfigurationBuilder.client.getRequestAttachment(
                requestId,
                attachmentId
            );

            let content = attachment?.content;

            // ✅ Maneja caso Buffer o base64 string
            if (content && typeof content === "object" && content.data) {
                content = new Uint8Array(content.data);
            } else if (typeof content === "string") {
                content = Uint8Array.from(atob(content), (c) => c.charCodeAt(0));
            } else {
                throw new Error("Formato de contenido no reconocido en el adjunto.");
            }

            const blob = new Blob([content]);
            const url = window.URL.createObjectURL(blob);
            const anchor = document.createElement("a");
            anchor.href = url;
            anchor.download = name || "archivo";
            document.body.appendChild(anchor);
            anchor.click();
            anchor.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error("Error descargando adjunto:", err);
        } finally {
            setState(STATUS_WAITING);
        }
    }

    function addLoader() {
        if (state === STATUS_DOWNLOADING) {
            return (
                <span className="ml-2 inline-block">
                    <span className="block w-5 h-5 border-2 border-gray-300 border-t-sky-600 rounded-full animate-spin" />
                </span>
            );
        }
        return null;
    }

    useEffect(() => {
        if (state === STATUS_DOWNLOADING) {
            waitForAttachment();
        }
    }, [state]);

    return (
        <div className="flex flex-row items-center">
            <GenericButton
                variant="link"
                disabled={state === STATUS_DOWNLOADING}
                onClick={() => setState(STATUS_DOWNLOADING)}
                className="underline underline-offset-4 inline-block mr-2"
            >
                {name}
            </GenericButton>
            {addLoader()}
        </div>
    );
}
