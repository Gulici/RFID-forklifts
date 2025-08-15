import type { Role } from '../types/dto';

declare module 'vue-router' {
  interface RouteMeta {
    roles?: Role[];
  }
}