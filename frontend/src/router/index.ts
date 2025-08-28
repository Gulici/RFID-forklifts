import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue';
import LocationHistoryView from '../views/LocationHistoryView.vue';
import LocationsView from '../views/LocationsView.vue';
import CompaniesView from '../views/CompaniesView.vue';
import ForbiddenView from '../views/ForbiddenView.vue';
import NotFoundView from '../views/NotFoundView.vue';
import { useAuth } from '../services/auth'
import type { Role } from '../types/dto'
import DeviceDetailsView from '../views/DeviceDetailsView.vue';

interface MetaWithRoles {
  roles?: Role[];
}

const routes: Array<RouteRecordRaw & { meta?: MetaWithRoles }> = [
  { path: '/', name: 'login', component: LoginView },
  { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { roles: ['ROLE_USER'] } },
  { path: '/history', name: 'history', component: LocationHistoryView, meta: { roles: ['ROLE_USER'] } },
  { path: '/locations', name: 'locations', component: LocationsView, meta: { roles: ['ROLE_ADMIN'] } },
  { path: '/companies', name: 'companies', component: CompaniesView, meta: { roles: ['ROLE_ROOT'] } },
  { path: '/devices/:id', name: 'DeviceDetailView', component:DeviceDetailsView, meta: {roles: ['ROLE_USER']}},
  { path: '/403', name: 'forbidden', component: ForbiddenView },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView},
];

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _, next) => {
  const auth = useAuth();
  const roles = (to.meta.roles ?? []) as Role[];

  // guard for all authorized paths
  if (roles.length && !auth.isLoggedIn) {
    return next({ name: 'login' });
  }

  if (to.name === 'not-found') {
    if (!auth.isLoggedIn) {
      return next({ name: 'login' })
    } 
    if (auth.hasRole('ROLE_ROOT')) {
      return next({ name: 'companies' })
    }
    // user/admin
    return next({ name: 'dashboard' })
  }

  if (roles.length && !roles.some(r => auth.hasRole(r))) {
    return next({ name: 'forbidden' });
  }
  
  next();
});


export default router;