import React from 'react';
import type { MenuItem } from '../components/SideMenu';

export type DemoId =
    | 'buttons'
    | 'inputs'
    | 'selects'
    | 'selectBasic'
    | 'upload'
    | 'attachments'
    | 'daterange'
    | 'breadcrumb'
    | 'modal'
    | 'table'
    | 'stepper';

export const MENU: MenuItem[] = [
    { id: 'buttons', label: 'Buttons' },
    { id: 'inputs', label: 'Inputs' },
    { id: 'selects', label: 'Selects' },
    { id: 'selectBasic', label: 'Select (básico)' },
    { id: 'upload', label: 'Dropzone' },
    { id: 'attachments', label: 'AttachmentUploader' },
    { id: 'daterange', label: 'DateRangePicker' },
    { id: 'breadcrumb', label: 'Breadcrumb' },
    { id: 'modal', label: 'Modal' },
    { id: 'table', label: 'Table' },
    { id: 'stepper', label: 'Stepper' },
];

export const DEMOS: Record<DemoId, React.LazyExoticComponent<React.ComponentType<any>>> = {
    buttons: React.lazy(() => import('./ButtonsDemo')),
    inputs: React.lazy(() => import('./InputsDemo')),
    selects: React.lazy(() => import('./SelectsDemo')),
    selectBasic: React.lazy(() => import('./SelectBasicDemo')),
    upload: React.lazy(() => import('./UploadDemo')),
    attachments: React.lazy(() => import('./AttachmentsDemo')),
    daterange: React.lazy(() => import('./DateRangeDemo')),
    breadcrumb: React.lazy(() => import('./BreadcrumbDemo')),
    modal: React.lazy(() => import('./ModalDemo')),
    table: React.lazy(() => import('./TableDemo')),
    stepper: React.lazy(() => import('./StepperDemo')),
};
