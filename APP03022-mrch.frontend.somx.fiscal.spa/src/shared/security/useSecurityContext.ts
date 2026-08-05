import { useCallback, useEffect, useMemo, useState } from 'react';
import { securityService, type AccessContext } from './securityService';
import { getCurrentUserKey } from './currentUserKey';
import { useAppSelector } from '@/store/hooks/useAppSelector';

interface CacheEntry {
    data: AccessContext | null;
    error: unknown;
    inFlight?: Promise<AccessContext | null>;
}

const cache = new Map<string, CacheEntry>();

const ADMIN_PROFILE_KEYS = ['PER009'];
const ADMIN_ROLE_KEYS = ['ROL010'];

const normalizeLabel = (s: string): string =>
    s
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .trim()
        .toLowerCase()
        .replace(/\s+/g, ' ');

/** Limpia la caché de seguridad (solo para tests). */
export function clearSecurityContextCache(): void {
    cache.clear();
}

function storeCache(
    userKey: string,
    data: AccessContext | null,
    error: unknown,
    inFlight?: Promise<AccessContext | null>,
): void {
    cache.set(userKey, { data, error, inFlight });
}

async function fetchContext(userKey: string): Promise<AccessContext | null> {
    const existing = cache.get(userKey);
    if (existing?.inFlight) return existing.inFlight;

    const promise = securityService
        .getAccessContext(userKey)
        .then((data) => {
            storeCache(userKey, data, null);
            return data;
        })
        .catch((error) => {
            storeCache(userKey, null, error);
            return null;
        });

    let previousData: AccessContext | null = null;
    if (existing) previousData = existing.data;
    storeCache(userKey, previousData, null, promise);
    return promise;
}

async function loadAccessContext(
    userKey: string,
): Promise<{ data: AccessContext | null; error: unknown }> {
    const hit = cache.get(userKey);
    if (hit?.data) {
        return { data: hit.data, error: hit.error };
    }

    const data = await fetchContext(userKey);
    return { data, error: cache.get(userKey)?.error };
}

function resolveIsAdmin(profiles: { key: string }[], roles: { key: string }[]): boolean {
    if (profiles.some((p) => ADMIN_PROFILE_KEYS.includes(p.key))) return true;
    return roles.some((r) => ADMIN_ROLE_KEYS.includes(r.key));
}

type AppEntry = { key: string; events?: { key: string; name?: string }[] };

interface AppIndexes {
    appKeySet: Set<string>;
    eventByApp: Map<string, Set<string>>;
    labelByApp: Map<string, Set<string>>;
    eventGlobal: Set<string>;
}

function listOrEmpty<T>(items: T[] | undefined): T[] {
    if (items) return items;
    return [];
}

/** Construye los índices de búsqueda usados por los checkers de permisos. */
function buildAppIndexes(apps: AppEntry[]): AppIndexes {
    const appKeySet = new Set(apps.map((a) => a.key));
    const eventByApp = new Map<string, Set<string>>();
    const labelByApp = new Map<string, Set<string>>();
    const eventGlobal = new Set<string>();

    for (const app of apps) {
        const events = listOrEmpty(app.events);
        const eventKeys = new Set<string>();
        const labels = new Set<string>();

        for (const ev of events) {
            eventKeys.add(ev.key);
            eventGlobal.add(ev.key);
            labels.add(normalizeLabel(ev.name || ''));
        }
        labels.delete('');

        eventByApp.set(app.key, eventKeys);
        labelByApp.set(app.key, labels);
    }

    return { appKeySet, eventByApp, labelByApp, eventGlobal };
}

function evaluateCan(
    appEvent: { app: string; event: string; label?: string },
    isAdmin: boolean,
    eventByApp: Map<string, Set<string>>,
    labelByApp: Map<string, Set<string>>,
): boolean {
    if (isAdmin) return true;
    if (!appEvent.app) return false;
    if (eventByApp.get(appEvent.app)?.has(appEvent.event)) return true;
    if (!appEvent.label) return false;
    return labelByApp.get(appEvent.app)?.has(normalizeLabel(appEvent.label)) === true;
}

