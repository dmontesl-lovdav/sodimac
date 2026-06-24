import { useQuery } from '@tanstack/react-query';
import { securityService } from '../services/securityService';
import { getCurrentUserCatalogKey } from '../utils/currentUserCatalogKey';

type ContextProfiles = { profiles?: { key: string }[] };

const ADMIN_KEYS =
  typeof process !== 'undefined' && process.env.SECURITY_ADMIN_PROFILE_KEYS
    ? process.env.SECURITY_ADMIN_PROFILE_KEYS.split(',')
        .map((s) => s.trim())
        .filter(Boolean)
    : [];

export function useSecurityAdmin(enabled = true) {
  const userKey = getCurrentUserCatalogKey();
  const query = useQuery({
    queryKey: ['security', 'access-context', userKey],
    queryFn: async () => {
      try {
        return (await securityService.getAccessContext(userKey)) as ContextProfiles;
      } catch {
        return { profiles: [] };
      }
    },
    enabled: enabled && Boolean(userKey),
    staleTime: 60_000,
  });

  const isAdmin =
    ADMIN_KEYS.length === 0 ||
    Boolean(query.data?.profiles?.some((p) => ADMIN_KEYS.includes(p.key)));

  return { isAdmin, isLoading: query.isLoading, error: query.error, userKey };
}
