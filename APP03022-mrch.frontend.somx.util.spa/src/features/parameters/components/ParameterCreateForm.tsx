import { FC, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import DatePicker, { registerLocale } from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { es } from 'date-fns/locale';

registerLocale('es', es);
import { CatalogItem } from '../services/parameterService';
import '../styles/AddEditParameterForm.css';
import { useCreateParameter, useCheckParameterNameExists } from '../hooks';
import { useModalNotification } from '@shared/components/ui/modal';
import { extractApiErrorMessage } from '@shared/utils/errorMessage';

const toIsoStartOfDay = (date: Date): string => {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    return d.toISOString();
};

const CalendarIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
        <line x1="16" y1="2" x2="16" y2="6" />
        <line x1="8" y1="2" x2="8" y2="6" />
        <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
);

// Zod Schema for form validation
const parameterSchema = z.object({
    name: z
        .string()
        .min(1, 'El nombre del parámetro es requerido')
        .max(50, 'El nombre no puede exceder 50 caracteres'),
    description: z.string().max(250, 'La descripción no puede exceder 250 caracteres').optional(),
    module: z.string().min(1, 'El módulo es requerido'),
    type: z.string().min(1, 'El tipo de parámetro es requerido'),
    value: z
        .string()
        .min(1, 'El valor del parámetro es requerido')
        .max(150, 'El valor no puede exceder 150 caracteres'),
    startDate: z
        .date()
        .refine((date) => date !== null && date !== undefined, 'La fecha de inicio de vigencia es requerida')
        .refine((date) => {
            if (!date) return true;
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            return date >= today;
        }, 'La fecha de inicio de la vigencia debe ser igual o mayor a la fecha actual'),
    endDate: z.date().nullable().optional(),
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

type ParameterFormData = z.infer<typeof parameterSchema>;

interface ParameterCreateFormProps {
    onSuccess: () => void;
    onCancel: () => void;
    catalogs: {
        modules: CatalogItem[];
        parameterTypes: CatalogItem[];
    };
}

export const ParameterCreateForm: FC<ParameterCreateFormProps> = ({
    onSuccess,
    onCancel,
    catalogs,
}) => {
    const {
        register,
        handleSubmit,
        control,
        reset,
        watch,
        formState: { errors },
    } = useForm<ParameterFormData>({
        resolver: zodResolver(parameterSchema),
        defaultValues: {
            name: '',
            description: '',
            module: '',
            type: '',
            value: '',
            startDate: undefined,
            endDate: null,
        },
    });

    const createMutation = useCreateParameter();
    const checkNameMutation = useCheckParameterNameExists();
    const { showSuccess, showError, ModalNode } = useModalNotification();
    const [nameExistsError, setNameExistsError] = useState<string | null>(null);
    const [isCheckingName, setIsCheckingName] = useState(false);

    const handleNameBlur = async (e: React.FocusEvent<HTMLInputElement>) => {
        const name = e.target.value.trim();
        if (!name) {
            setNameExistsError(null);
            return;
        }

        setIsCheckingName(true);
        try {
            const exists = await checkNameMutation.mutateAsync(name);
            if (exists) {
                setNameExistsError('El nombre del parámetro ya existe en el sistema. Para generar una nueva versión, por favor localice el registro existente en el listado y utilice la opción Editar');
            } else {
                setNameExistsError(null);
            }
        } catch {
            setNameExistsError(null);
        } finally {
            setIsCheckingName(false);
        }
    };

    const onSubmit = (data: ParameterFormData) => {
        if (nameExistsError) {
            showError(
                'El nombre del parámetro ya está en uso. Localiza el registro en el listado y utiliza la opción Editar para crear una nueva versión.',
                'No se puede crear el parámetro',
            );
            return;
        }

        createMutation.mutate(
            {
                idModule: Number(data.module),
                idType: Number(data.type),
                name: data.name,
                description: data.description,
                value: data.value,
                startDate: toIsoStartOfDay(data.startDate),
                endDate: data.endDate ? toIsoStartOfDay(data.endDate) : undefined,
            },
            {
                onSuccess: () => {
                    showSuccess(
                        'El parámetro fue creado correctamente.',
                        'Operación exitosa',
                        () => onSuccess(),
                    );
                },
                onError: (error: unknown) => {
                    console.error('Error al crear parámetro:', error);
                    showError(
                        extractApiErrorMessage(error, {
                            fallback:
                                'No fue posible crear el parámetro. Verifica la información e inténtalo nuevamente.',
                        }),
                        'No se pudo crear el parámetro',
                    );
                },
            }
        );
    };

    const handleClear = () => {
        reset();
        setNameExistsError(null);
    };

    const descriptionValue = watch('description') || '';

    const hasRequiredFieldError = errors.name?.type === 'too_small' ||
        errors.module?.type === 'too_small' ||
        errors.type?.type === 'too_small' ||
        errors.value?.type === 'too_small' ||
        errors.startDate?.type === 'invalid_type';

    return (
        <div className="parameter-create">
            <div className="parameter-create__header">
                <h1 className="parameter-create__title">Nuevo Parámetro</h1>
                <p className="parameter-create__description">
                    Para crear un nuevo parámetro, sigue las instrucciones y completa el formulario, asegurándote de ingresar información precisa, completa y actualizada.
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
                        <p className="parameter-create__section-subtitle">Captura la información general del nuevo parámetro.</p>
                    </div>

                    <form className="parameter-create__form" onSubmit={handleSubmit(onSubmit)}>
                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="name">
                                Nombre del parámetro<span>*</span>
                                {isCheckingName && (
                                    <span style={{ marginLeft: '8px', fontSize: '12px', color: '#666' }}>
                                        Verificando...
                                    </span>
                                )}
                            </label>
                            <input
                                id="name"
                                type="text"
                                className="parameter-create__input"
                                maxLength={50}
                                placeholder="Nombre del parámetro"
                                {...register('name')}
                                onBlur={(e) => {
                                    register('name').onBlur(e);
                                    handleNameBlur(e);
                                }}
                                style={{
                                    borderColor: nameExistsError ? '#dc3545' : undefined
                                }}
                            />
                            {errors.name && <span className="parameter-create__error-msg">{errors.name.message}</span>}
                            {nameExistsError && !errors.name && (
                                <span className="parameter-create__error-msg" style={{ color: '#dc3545' }}>
                                    {nameExistsError}
                                </span>
                            )}
                        </div>

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

                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="module">
                                Módulo<span>*</span>
                            </label>
                            <select
                                id="module"
                                className="parameter-create__select"
                                {...register('module')}
                            >
                                <option value="">Selecciona un módulo</option>
                                {catalogs.modules.map((m) => (
                                    <option key={m.value} value={m.value}>
                                        {m.label}
                                    </option>
                                ))}
                            </select>
                            {errors.module && <span className="parameter-create__error-msg">{errors.module.message}</span>}
                        </div>

                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="type">
                                Tipo de parámetro<span>*</span>
                            </label>
                            <select
                                id="type"
                                className="parameter-create__select"
                                {...register('type')}
                            >
                                <option value="">Selecciona un tipo</option>
                                {catalogs.parameterTypes.map((t) => (
                                    <option key={t.value} value={t.value}>
                                        {t.label}
                                    </option>
                                ))}
                            </select>
                            {errors.type && <span className="parameter-create__error-msg">{errors.type.message}</span>}
                        </div>

                        <div className="parameter-create__field">
                            <label className="parameter-create__label" htmlFor="value">
                                Valor del parámetro<span>*</span>
                            </label>
                            <input
                                id="value"
                                type="text"
                                className="parameter-create__input"
                                maxLength={150}
                                placeholder="Valor del parámetro"
                                {...register('value')}
                            />
                            {errors.value && <span className="parameter-create__error-msg">{errors.value.message}</span>}
                        </div>

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

                        {hasRequiredFieldError && Object.keys(errors).length > 0 && (
                            <div className="parameter-create__error-msg" style={{ fontWeight: 600, maxWidth: '400px' }}>
                                Falta la captura de algunos campos obligatorios
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
                                    disabled={createMutation.isPending || !!nameExistsError || isCheckingName}
                                >
                                    {createMutation.isPending ? 'Guardando...' : 'Guardar Parámetro'}
                                </button>
                            </div>
                            <button type="button" className="parameter-create__btn-back" onClick={onCancel}>
                                Volver
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            {ModalNode}
        </div>
    );
};
