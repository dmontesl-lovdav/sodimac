import { useState } from 'react';
import { GenericDropzone } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

export default function UploadDemo() {
    const [file, setFile] = useState<File | null>(null);

    return (
        <DemoCard
            title="Dropzone"
            desc="Arrastra y suelta o haz clic para seleccionar un archivo."
        >
            <div className="grid gap-4 max-w-lg">
                <GenericDropzone
                    accept=".png,.jpg,.jpeg,.pdf"
                    maxSizeMb={10}
                    file={file}
                    onFileSelect={setFile}
                    children={null}
                />
                <div className="text-sm text-slate-600">
                    File: <b>{file?.name ?? '—'}</b>
                </div>
            </div>
        </DemoCard>
    );
}
