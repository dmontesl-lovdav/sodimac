import { useState } from 'react';
import DemoCard from '../components/DemoCard';
import { GenericAttachmentUploader } from '@shared/components/ui';

export default function AttachmentsDemo() {
    const [files, setFiles] = useState<File[]>([]);

    return (
        <DemoCard
            title="AttachmentUploader"
            desc="Arrastra/selecciona archivos con validación, vista previa y descarga."
        >
            <div className="max-w-3xl">
                <GenericAttachmentUploader
                    files={files as any}
                    setFiles={setFiles as any}
                    fileExtensions={['pdf', 'jpg', 'jpeg', 'png']}
                    fileSize={4 * 1024 * 1024}
                    filenameLength={64}
                    multiple
                />
            </div>
        </DemoCard>
    );
}
