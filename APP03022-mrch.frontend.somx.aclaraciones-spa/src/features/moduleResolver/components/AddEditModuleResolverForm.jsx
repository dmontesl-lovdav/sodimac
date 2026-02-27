import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import {
    Breadcrumb,
    GenericButton,
    GenericSelectFloating,
    GenericInput,
    GenericModal
} from '@shared/components/ui';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog } from '@/features/cases/components/RequestUtils';

import {
    upsertModuleResolver,
    getResolverById,
} from '../api/moduleResolverService';

import '../styles/AddEditModuleResolverForm.css';

export default function AddEditModuleResolverForm() {
    const { id } = useParams();
    const nav = useNavigate();
    const api = ConfigurationBuilder.client;

    const [modules, setModules] = useState([]);
    const [moduleId, setModuleId] = useState('');
    const [moduleName, setModuleName] = useState('');
    const [area, setArea] = useState('');

    const [personName, setPersonName] = useState('');
    const [resolverEmail, setResolverEmail] = useState('');

    const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(resolverEmail);
    const isValid = personName.trim() && resolverEmail.trim() && emailValid;

    const [loadingModal, setLoadingModal] = useState(false);
    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
    });

    const CATALOG_TYPE = 3;

    /* ---------- 1) Catálogo de módulos ---------- */
    useEffect(() => {
        loadCatalog(api, CATALOG_TYPE, (arr) => {
            setModules(arr);
        });
    }, [api]);

    /* ---------- 2) Editar: cargar registro ---------- */
    useEffect(() => {
        if (!id) return;

        (async () => {
            const row = await getResolverById(Number(id));

            setModuleId(String(row.moduleId));
            setModuleName(row.moduleName);
            setArea(row.area);
            setPersonName(row.personName);
            setResolverEmail(row.resolverEmail);
        })();
    }, [id]);

    /* ---------- 3) Al cambiar módulo → autocompletar ---------- */
    useEffect(() => {
        const m = modules.find((x) => String(x.id) === String(moduleId));
        if (m) {
            setModuleName(m.description);
            setArea(m.area || '');
        }
    }, [moduleId, modules]);

    /* ---------- Cambio de país: salir ---------- */
    useEffect(() => {
        const handler = () => nav('/moduleResolver');
        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, [nav]);

    /* ---------- Validaciones ---------- */
    const validate = () => {
        if (!personName.trim()) {
            setAlert({
                visible: true,
                title: 'Campo requerido',
                message: 'El nombre de la persona es obligatorio.',
            });
            return false;
        }

        if (!resolverEmail.trim()) {
            setAlert({
                visible: true,
                title: 'Campo requerido',
                message: 'El email del resolutor es obligatorio.',
            });
            return false;
        }

        if (!emailValid) {
            setAlert({
                visible: true,
                title: 'Correo inválido',
                message: 'El formato del correo no es válido.',
            });
            return false;
        }

        return true;
    };

    /* ---------- Guardar ---------- */
    const save = async () => {
        if (!validate()) return;

        setLoadingModal(true);

        await upsertModuleResolver({
            id: id ? Number(id) : undefined,
            moduleId: Number(moduleId),
            moduleName,
            area,
            personName,
            resolverEmail,
        });

        setLoadingModal(false);
        nav('/moduleResolver');
    };

    const breadcrumbItems = [
        { label: 'Centro de ayuda', to: '/' },
        { label: 'Mantenedor', to: '/mantenedor' },
        { label: 'Resolutores por módulo', to: '/moduleResolver' },
        { label: id ? 'Editar resolutor' : 'Agregar resolutor' },
    ];

    return (
        <div className="amr-layout">
            <GenericModal
                visible={loadingModal}
                variant="loading"
                message="Guardando información…"
            />

            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                buttonText="Aceptar"
                onClose={() => setAlert({ ...alert, visible: false })}
            />

            <Breadcrumb items={breadcrumbItems} />

            <div className="amr-box">
                <h2 className="amr-title">
                    {id ? 'Editar resolutor' : 'Agregar resolutor'}
                </h2>

                <GenericSelectFloating
                    label="Módulo"
                    value={moduleId}
                    onChange={(e) => setModuleId(e.target.value)}
                    options={modules.map((m) => ({
                        value: String(m.id),
                        label: m.description,
                    }))}
                />

                <GenericInput
                    className="amr-field"
                    label="Área (auto)"
                    value={area}
                    disabled
                />

                <GenericInput
                    className="amr-field"
                    label="Nombre de la persona"
                    required
                    value={personName}
                    onChange={(e) => setPersonName(e.target.value)}
                />

                <GenericInput
                    className="amr-field"
                    label="Resolver email"
                    required
                    validateEmail
                    value={resolverEmail}
                    onChange={(e) => setResolverEmail(e.target.value)}
                />

                <div className="amr-footer">
                    <GenericButton
                        variant="text"
                        onClick={() => nav('/moduleResolver')}
                    >
                        Volver
                    </GenericButton>

                    <GenericButton
                        variant="primary"
                        onClick={save}
                        disabled={!isValid}
                        className={!isValid ? 'amr-disabled' : ''}
                    >
                        Guardar
                    </GenericButton>
                </div>
            </div>
        </div>
    );
}
