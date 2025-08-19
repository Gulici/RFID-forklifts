<template>
  <div class="forbidden-container">
    <h1>403 - Forbidden</h1>
    <p>You do not have permission to access this page.</p>
    <button @click="goBack">Go back</button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuth } from '../services/auth';

const router = useRouter();
const auth = useAuth();

const goBack = () => {
  // jeśli root → companies, admin/user → dashboard
  if (auth.hasRole('ROLE_ROOT')) {
    router.push({ name: 'companies' });
  } else {
    router.push({ name: 'dashboard' });
  }
}
</script>

<style scoped>
.forbidden-container {
  text-align: center;
  margin-top: 100px;
}
button {
  margin-top: 20px;
  padding: 10px 20px;
  cursor: pointer;
}
</style>
