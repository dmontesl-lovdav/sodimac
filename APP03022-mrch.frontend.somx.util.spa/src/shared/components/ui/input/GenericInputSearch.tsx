import React from 'react';
import './styles/GenericInputSearch.css';

interface GenericInputSearchProps
    extends React.InputHTMLAttributes<HTMLInputElement> {
    width?: string;
    fullWidth?: boolean;
}

export default function GenericInputSearch({
    width,
    fullWidth = false,
    className = '',
    style,
    ...props
}: GenericInputSearchProps) {
    return (
        <input
            {...props}
            className={`generic-input ${fullWidth ? 'generic-input-full' : ''} ${className}`}
            style={{
                width: fullWidth ? '100%' : width,
                ...style,
            }}
        />
    );
}
