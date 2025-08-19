<template>
  <div class="not-found">
    <h1>404 - Page Not Found</h1>
    <p>Sorry, the page you are looking for does not exist.</p>
    <button @click="goHome">Go Home</button>
  </div>
</template>

<script setup lang="ts">
import { useAuth } from '../services/auth'
import { useRouter } from 'vue-router'

const auth = useAuth()
const router = useRouter()

function goHome() {
  if (auth.isLoggedIn) {
    if (auth.hasRole('ROLE_ROOT')) {
      router.push({ name: 'companies' })
    } else {
      router.push({ name: 'dashboard' })
    }
  } else {
    router.push({ name: 'login' })
  }
}
</script>

<style scoped>
.not-found {
  text-align: center;
  margin-top: 100px;
}
button {
  margin-top: 20px;
  padding: 10px 20px;
  cursor: pointer;
}
</style>
