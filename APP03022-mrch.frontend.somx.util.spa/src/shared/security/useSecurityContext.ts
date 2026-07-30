import { useQuery } from '@tanstack/react-query';
import { useCallback, useMemo } from 'react';
import { securityService } from '@features/security/services/securityService';
import { getCurrentUserCatalogKey } from '@features/security/utils/currentUserCatalogKey';

interface AccessContextEvent {
    key: string;
    name?: string;
}

interface AccessContextApp {
    key: string;
    name?: string;
    events?: AccessContextEvent[];
}

interface AccessContextEntity {
    key: string;
    name?: string;
}

interface AccessContextPermission extends AccessContextEntity {
    rol?: AccessContextEntity;
}

interface AccessContext {
    user?: { key?: string; name?: string; email?: string };
    profiles?: AccessContextEntity[];
    apps?: AccessContextApp[];
    roles?: AccessContextEntity[];
    permissions?: AccessContextPermission[];
    providers?: AccessContextPermission[];
}

interface ApiEnvelope<T> {
    success?: boolean;
    data?: T;
}

const SECURITY_QUERY_KEY = (userKey: string) => ['security', 'access-context', userKey];

const normalizeLabel = (s: string): string =>
    s
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .trim()
        .toLowerCase()
        .replace(/\s+/g, ' ');

const ADMIN_PROFILE_KEYS =
    typeof process !== 'undefined' && process.env.SECURITY_ADMIN_PROFILE_KEYS
        ? process.env.SECURITY_ADMIN_PROFILE_KEYS.split(',').map((s) => s.trim()).filter(Boolean)
        : [];

interface UseSecurityContextOptions {
    enabled?: boolean;
}

export interface SecurityContextResult {
    isLoading: boolean;
    error: unknown;
    userKey: string;
    isAdmin: boolean;
    raw: AccessContext | null;
    apps: AccessContextApp[];
    profiles: AccessContextEntity[];
    roles: AccessContextEntity[];
    permissions: AccessContextPermission[];
    hasApp: (appKey: string) => boolean;
    hasAnyApp: (appKeys: string[]) => boolean;
    hasEvent: (appKey: string, eventKey: string) => boolean;
    hasEventLabel: (appKey: string, label: string) => boolean;
    hasEventInAnyApp: (eventKey: string) => boolean;
    hasPermission: (permissionKey: string) => boolean;
    hasProfile: (profileKey: string) => boolean;
    hasRole: (roleKey: string) => boolean;
}

export function useSecurityContext(opts: UseSecurityContextOptions = {}): SecurityContextResult {
    const { enabled = true } = opts;
    const userKey = getCurrentUserCatalogKey();

    const query = useQuery({
        queryKey: SECURITY_QUERY_KEY(userKey),
        queryFn: async () => {
            const resp = (await securityService.getAccessContext(userKey)) as
                | AccessContext
                | ApiEnvelope<AccessContext>
                | null;
            if (resp && typeof resp === 'object' && 'data' in resp && resp.data) {
                return resp.data;
            }
            return (resp as AccessContext | null) ?? null;
        },
        enabled: enabled && Boolean(userKey),
        staleTime: 60_000,
        retry: false,
    });

    const ctx = query.data ?? null;

    const apps = useMemo<AccessContextApp[]>(() => ctx?.apps ?? [], [ctx]);
    const profiles = useMemo<AccessContextEntity[]>(() => ctx?.profiles ?? [], [ctx]);
    const roles = useMemo<AccessContextEntity[]>(() => ctx?.roles ?? [], [ctx]);
    const permissions = useMemo<AccessContextPermission[]>(() => ctx?.permissions ?? [], [ctx]);

    const appKeySet = useMemo(() => new Set(apps.map((a) => a.key)), [apps]);
    const eventByApp = useMemo(() => {
        const map = new Map<string, Set<string>>();
        for (const app of apps) {
            map.set(app.key, new Set((app.events ?? []).map((e) => e.key)));
        }
        return map;
    }, [apps]);
    const labelByApp = useMemo(() => {
        const map = new Map<string, Set<string>>();
        for (const app of apps) {
            map.set(
                app.key,
                new Set(
                    (app.events ?? [])
                        .map((e) => normalizeLabel(e.name ?? ''))
                        .filter((s) => s !== ''),
                ),
            );
        }
        return map;
    }, [apps]);
    const eventGlobalSet = useMemo(() => {
        const set = new Set<string>();
        for (const app of apps) {
            for (const ev of app.events ?? []) {
                if (ev.key) set.add(ev.key);
            }
        }
        return set;
    }, [apps]);
    const permKeySet = useMemo(() => new Set(permissions.map((p) => p.key)), [permissions]);
    const profileKeySet = useMemo(() => new Set(profiles.map((p) => p.key)), [profiles]);
    const roleKeySet = useMemo(() => new Set(roles.map((r) => r.key)), [roles]);

    const hasApp = useCallback((key: string) => Boolean(key) && appKeySet.has(key), [appKeySet]);
    const hasAnyApp = useCallback(
        (keys: string[]) => keys.some((k) => Boolean(k) && appKeySet.has(k)),
        [appKeySet],
    );
    const hasEvent = useCallback(
        (appKey: string, eventKey: string) => {
            if (!appKey || !eventKey) return false;
            return eventByApp.get(appKey)?.has(eventKey) ?? false;
        },
        [eventByApp],
    );
    const hasEventLabel = useCallback(
        (appKey: string, label: string) => {
            if (!appKey || !label) return false;
            return labelByApp.get(appKey)?.has(normalizeLabel(label)) ?? false;
        },
        [labelByApp],
    );
    const hasEventInAnyApp = useCallback(
        (eventKey: string) => Boolean(eventKey) && eventGlobalSet.has(eventKey),
        [eventGlobalSet],
    );
    const hasPermission = useCallback(
        (permKey: string) => Boolean(permKey) && permKeySet.has(permKey),
        [permKeySet],
    );
    const hasProfile = useCallback(
        (profileKey: string) => Boolean(profileKey) && profileKeySet.has(profileKey),
        [profileKeySet],
    );
    const hasRole = useCallback(
        (roleKey: string) => Boolean(roleKey) && roleKeySet.has(roleKey),
        [roleKeySet],
    );

    const isAdmin =
        ADMIN_PROFILE_KEYS.length === 0
            ? true
            : profiles.some((p) => ADMIN_PROFILE_KEYS.includes(p.key));

    return {
        isLoading: query.isLoading,
        error: query.error,
        userKey,
        isAdmin,
        raw: ctx,
        apps,
        profiles,
        roles,
        permissions,
        hasApp,
        hasAnyApp,
        hasEvent,
        hasEventLabel,
        hasEventInAnyApp,
        hasPermission,
        hasProfile,
        hasRole,
    };
}
