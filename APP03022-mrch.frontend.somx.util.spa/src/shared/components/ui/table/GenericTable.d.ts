import { FC, ReactNode } from 'react';
import { NavigateFunction } from 'react-router-dom';

type Align = 'left' | 'center' | 'right';

export interface Column<T = any> {
    header: string | ReactNode;
    align?: Align;
    render: (row: T, nav?: NavigateFunction) => ReactNode;
}

export interface RowAction<T = any> {
    title: string;
    icon: string;
    onClick: (row: T, nav?: NavigateFunction) => void;
    isDisabled?: (row: T) => boolean;
}

export interface SwitchProps {
    on: boolean;
    onClick: () => void;
}

export declare function Switch(props: SwitchProps): JSX.Element;

interface GenericTableProps<T = any> {
    rows?: T[];
    columns?: Column<T>[];
    actions?: RowAction<T>[];
    emptyLabel?: string;
    perPage?: number;
    page?: number;
    totalPages?: number;
    totalItems?: number;
    onChangePerPage?: (value: number) => void;
    onChangePage?: (value: number) => void;
    enableSelection?: boolean;
    selectedIds?: any[];
    onSelectRow?: (id: any, selected: boolean) => void;
    selectionHeader?: ReactNode;
    showPagination?: boolean;
    showPageSizeSelector?: boolean;
}

declare function GenericTable<T = any>(props: GenericTableProps<T>): JSX.Element;
export default GenericTable;
