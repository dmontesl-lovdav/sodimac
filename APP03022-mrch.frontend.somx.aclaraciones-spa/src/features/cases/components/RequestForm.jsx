import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { Buffer } from 'buffer';
import { useEffect, useState } from 'react';
import AttachmentUploader from '../../../shared/components/ui/attachmentUploader/AttachmentUploader';
import { Step, VerticalStepper } from '../../../shared/components/ui/verticalStepper/VerticalStepper';
import '../styles/RequestForm.css';
import RequestConfirm from './RequestConfirm';
import { loadCatalog } from './RequestUtils';
import {
    GenericSelectFloating,
    GenericInput,
    GenericButton,
    GenericLinearProgress,
} from '@shared/components/ui';
import GenericModal from '@shared/components/ui/modal/GenericModal';

export default function RequestForm({ backCallback, businessUnits, countries, modules, reasons }) {
    const STATE_LOADING = 1;
    const STATE_LOADED = 2;
    const STATE_FORM_SENT = 3;
    const CATALOGS_DETAILS = 5;

    const apiClient = ConfigurationBuilder.client;

    const [state, setState] = useState(STATE_LOADED);
    const [businessUnitId, setBusinessUnitId] = useState('');
    const [countryId, setCountryId] = useState('');
    const [company, setCompany] = useState('');
    const [rut, setRut] = useState('');
    const [moduleId, setModuleId] = useState('');
    const [reasonId, setReasonId] = useState('');
    const [detailId, setDetailId] = useState('');
    const [orderId, setOrderId] = useState('');
    const [description, setDescription] = useState('');
    const [details, setDetails] = useState([]);
    const [files, setFiles] = useState([]);

    const [submitted, setSubmitted] = useState(null);

    const filteredCountries = businessUnitId === '2'
        ? countries.filter(c => c.id === 4 || c.id === 6)
        : [];

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');
    const [modalSeverity, setModalSeverity] = useState('error');

    const closeModal = () => setModalVisible(false);

    function toOptions(arr, valueKey = 'id', labelKey = 'description') {
        return arr
            .map((o) => ({
                value: String(o?.[valueKey] ?? ''),
                label: String(o?.[labelKey] ?? ''),
            }))
            .filter((o) => o.value && o.label);
    }

    const buOptions = toOptions(
        businessUnits.filter(bu => bu.id === 2)
    );
    const countryOptions = toOptions(countries);
    const moduleOptions = toOptions(modules);
    const reasonOptions = toOptions(reasons);
    const detailOptions = toOptions(details);

    useEffect(() => {
        (async () => {
            if (!reasonId) {
                setDetails([]);
                setDetailId('');
                return;
            }
            try {
                await loadCatalog(apiClient, CATALOGS_DETAILS, setDetails, reasonId);
                setDetailId('');
            } catch {
                setDetails([]);
                setDetailId('');
            }
        })();
    }, [reasonId]);

    useEffect(() => {
        setCountryId('');
    }, [businessUnitId]);

    const formIsValid =
        company.trim().length >= 2 &&
        businessUnitId &&
        countryId &&
        moduleId &&
        reasonId &&
        detailId &&
        description.trim().length >= 2;

    async function postRequest() {
        const validFiles = (files || []).filter(f => !f.err);
        if (validFiles.length !== (files || []).length) {
            setFiles(validFiles);
        }

        let attachments;
        if (validFiles.length > 0) {
            const _files = [];
            for (const _file of validFiles) {
                _files.push({
                    name: _file.name,
                    content: Buffer.from(await _file.arrayBuffer()).toString('base64'),
                });
            }
            attachments = _files;
        }

        setState(STATE_LOADING);
        try {
            const payload = {
                company: company.trim(),
                rut: rut.trim() || null,
                businessUnit: businessUnitId ? Number(businessUnitId) : null,
                country: countryId ? Number(countryId) : null,
                module: moduleId ? Number(moduleId) : null,
                reason: reasonId ? Number(reasonId) : null,
                detail: detailId ? Number(detailId) : null,
                orderId: orderId.trim() || null,
                description: description.trim(),
                clazz: 23,
            };
            if (attachments) payload.attachments = attachments;

            const ticket = await apiClient.postRequest(payload);

            setSubmitted({ ...payload, ticket });
            setState(STATE_FORM_SENT);
        } catch (err) {
            const resp = err?.response;
            const messageFromApi =
                resp?.data?.message ||
                err?.message ||
                'Ocurrió un error inesperado al crear tu caso.';
            const codeFromApi = resp?.data?.code || resp?.status;

            setModalTitle('No pudimos crear tu caso');
            setModalMessage(
                `${messageFromApi}${codeFromApi ? `\nCódigo: ${codeFromApi}` : ''}\n\nIntenta nuevamente más tarde.`
            );
            setModalSeverity('error');
            setModalVisible(true);
            setState(STATE_LOADED);
            return;
        }
    }

    function buildSubmit() {
        return (
            <>
                <div className="rf-border-bottom rf-pt-15"></div>

                <div className="rf-submit-bar">
                    <GenericButton
                        variant="text"
                        className="rf-back-link"
                        onClick={backCallback}
                    >
                        Volver
                    </GenericButton>

                    <GenericButton
                        className="rf-submit-btn"
                        disabled={!formIsValid}
                        onClick={postRequest}
                    >
                        Crear Caso
                    </GenericButton>
                </div>
            </>
        );
    }

    function buildFilesUpload() {
        return (
            <div>
                <h3 className="rf-font-semibold rf-mt-4">Adjunta documentos y/o evidencias</h3>

                <div className="rf-mt-6 rf-mb-3">
                    Asegúrate de que los documentos sean legibles, estén bien iluminados y contengan solo una
                    imagen por archivo.
                </div>

                <AttachmentUploader filenameLength={64} files={files} setFiles={setFiles} multiple />
            </div>
        );
    }

    function buildProviderSection() {
        return (
            <VerticalStepper>
                <Step>
                    <div>
                        <h3 className="rf-font-semibold rf-mt-1">Selecciona tu Unidad de Negocio y País</h3>

                        <GenericSelectFloating
                            className="rf-mt-2"
                            label="Unidad de Negocio"
                            value={businessUnitId}
                            onChange={(e) => setBusinessUnitId(e.target.value)}
                            options={buOptions}
                            required={true}
                            fullWidth
                        />

                        <GenericSelectFloating
                            className="rf-mt-4"
                            label="País"
                            value={countryId}
                            onChange={(e) => setCountryId(e.target.value)}
                            options={toOptions(filteredCountries)}
                            required={true}
                            fullWidth
                            disabled={!businessUnitId}
                        />

                        <h3 className="rf-font-semibold rf-mt-4">Datos de tu empresa</h3>

                        <GenericInput
                            className="rf-mt-2"
                            label="Nombre de la Empresa"
                            value={company}
                            onChange={(e) => setCompany(e.target.value)}
                            placeholder="Empresa S.A."
                            maxLength={32}
                            required
                        />

                        <GenericInput
                            className="rf-mt-4"
                            label="Rut Empresa"
                            value={rut}
                            onChange={(e) => setRut(e.target.value)}
                            placeholder="76.555.444-3"
                            maxLength={32}
                            required={true}
                        />

                        <h3 className="rf-font-semibold rf-mt-4">¿En qué módulo tuviste inconveniente?</h3>

                        <GenericSelectFloating
                            className="rf-mt-2"
                            label="Módulo"
                            value={moduleId}
                            onChange={(e) => setModuleId(e.target.value)}
                            options={moduleOptions}
                            required={true}
                            fullWidth
                        />

                        <h3 className="rf-font-semibold rf-mt-4">Selecciona el motivo</h3>

                        <GenericSelectFloating
                            className="rf-mt-2"
                            label="Motivo"
                            value={reasonId}
                            onChange={(e) => setReasonId(e.target.value)}
                            options={reasonOptions}
                            required={true}
                            fullWidth
                        />

                        <GenericSelectFloating
                            className="rf-mt-4"
                            label="Tipo"
                            value={detailId}
                            onChange={(e) => setDetailId(e.target.value)}
                            options={detailOptions}
                            fullWidth
                            disabled={!reasonId}
                            required={true}
                        />

                        <h3 className="rf-font-semibold rf-mt-4">Ingresa el número de orden de compra</h3>

                        <GenericInput
                            className="rf-mt-2"
                            label="Número de OC"
                            value={orderId}
                            onChange={(e) => setOrderId(e.target.value)}
                            placeholder="77-666-555-444"
                            maxLength={32}
                        />
                    </div>
                </Step>

                <Step>
                    <div>
                        <h3 className="rf-font-semibold rf-mt-1">Describe tu inconveniente o problema</h3>

                        <GenericInput
                            className="rf-mt-2"
                            label="Detalle"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Detalla tu caso"
                            maxLength={300}
                            required={true}
                        />

                        {buildFilesUpload()}
                    </div>
                </Step>
            </VerticalStepper>
        );
    }

    function buildHeader() {
        return (
            <div>
                <div className="rf-title">Crear nuevo caso</div>

                <div className="rf-subtitle">
                    Para crear tu caso, sigue las instrucciones y completa el formulario, asegurándote de
                    proporcionar información precisa y detallada.
                </div>

                <div className="rf-border-bottom rf-mt-10 rf-mb-10"></div>
            </div>
        );
    }

    switch (state) {
        default:
        case STATE_LOADING:
            return (
                <div>
                    <GenericLinearProgress indeterminate value={1} max={3} buffer={1.5} fullWidth />
                </div>
            );

        case STATE_LOADED:
            return (
                <>
                    <GenericModal
                        visible={modalVisible}
                        variant="alert"
                        title={modalTitle}
                        message={modalMessage}
                        severity={modalSeverity}
                        buttonText="Aceptar"
                        onClose={closeModal}
                    />

                    {buildHeader()}

                    <div className="rf-w-lg">
                        {buildProviderSection()}
                        {buildSubmit()}
                    </div>
                </>
            );

        case STATE_FORM_SENT:
            return (
                <div>
                    <RequestConfirm
                        form={submitted}
                        backCallback={backCallback}
                        businessUnits={businessUnits}
                        details={details}
                        modules={modules}
                        reasons={reasons}
                    />
                </div>
            );
    }
}
