<template>
  <div class="login">
    <h2>Logowanie</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" placeholder="Użytkownik" required />
      <input v-model="password" type="password" placeholder="Hasło" required />
      <button type="submit">Zaloguj</button>
      <p v-if="error" style="color: red;">{{ error }}</p>
    </form>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import api from '../services/api'
import { useRouter } from 'vue-router'
import { LoginRequest, LoginResponse } from '../types/dto'

const username = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()

const handleLogin = async () => {

  const payload: LoginRequest = {
    username: username.value,
    password: password.value
  }

  try {
    const res = await api.post<LoginResponse>('/auth/login', payload)
    localStorage.setItem('token', res.data.jwt)
    console.log('Token saved:', localStorage.getItem('token'))
    router.push('/dashboard')
  } catch (err) {
    error.value = 'Invalid login or password'
  }
}
</script>