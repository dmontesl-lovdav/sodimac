import { FC, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import DatePicker, { registerLocale } from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { es } from 'date-fns/locale';

registerLocale('es', es);
import { CatalogItem } from '../services/parameterService';
import { useUpdateParameter, useCreateParameterVersion } from '../hooks';
import type { Parameter } from '../types';
import '../styles/AddEditParameterForm.css';

// Zod Schema for edit form validation
const editParameterSchema = z.object({
    description: z.string().max(250, 'La descripción no puede exceder 250 caracteres').optional(),
    value: z
        .string()
        .min(1, 'El valor del parámetro es requerido')
        .max(150, 'El valor no puede exceder 150 caracteres'),
    startDate: z
        .date()
        .refine((date) => date !== null && date !== undefined, 'La fecha de inicio de vigencia es requerida'),
    endDate: z.date().nullable().optional(),
    status: z.union([z.literal(0), z.literal(1), z.literal('0'), z.literal('1')]), // Acepta 0, 1, '0', '1'
}).refine(
    (data) => {
        if (data.endDate && data.startDate) {
            return data.endDate >= data.startDate;
        }
        return true;
    },
    {
        message: 'La fecha fin de la vigencia debe ser mayor o igual a la fecha de inicio de la vigencia',
        path: ['endDate'],
    }
);

type EditParameterFormData = z.infer<typeof editParameterSchema>;

interface ParameterEditFormProps {
    parameter: Parameter;
    onSuccess: () => void;
    onCancel: () => void;
    catalogs: {
        modules: CatalogItem[];
        parameterTypes: CatalogItem[];
        statuses: CatalogItem[];
    };
}

/**
 * Formatea la versión para mostrar siempre con un solo decimal
 * Ej: "1.20" → "1.2", 1.0 → "1.0", "2" → "2.0"
 */
const formatVersion = (version: string | number): string => {
    const versionNum = typeof version === 'number' ? version : parseFloat(version);
    return versionNum.toFixed(1);
};

/**
 * Calcula la siguiente versión del parámetro
 * - Incremento estándar: Sumar 0.1 (1.0 → 1.1 → 1.2 → ... → 1.9 → 2.0)
 * - Salto de entero: Si termina en .9, la siguiente es X+1.0
 */
const calculateNextVersion = (currentVersion: string | number): string => {
    const versionNum = typeof currentVersion === 'number' ? currentVersion : parseFloat(currentVersion);
    const majorVersion = Math.floor(versionNum);
    // Obtener el decimal (ej: 1.9 → 9, 1.0 → 0)
    const minorVersion = Math.round((versionNum - majorVersion) * 10);

    if (minorVersion >= 9) {
        // Salto al siguiente entero (1.9 → 2.0)
        return (majorVersion + 1).toFixed(1);
    } else {
        // Incremento estándar de 0.1 (1.0 → 1.1)
        return (versionNum + 0.1).toFixed(1);
    }
};

export const ParameterEditForm: FC<ParameterEditFormProps> = ({
    parameter,
    onSuccess,
    onCancel,
    catalogs,
}) => {
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [showConfirmDialog, setShowConfirmDialog] = useState(false);
    const [pendingFormData, setPendingFormData] = useState<EditParameterFormData | null>(null);

    // Valor original para detectar cambios
    const originalValue = parameter.value;

    const {
        register,
        handleSubmit,
        control,
        watch,
        reset,
        formState: { errors },
    } = useForm<EditParameterFormData>({
        resolver: zodResolver(editParameterSchema),
        defaultValues: {
            description: parameter.description || '',
            value: parameter.value,
            startDate: parameter.startDate ? new Date(parameter.startDate + 'T00:00:00') : undefined,
            endDate: parameter.endDate ? new Date(parameter.endDate + 'T00:00:00') : null,
            status: parameter.status,
        },
    });

    const updateMutation = useUpdateParameter();
    const createVersionMutation = useCreateParameterVersion();

    const currentValue = watch('value');
    const valueChanged = currentValue !== originalValue;

    const onSubmit = (data: EditParameterFormData) => {
        setSuccessMessage(null);
        setErrorMessage(null);

        if (valueChanged) {
            // Si el valor cambió, mostrar diálogo de confirmación
            setPendingFormData(data);
            setShowConfirmDialog(true);
        } else {
            // Si solo cambiaron otros campos, hacer update simple
            performSimpleUpdate(data);
        }
    };

    const performSimpleUpdate = (data: EditParameterFormData) => {
        updateMutation.mutate(
            {
                id: parameter.idParameter,
                data: {
                    description: data.description,
                    startDate: data.startDate.toISOString().split('T')[0],
                    endDate: data.endDate?.toISOString().split('T')[0],
                },
            },
            {
                onSuccess: () => {
                    setSuccessMessage('El parámetro ha sido modificado correctamente.');
                    setTimeout(() => {
                        onSuccess();
                    }, 1500);
                },
                onError: (error: unknown) => {
                    let errorMsg = 'Error desconocido';
                    if (error instanceof Error) {
                        errorMsg = error.message;
                    } else if (typeof error === 'object' && error !== null && 'message' in error) {
                        errorMsg = String((error as { message: unknown }).message);
                    }
                    console.error('Error al modificar parámetro:', error);
                    setErrorMessage(`Error al modificar el parámetro: ${errorMsg}`);
                },
            }
        );
    };

    const performVersionCreate = async () => {
        if (!pendingFormData) return;

        // Detectar si la descripción cambió para actualizarla en la NUEVA versión
        const descriptionChanged = pendingFormData.description !== (parameter.description || '');

        // Crear la nueva versión primero
        createVersionMutation.mutate(
            {
                id: parameter.idParameter,
                data: {
                    value: pendingFormData.value,
                    startDate: pendingFormData.startDate.toISOString().split('T')[0],
                    endDate: pendingFormData.endDate?.toISOString().split('T')[0],
                    changeReason: 'Actualización de valor',
                },
            },
            {
                onSuccess: (newParameter) => {
                    // Si la descripción cambió, actualizar la NUEVA versión (no la anterior)
                    if (descriptionChanged && newParameter?.idParameter) {
                        updateMutation.mutate(
                            {
                                id: newParameter.idParameter,
                                data: {
                                    description: pendingFormData.description,
                                },
                            },
                            {
                                onSuccess: () => {
                                    setShowConfirmDialog(false);
                                    setSuccessMessage('Se ha generado una nueva versión del parámetro correctamente.');
                                    setTimeout(() => {
                                        onSuccess();
                                    }, 1500);
                                },
                                onError: (error: unknown) => {
                                    // La versión se creó pero falló la actualización de descripción
                                    setShowConfirmDialog(false);
                                    let errorMsg = 'Error desconocido';
                                    if (error instanceof Error) {
                                        errorMsg = error.message;
                                    } else if (typeof error === 'object' && error !== null && 'message' in error) {
                                        errorMsg = String((error as { message: unknown }).message);
                                    }
                                    console.error('Error al actualizar descripción de nueva versión:', error);
                                    setErrorMessage(`Nueva versión creada, pero error al actualizar descripción: ${errorMsg}`);
                                },
                            }
                        );
                    } else {
                        // No cambió la descripción, solo mostrar éxito
                        setShowConfirmDialog(false);
                        setSuccessMessage('Se ha generado una nueva versión del parámetro correctamente.');
                        setTimeout(() => {
                            onSuccess();
                        }, 1500);
                    }
                },
                onError: (error: unknown) => {
                    setShowConfirmDialog(false);
                    let errorMsg = 'Error desconocido';
                    if (error instanceof Error) {
                        errorMsg = error.message;
                    } else if (typeof error === 'object' && error !== null && 'message' in error) {
                        errorMsg = String((error as { message: unknown }).message);
                    }
                    console.error('Error al crear nueva versión:', error);
                    setErrorMessage(`Error al crear nueva versión: ${errorMsg}`);
                },
            }
        );
    };

    const handleClear = () => {
        // Resetea los campos editables a sus valores originales
        reset({
            description: parameter.description || '',
            value: parameter.value,
            startDate: parameter.startDate ? new Date(parameter.startDate + 'T00:00:00') : undefined,
            endDate: parameter.endDate ? new Date(parameter.endDate + 'T00:00:00') : null,
            status: parameter.status,
        });
        setSuccessMessage(null);
        setErrorMessage(null);
    };

    const descriptionValue = watch('description') || '';

    const CalendarIcon = () => (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
        </svg>
    );

    const getModuleLabel = () => {
        const module = catalogs.modules.find(m => m.value === String(parameter.idModule));
        return module?.label || parameter.module;
    };

    const getTypeLabel = () => {
        const type = catalogs.parameterTypes.find(t => t.value === String(parameter.idType));
        return type?.label || parameter.parameterType;
    };

    const isPending = updateMutation.isPending || createVersionMutation.isPending;

    return (
        <div className="parameter-create">
            {/* Diálogo de confirmación para versionado */}
            {showConfirmDialog && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000,
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '8px',
                        padding: '24px',
                        maxWidth: '500px',
                        width: '90%',
                        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)',
                    }}>
                        <h3 style={{ marginTop: 0, marginBottom: '16px', color: '#333' }}>
                            Confirmar cambio de versión
                        </h3>
                        <p style={{ color: '#666', marginBottom: '8px' }}>
                            Al editar el valor se generará una nueva versión del parámetro y la anterior quedará inactiva.
                        </p>
                        <p style={{ color: '#666', marginBottom: '24px' }}>
                            <strong>Versión actual:</strong> {formatVersion(parameter.version)}<br />
                            <strong>Nueva versión:</strong> {calculateNextVersion(parameter.version)}
                        </p>
                        <p style={{ color: '#333', fontWeight: 500, marginBottom: '24px' }}>
                            ¿Desea continuar?
                        </p>
                        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                            <button
                                type="button"
                                onClick={() => {
                                    setShowConfirmDialog(false);
                                    setPendingFormData(null);
                                }}
                                style={{
                                    padding: '10px 20px',
                                    border: '1px solid #ccc',
                                    borderRadius: '6px',
                                    backgroundColor: 'white',
                                    cursor: 'pointer',
                                    fontWeight: 500,
                                }}
                            >
                                Cancelar
                            </button>
                            <button
                                type="button"
                                onClick={performVersionCreate}
                                disabled={createVersionMutation.isPending}
                                style={{
                                    padding: '10px 20px',
                                    border: 'none',
                                    borderRadius: '6px',
                                    backgroundColor: '#0071CE',
                                    color: 'white',
                                    cursor: 'pointer',
                                    fontWeight: 500,
                                }}
                            >
                                {createVersionMutation.isPending ? 'Procesando...' : 'Confirmar'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <div className="parameter-create__header">
                <h1 className="parameter-create__title">Editar Parámetro</h1>
                <p className="parameter-create__description">
                    Para editar un parámetro existente, actualiza la información en el formulario, asegurándote de ingresar datos precisos, completos y actualizados.
                </p>
            </div>

            <hr className="parameter-create__divider" />

            <div className="parameter-create__content">
                <div className="parameter-create__stepper">
                    <div className="parameter-create__step-circle">1</div>
                    <div className="parameter-create__step-line"></div>
                </div>

                <div className="parameter-create__form-container">
                    <div className="parameter-create__section-header">
                        <h2 className="parameter-create__section-title">Datos de Parámetro</h2>
                        <p className="parameter-create__section-subtitle">Modifica la información del parámetro.</p>
                    </div>

                    <form className="parameter-create__form" onSubmit={handleSubmit(onSubmit)}>
                        {/* 1. Nombre del parámetro */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="name">
                                Nombre del parámetro
                            </label>
                            <input
                                id="name"
                                type="text"
                                className="parameter-create__input"
                                value={parameter.name}
                                disabled
                                style={{ backgroundColor: '#f5f5f5', cursor: 'not-allowed' }}
                            />
                        </div>

                        {/* 2. Descripción del parámetro */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="description">
                                Descripción del parámetro
                            </label>
                            <input
                                id="description"
                                type="text"
                                className="parameter-create__input"
                                maxLength={250}
                                placeholder="Descripción del parámetro"
                                {...register('description')}
                            />
                        </div>
                        <div className="parameter-create__char-count">
                            {descriptionValue.length} / 250
                        </div>

                        {/* 3. Módulo */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="module">
                                Módulo
                            </label>
                            <input
                                id="module"
                                type="text"
                                className="parameter-create__input"
                                value={getModuleLabel()}
                                disabled
                                style={{ backgroundColor: '#f5f5f5', cursor: 'not-allowed' }}
                            />
                        </div>

                        {/* 4. Tipo de parámetro */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="type">
                                Tipo de parámetro
                            </label>
                            <input
                                id="type"
                                type="text"
                                className="parameter-create__input"
                                value={getTypeLabel()}
                                disabled
                                style={{ backgroundColor: '#f5f5f5', cursor: 'not-allowed' }}
                            />
                        </div>

                        {/* 5. Valor del parámetro */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="value">
                                Valor del parámetro<span>*</span>
                                {valueChanged && (
                                    <span style={{
                                        marginLeft: '10px',
                                        fontSize: '12px',
                                        color: '#f59e0b',
                                        fontWeight: 'normal'
                                    }}>
                                        (Se generará nueva versión)
                                    </span>
                                )}
                            </label>
                            <input
                                id="value"
                                type="text"
                                className="parameter-create__input"
                                maxLength={150}
                                placeholder="Valor del parámetro"
                                {...register('value')}
                                style={{
                                    borderColor: valueChanged ? '#f59e0b' : undefined
                                }}
                            />
                            {errors.value && <span className="parameter-create__error-msg">{errors.value.message}</span>}
                        </div>

                        {/* 6. Versión del parámetro */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="currentVersion">
                                Versión del parámetro
                            </label>
                            <input
                                id="currentVersion"
                                type="text"
                                className="parameter-create__input"
                                value={formatVersion(parameter.version)}
                                disabled
                                style={{ backgroundColor: '#f5f5f5', cursor: 'not-allowed' }}
                            />
                        </div>

                        {/* Mostrar siguiente versión cuando el valor cambia */}
                        {valueChanged && (
                            <div className="parameter-create__field">
                                <label className="parameter-create__label" htmlFor="nextVersion">
                                    Nueva versión (se generará al guardar)
                                </label>
                                <input
                                    id="nextVersion"
                                    type="text"
                                    className="parameter-create__input"
                                    value={calculateNextVersion(parameter.version)}
                                    disabled
                                    style={{
                                        backgroundColor: '#fff3cd',
                                        cursor: 'not-allowed',
                                        borderColor: '#f59e0b',
                                        fontWeight: 600
                                    }}
                                />
                            </div>
                        )}

                        {/* 7. Fecha de inicio vigencia */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="startDate">
                                Fecha de inicio vigencia<span>*</span>
                            </label>
                            <div className="parameter-create__input-wrapper">
                                <Controller
                                    control={control}
                                    name="startDate"
                                    render={({ field }) => (
                                        <DatePicker
                                            id="startDate"
                                            selected={field.value}
                                            onChange={(date) => field.onChange(date)}
                                            dateFormat="dd-MM-yyyy"
                                            placeholderText="dd-mm-aaaa"
                                            autoComplete="off"
                                            locale="es"
                                        />
                                    )}
                                />
                                <div className="parameter-create__datepicker-icon">
                                    <CalendarIcon />
                                </div>
                            </div>
                            {errors.startDate && <span className="parameter-create__error-msg">{errors.startDate.message}</span>}
                        </div>

                        {/* 8. Fecha de fin vigencia */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="endDate">
                                Fecha de fin vigencia
                            </label>
                            <div className="parameter-create__input-wrapper">
                                <Controller
                                    control={control}
                                    name="endDate"
                                    render={({ field }) => (
                                        <DatePicker
                                            id="endDate"
                                            selected={field.value}
                                            onChange={(date) => field.onChange(date)}
                                            dateFormat="dd-MM-yyyy"
                                            placeholderText="dd-mm-aaaa"
                                            autoComplete="off"
                                            locale="es"
                                        />
                                    )}
                                />
                                <div className="parameter-create__datepicker-icon">
                                    <CalendarIcon />
                                </div>
                            </div>
                            {errors.endDate && <span className="parameter-create__error-msg">{errors.endDate.message}</span>}
                        </div>

                        {/* 9. Estatus del parámetro */}
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="status">
                                Estatus del parámetro<span>*</span>
                            </label>
                            <select
                                id="status"
                                className="parameter-create__select"
                                {...register('status')}
                            >
                                {catalogs.statuses.map((s) => (
                                    <option key={s.value} value={s.value}>
                                        {s.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {successMessage && (
                            <div style={{
                                backgroundColor: '#d4edda',
                                color: '#155724',
                                padding: '12px 16px',
                                borderRadius: '6px',
                                marginBottom: '16px',
                                fontWeight: 500,
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px'
                            }}>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                                    <polyline points="22 4 12 14.01 9 11.01" />
                                </svg>
                                {successMessage}
                            </div>
                        )}

                        {errorMessage && (
                            <div style={{
                                backgroundColor: '#f8d7da',
                                color: '#721c24',
                                padding: '12px 16px',
                                borderRadius: '6px',
                                marginBottom: '16px',
                                fontWeight: 500
                            }}>
                                {errorMessage}
                            </div>
                        )}

                        <div className="parameter-create__footer">
                            <div className="parameter-create__footer-left">
                                <button type="button" className="parameter-create__btn-clear" onClick={handleClear}>
                                    Limpiar
                                </button>
                                <button
                                    type="submit"
                                    className="parameter-create__btn-save"
                                    disabled={isPending}
                                >
                                    {isPending ? 'Guardando...' : 'Guardar Cambios'}
                                </button>
                            </div>
                            <button type="button" className="parameter-create__btn-back" onClick={onCancel}>
                                Volver
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};
