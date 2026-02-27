// src/features/faq/components/parts/FormFooter.jsx
import { GenericButton } from '@shared/components/ui';
import './styles/FormFooter.css';

export default function FormFooter({ isEdit, isSaving, onCancel, onSubmit }) {
    return (
        <div className="form-footer">
            <GenericButton variant="text" type="button" onClick={onCancel}>
                Volver
            </GenericButton>

            <GenericButton
                type="button"
                onClick={onSubmit}
                disabled={isSaving}
                className={isSaving ? 'ff-disabled' : ''}
            >
                {isSaving
                    ? isEdit
                        ? 'Guardando…'
                        : 'Guardando…'
                    : isEdit
                        ? 'Guardar cambios'
                        : 'Guardar'}
            </GenericButton>
        </div>
    );
}
