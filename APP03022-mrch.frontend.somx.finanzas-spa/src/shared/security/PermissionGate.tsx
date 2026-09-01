import type { ReactNode } from 'react';
import { useSecurityContext } from './useSecurityContext';

interface PermissionGateProps {
    app?: string;
    anyApp?: string[];
    event?: string;
    appEvent?: { app: string; event: string; label?: string };
    permission?: string;
    profile?: string;
    hideWhileLoading?: boolean;
    fallback?: ReactNode;
    children: ReactNode;
}

export function PermissionGate({
    app,
    anyApp,
    event,
    appEvent,
    permission,
    profile,
    hideWhileLoading = true,
    fallback = null,
    children,
}: PermissionGateProps) {
    const sec = useSecurityContext();
    

    if (sec.isLoading) {
        return hideWhileLoading ? <>{fallback}</> : <>{children}</>;
    }
    if (sec.error) {
        return <>{fallback}</>;
    }

    const checks: boolean[] = [];
    if (app !== undefined)         checks.push(sec.hasApp(app));
    if (anyApp !== undefined)      checks.push(sec.hasAnyApp(anyApp));
    if (event !== undefined)       checks.push(sec.hasEventInAnyApp(event));
    if (appEvent !== undefined) {
        const byKey = sec.hasEvent(appEvent.app, appEvent.event);
        const byLabel = appEvent.label ? sec.hasEventLabel(appEvent.app, appEvent.label) : false;
        checks.push(byKey || byLabel);
    }
    if (permission !== undefined)  checks.push(sec.hasPermission(permission));
    if (profile !== undefined)     checks.push(sec.hasProfile(profile));

    if (checks.length === 0) return <>{children}</>;
    const allowed = checks.every(Boolean);
    return allowed ? <>{children}</> : <>{fallback}</>;
}
