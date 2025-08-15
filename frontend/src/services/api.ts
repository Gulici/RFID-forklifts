import axios, { AxiosHeaders, InternalAxiosRequestConfig, AxiosInstance } from 'axios'
import { useAuth } from './auth';

const instance: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/',
  headers: {
    'Content-Type': 'application/json',
  },
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')
  if (token) {
    if (!config.headers) {
      config.headers = new AxiosHeaders()
    } else if (!(config.headers instanceof AxiosHeaders)) {
      config.headers = new AxiosHeaders(config.headers)
    }
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

instance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const auth = useAuth();
      auth.logout();
    }
    return Promise.reject(error);
  }
);

export default instance 