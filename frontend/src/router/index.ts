import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue';
import LocationHistoryView from '../views/LocationHistoryView.vue';
import AddLocationView from '../views/AddLocationView.vue';
import { useAuth } from '../services/auth'
import type { Role } from '../types/dto'

interface MetaWithRoles {
  roles?: Role[];
}

const routes: Array<RouteRecordRaw & { meta?: MetaWithRoles }> = [
  { path: '/', name: 'login', component: LoginView },
  { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { roles: ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ROOT'] } },
  { path: '/history', name: 'history', component: LocationHistoryView, meta: { roles: ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ROOT'] } },
  { path: '/add-location', name: 'addLocation', component: AddLocationView, meta: { roles: ['ROLE_ADMIN', 'ROLE_ROOT'] } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _, next) => {
  const auth = useAuth();
  const roles = (to.meta.roles ?? []) as Role[];

  if (roles.length && !auth.isLoggedIn) {
    return next({ name: 'login' });
  }
  
  next();
});


export default router;