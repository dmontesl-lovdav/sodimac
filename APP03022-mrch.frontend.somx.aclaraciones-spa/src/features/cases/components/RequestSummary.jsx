// @ts-nocheck
import { useEffect, useState } from 'react';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { translateDate, translateHour, translateIdToString } from './RequestUtils';
import { buildStatusPill } from '@shared/utils/statusPill';
import AttachmentSummary from '../utils/AttachmentSummary';
import CommentSummary from '../utils/CommentSummary';
import RequestConfigure from './RequestConfigure';
import { GenericButton } from '@shared/components/ui';
import { useAppSelector } from '@/store/hooks/useAppSelector';
import '../styles/RequestSummary.css';

export default function RequestSummary({
    requestId,
    form,
    backCallback,
    businessUnits,
    modules,
    reasons,
    details,
}) {
    const STATE_LOADING = 1;
    const STATE_LOADED = 2;
    const STATE_CHANGE_LOADED = 3;

    const [request, setRequest] = useState({});
    const [state, setState] = useState(STATE_LOADING);

    const roles =
        useAppSelector(
            (s) => s.authentication?.tokenDecoded?.resource_access?.['fbc-aclaraciones']?.roles
        ) || [];

    const isVendor = Array.isArray(roles) && roles.includes('ppsomx-vendor');

    async function waitForRequest() {
        try {
            if (form) {
                setRequest(form);
                return;
            }

            const response = await ConfigurationBuilder.client.getRequest(requestId);
            const [comments, attachments] = await Promise.all([
                ConfigurationBuilder.client.getRequestComments(requestId),
                ConfigurationBuilder.client.getRequestAttachments(requestId),
            ]);

            response.comments = comments || [];
            response.attachments = attachments || [];
            setRequest(response);
        } catch (error) {
            console.error('Error loading request:', error);
        } finally {
            setState(STATE_LOADED);
        }
    }

    useEffect(() => {
        if (state === STATE_LOADING) waitForRequest();
    }, [state]);

    const Header = () => {
        const statusClazz = request?.clazz ?? request?.status;
        const pillHtml = buildStatusPill(statusClazz);

        return (
            <div className="rs-header">
                <div className="rs-header-title">
                    Solicitud {request.id}
                </div>
                <span dangerouslySetInnerHTML={{ __html: pillHtml }} />
            </div>
        );
    };

    const Body = () => (
        <div>
            <Header />

            <div className="rs-item">
                <span className="rs-label">Fecha solicitud:</span>
                {translateDate(request.creationTime)}
            </div>

            <div className="rs-item">
                <span className="rs-label">Hora:</span>
                {translateHour(request.creationTime)}
            </div>

            <div className="rs-item">
                <span className="rs-label">Unidad de negocio:</span>
                {translateIdToString(request.businessUnit, businessUnits)}
            </div>

            <div className="rs-item">
                <span className="rs-label">Módulo:</span>
                {translateIdToString(request.module, modules)}
            </div>

            <div className="rs-item">
                <span className="rs-label">Motivo:</span>
                {translateIdToString(request.reason, reasons)}
            </div>

            <div className="rs-item">
                <span className="rs-label">Tipo:</span>
                {translateIdToString(request.detail, details)}
            </div>

            <div className="rs-item">
                <span className="rs-label">Descripción del caso:</span>
                {request.description}
            </div>

            <div className="rs-divider">
                <CommentSummary comments={request.comments} />
            </div>

            <div className="rs-attachments">
                <AttachmentSummary
                    requestId={request.id}
                    attachments={request.attachments}
                />
            </div>

            <div className="rs-footer rs-footer-actions">
                <GenericButton
                    variant="text"
                    className="rs-back-link"
                    onClick={backCallback}
                >
                    Volver
                </GenericButton>

                {!isVendor && (
                    <GenericButton
                        variant="outline"
                        className="rs-modify-btn"
                        onClick={() => setState(STATE_CHANGE_LOADED)}
                    >
                        Modificar Solicitud
                    </GenericButton>
                )}
            </div>
        </div>
    );

    switch (state) {
        case STATE_LOADING:
            return (
                <div className="rs-loading">
                    <div className="rs-loading-bar">
                        <div className="rs-loading-bar-inner" />
                    </div>
                </div>
            );

        case STATE_CHANGE_LOADED:
            return (
                <RequestConfigure
                    request={request}
                    backCallback={() => setState(STATE_LOADING)}
                    businessUnits={businessUnits}
                    modules={modules}
                    reasons={reasons}
                    details={details}
                />
            );

        case STATE_LOADED:
        default:
            return <div className="rs-wrapper"><Body /></div>;
    }
}
