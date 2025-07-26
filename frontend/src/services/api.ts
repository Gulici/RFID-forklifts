import axios, { AxiosHeaders, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/'
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
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

export default api