export interface SecurityContextResult {
    isLoading: boolean;
    error: unknown;
    userKey: string;
    isAdmin: boolean;
    raw: AccessContext | null;
    apps: { key: string; events?: { key: string; name?: string }[] }[];
    profiles: { key: string }[];
    roles: { key: string }[];
    permissions: { key: string }[];
    hasApp: (appKey: string) => boolean;
    hasAnyApp: (appKeys: string[]) => boolean;
    hasEvent: (appKey: string, eventKey: string) => boolean;
    hasEventLabel: (appKey: string, label: string) => boolean;
    can: (appEvent: { app: string; event: string; label?: string }) => boolean;
    hasEventInAnyApp: (eventKey: string) => boolean;
    hasPermission: (permissionKey: string) => boolean;
    hasProfile: (profileKey: string) => boolean;
}

/** Carga (o reutiliza de caché) el contexto de acceso del usuario actual. */
function useAccessContextState(userKey: string) {
    const [data, setData] = useState<AccessContext | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (!userKey) {
            setData(null);
            setError(null);
            setIsLoading(false);
            return;
        }

        let cancelled = false;
        setIsLoading(true);
        loadAccessContext(userKey).then((resolution) => {
            if (cancelled) return;
            setData(resolution.data);
            setError(resolution.error);
            setIsLoading(false);
        });
        return () => {
            cancelled = true;
        };
    }, [userKey]);

    return { data, error, isLoading };
}

export function useSecurityContext(): SecurityContextResult {
    useAppSelector((s) => s.authentication);
    const userKey = getCurrentUserKey();
    const { data, error, isLoading } = useAccessContextState(userKey);

    const apps = useMemo(() => listOrEmpty(data?.apps), [data]);
    const profiles = useMemo(() => listOrEmpty(data?.profiles), [data]);
    const roles = useMemo(() => listOrEmpty(data?.roles), [data]);
    const permissions = useMemo(() => listOrEmpty(data?.permissions), [data]);

    const { appKeySet, eventByApp, labelByApp, eventGlobal } = useMemo(
        () => buildAppIndexes(apps),
        [apps],
    );
    const permSet = useMemo(() => new Set(permissions.map((p) => p.key)), [permissions]);
    const profileSet = useMemo(() => new Set(profiles.map((p) => p.key)), [profiles]);
    const isAdmin = useMemo(() => resolveIsAdmin(profiles, roles), [profiles, roles]);

    const hasApp = useCallback((key: string) => appKeySet.has(key), [appKeySet]);
    const hasAnyApp = useCallback(
        (keys: string[]) => keys.some((k) => appKeySet.has(k)),
        [appKeySet],
    );
    const hasEvent = useCallback(
        (appKey: string, eventKey: string) => eventByApp.get(appKey)?.has(eventKey) === true,
        [eventByApp],
    );
    const hasEventLabel = useCallback(
        (appKey: string, label: string) =>
            labelByApp.get(appKey)?.has(normalizeLabel(label)) === true,
        [labelByApp],
    );
    const can = useCallback(
        (appEvent: { app: string; event: string; label?: string }) =>
            evaluateCan(appEvent, isAdmin, eventByApp, labelByApp),
        [eventByApp, labelByApp, isAdmin],
    );
    const hasEventInAnyApp = useCallback(
        (eventKey: string) => eventGlobal.has(eventKey),
        [eventGlobal],
    );
    const hasPermission = useCallback((permKey: string) => permSet.has(permKey), [permSet]);
    const hasProfile = useCallback(
        (profileKey: string) => profileSet.has(profileKey),
        [profileSet],
    );

    return {
        isLoading,
        error,
        userKey,
        isAdmin,
        raw: data,
        apps,
        profiles,
        roles,
        permissions,
        hasApp,
        hasAnyApp,
        hasEvent,
        hasEventLabel,
        can,
        hasEventInAnyApp,
        hasPermission,
        hasProfile,
    };
}
