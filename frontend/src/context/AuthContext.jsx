import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { useNavigate } from 'react-router-dom';
import {
  setApiCredentialsGetter,
  setApiUnauthorizedHandler,
} from '../services/apiClient.js';

const AuthContext = createContext(null);

const USER_ROLE = 'USER';

function normalizePrincipal(summary) {
  if (!summary || typeof summary !== 'object') {
    return {
      role: USER_ROLE,
      accountId: null,
      linkedUserId: null,
      linkedRestaurantId: null,
      linkedCourierId: null,
      linkedAdminId: null,
    };
  }
  return {
    ...summary,
    role: typeof summary.role === 'string' && summary.role ? summary.role : USER_ROLE,
    accountId: summary.accountId ?? null,
    linkedUserId: summary.linkedUserId ?? null,
    linkedRestaurantId: summary.linkedRestaurantId ?? null,
    linkedCourierId: summary.linkedCourierId ?? null,
    linkedAdminId: summary.linkedAdminId ?? null,
  };
}

export function getRoleHomeRoute(role) {
  switch (role) {
    case 'ADMIN':
      return '/admin';
    case 'COURIER':
      return '/courier';
    case 'RESTAURANT':
      return '/restaurant';
    case 'USER':
    default:
      return '/';
  }
}

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [email, setEmail] = useState(null);
  const [password, setPassword] = useState(null);
  const credentialsRef = useRef({ email: null, password: null });

  const isAuthenticated = Boolean(
    email && password != null && String(password).length > 0
  );
  const role = user?.role ?? USER_ROLE;
  const accountId = user?.accountId ?? null;
  const linkedUserId = user?.linkedUserId ?? null;
  const linkedRestaurantId = user?.linkedRestaurantId ?? null;
  const linkedCourierId = user?.linkedCourierId ?? null;
  const linkedAdminId = user?.linkedAdminId ?? null;
  const homeRoute = getRoleHomeRoute(role);

  const applyApiCredentials = useCallback((nextEmail, nextPassword) => {
    credentialsRef.current = { email: nextEmail, password: nextPassword };
    if (!nextEmail || nextPassword == null || String(nextPassword).length === 0) {
      return;
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setEmail(null);
    setPassword(null);
    applyApiCredentials(null, null);
  }, [applyApiCredentials]);

  const loginWithCredentials = useCallback((nextUser, nextEmail, nextPassword) => {
    setUser(normalizePrincipal(nextUser));
    setEmail(nextEmail ?? null);
    setPassword(nextPassword ?? null);
    applyApiCredentials(nextEmail ?? null, nextPassword ?? null);
  }, [applyApiCredentials]);

  const handleUnauthorized = useCallback(() => {
    setUser(null);
    setEmail(null);
    setPassword(null);
    applyApiCredentials(null, null);
    navigate('/login', { replace: true, state: { sessionExpired: true } });
  }, [applyApiCredentials, navigate]);

  useEffect(() => {
    setApiCredentialsGetter(() => {
      const current = credentialsRef.current;
      if (!current.email || current.password == null || String(current.password).length === 0) {
        return null;
      }
      return { email: current.email, password: current.password };
    });
    return () => {
      setApiCredentialsGetter(() => null);
    };
  }, []);

  useEffect(() => {
    setApiUnauthorizedHandler(handleUnauthorized);
    return () => setApiUnauthorizedHandler(() => {});
  }, [handleUnauthorized]);

  const value = useMemo(
    () => ({
      user,
      email,
      role,
      accountId,
      linkedUserId,
      linkedRestaurantId,
      linkedCourierId,
      linkedAdminId,
      homeRoute,
      isAuthenticated,
      loginWithCredentials,
      logout,
    }),
    [
      user,
      email,
      role,
      accountId,
      linkedUserId,
      linkedRestaurantId,
      linkedCourierId,
      linkedAdminId,
      homeRoute,
      isAuthenticated,
      loginWithCredentials,
      logout,
    ]
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
