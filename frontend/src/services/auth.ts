import { reactive, computed } from 'vue';
import axios from './api';
import router from '../router';
import {jwtDecode} from 'jwt-decode';
import type { JwtPayload, LoginResponse, Role } from '../types/dto';

interface AuthState {
  token: string | null;
  user: string | null;
  roles: Role[];
}

interface AuthStore extends AuthState {
  isLoggedIn: boolean;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  loadFromStorage: () => void;
}

let authInstance: AuthStore | null = null;

export function useAuth(): AuthStore {
  if (authInstance) return authInstance;

  const state = reactive<AuthState>({
    token: null,
    user: null,
    roles: [],
  });

  const isLoggedIn = computed(() => !!state.token);
  const isAdmin = computed(() => state.roles.includes('ROLE_ADMIN') || state.roles.includes('ROLE_ROOT'));

  function setToken(newToken: string) {
    if (!newToken || newToken.split('.').length !== 3) {
      console.error('Invalid token', newToken);
      return;
    }

    state.token = newToken;
    localStorage.setItem('token', newToken);

    const decoded = jwtDecode<JwtPayload>(newToken);
    state.user = decoded.sub;
    state.roles = (decoded.roles as Role[]) || [];
  }

  async function login(username: string, password: string) {
    try {
      const res = await axios.post<LoginResponse>('/auth/login', { username, password });
      setToken(res.data.jwt);
      router.push('/dashboard');
    } catch (err) {
      console.error('Login failed', err);
      throw err;
    }
  }

  function logout() {
    state.token = null;
    state.user = null;
    state.roles = [];
    localStorage.removeItem('token');
    router.push('/');
  }

  function loadFromStorage() {
    const savedToken = localStorage.getItem('token');
    if (savedToken) setToken(savedToken);
  }

  authInstance = {
    ...state,
    get isLoggedIn() { return isLoggedIn.value; },
    get isAdmin() { return isAdmin.value; },
    login,
    logout,
    loadFromStorage,
  };

  return authInstance;
}
