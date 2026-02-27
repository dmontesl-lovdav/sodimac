import { byCreationTimeSorter } from "../components/RequestUtils";
import AttachmentDownloader from "./AttachmentDownloader";

export default function AttachmentSummary({ requestId, attachments }) {
    function renderAttachments() {
        if (!attachments || attachments.length === 0) {
            return <div>--</div>;
        }

        return attachments
            .sort(byCreationTimeSorter)
            .map((attachment, i) => (
                <div key={attachment.id || i}>
                    <AttachmentDownloader
                        attachmentId={attachment.id}
                        requestId={requestId}
                        name={attachment.name}
                    />
                </div>
            ));
    }

    return (
        <div>
            <span className="font-bold">Documentos:</span>
            {renderAttachments()}
        </div>
    );
}
