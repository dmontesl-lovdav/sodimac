import React from 'react';
import { useAppSelector } from '@/store/hooks/useAppSelector';

export default function RolesDebug() {
    const roles =
        useAppSelector(
            (s) =>
                s.authentication?.tokenDecoded?.resource_access?.['fbc-aclaraciones']
                    ?.roles
        ) || [];

    return (
        <pre
            style={{
                padding: 16,
                background: '#111',
                color: '#eee',
                borderRadius: 8,
                fontSize: 14,
                lineHeight: 1.5,
            }
            }
        >
            {JSON.stringify(roles, null, 2)}
        </pre>
    );
}
