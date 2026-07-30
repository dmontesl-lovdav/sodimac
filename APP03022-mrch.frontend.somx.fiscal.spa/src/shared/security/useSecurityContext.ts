import { useCallback, useEffect, useMemo, useState } from 'react';
import { securityService, type AccessContext } from './securityService';
import { getCurrentUserKey } from './currentUserKey';
import { useAppSelector } from '@/store/hooks/useAppSelector';

interface CacheEntry {
    data: AccessContext | null;
    error: unknown;
    expiresAt: number;
    inFlight?: Promise<AccessContext | null>;
}

const CACHE_TTL_MS = 60_000;
const cache = new Map<string, CacheEntry>();

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

function getCached(userKey: string): CacheEntry | null {
    const entry = cache.get(userKey);
    if (!entry) return null;
    if (Date.now() > entry.expiresAt && !entry.inFlight) return null;
    return entry;
}

async function fetchContext(userKey: string): Promise<AccessContext | null> {
    const existing = cache.get(userKey);
    if (existing?.inFlight) return existing.inFlight;

    const promise = securityService
        .getAccessContext(userKey)
        .then((data) => {
            cache.set(userKey, { data, error: null, expiresAt: Date.now() + CACHE_TTL_MS });
            return data;
        })
        .catch((error) => {
            cache.set(userKey, { data: null, error, expiresAt: Date.now() + CACHE_TTL_MS });
            return null;
        });

    cache.set(userKey, {
        data: existing?.data ?? null,
        error: null,
        expiresAt: Date.now() + CACHE_TTL_MS,
        inFlight: promise,
    });
    return promise;
}

export interface SecurityContextResult {
    isLoading: boolean;
    error: unknown;
    userKey: string;
    raw: AccessContext | null;
    apps: { key: string; events?: { key: string; name?: string }[] }[];
    profiles: { key: string }[];
    roles: { key: string }[];
    permissions: { key: string }[];
    hasApp: (appKey: string) => boolean;
    hasAnyApp: (appKeys: string[]) => boolean;
    hasEvent: (appKey: string, eventKey: string) => boolean;
    hasEventLabel: (appKey: string, label: string) => boolean;
    hasEventInAnyApp: (eventKey: string) => boolean;
    hasPermission: (permissionKey: string) => boolean;
    hasProfile: (profileKey: string) => boolean;
}

export function useSecurityContext(): SecurityContextResult {
    useAppSelector((s) => s.authentication);
    const userKey = getCurrentUserKey();
    const cached = userKey ? getCached(userKey) : null;
    const cachedData = cached?.data ?? null;
    const cachedError = cached?.error ?? null;

    const [data, setData] = useState<AccessContext | null>(cachedData);
    const [error, setError] = useState<unknown>(cachedError);
    const [isLoading, setIsLoading] = useState<boolean>(!cachedData && Boolean(userKey));

    useEffect(() => {
        if (!userKey) {
            setIsLoading(false);
            return;
        }
        const hit = getCached(userKey);
        if (hit?.data) {
            setData(hit.data);
            setError(hit.error ?? null);
            setIsLoading(false);
            return;
        }
        let cancelled = false;
        setIsLoading(true);
        fetchContext(userKey)
            .then((resp) => {
                if (cancelled) return;
                setData(resp);
                const entry = cache.get(userKey);
                setError(entry?.error ?? null);
            })
            .catch((err) => {
                if (cancelled) return;
                setError(err);
                setData(null);
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });
        return () => {
            cancelled = true;
        };
    }, [userKey]);

    const apps = useMemo(() => data?.apps ?? [], [data]);
    const profiles = useMemo(() => data?.profiles ?? [], [data]);
    const roles = useMemo(() => data?.roles ?? [], [data]);
    const permissions = useMemo(() => data?.permissions ?? [], [data]);

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
    const eventGlobal = useMemo(() => {
        const set = new Set<string>();
        for (const app of apps) {
            for (const ev of app.events ?? []) {
                if (ev.key) set.add(ev.key);
            }
        }
        return set;
    }, [apps]);
    const permSet = useMemo(() => new Set(permissions.map((p) => p.key)), [permissions]);
    const profileSet = useMemo(() => new Set(profiles.map((p) => p.key)), [profiles]);

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
        (eventKey: string) => Boolean(eventKey) && eventGlobal.has(eventKey),
        [eventGlobal],
    );
    const hasPermission = useCallback(
        (permKey: string) => Boolean(permKey) && permSet.has(permKey),
        [permSet],
    );
    const hasProfile = useCallback(
        (profileKey: string) => Boolean(profileKey) && profileSet.has(profileKey),
        [profileSet],
    );

    return {
        isLoading,
        error,
        userKey,
        raw: data,
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
    };
}
