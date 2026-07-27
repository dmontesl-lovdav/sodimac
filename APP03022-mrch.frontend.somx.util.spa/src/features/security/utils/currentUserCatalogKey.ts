import { localHomeStore } from '@/store/localStore';

interface DecodedToken {
  sub?: string;
  preferred_username?: string;
  email?: string;
}

export function getCurrentUserCatalogKey(): string {
  if (typeof window !== 'undefined') {
    const w = window as unknown as { __UTIL_USER_KEY__?: string };
    if (w.__UTIL_USER_KEY__) return w.__UTIL_USER_KEY__;
  }

  try {
    const state = localHomeStore.getState() as unknown as {
      authentication?: { tokenDecoded?: DecodedToken };
    };
    const decoded = state.authentication?.tokenDecoded;
    if (decoded?.sub) return decoded.sub;
    if (decoded?.preferred_username) return decoded.preferred_username;
    if (decoded?.email) return decoded.email;
  } catch {
  }

    const fromEnv = typeof process !== 'undefined' ? process.env.UTIL_CURRENT_USER_KEY : undefined;
    return fromEnv ?? 'TEST_USER_01';
}
