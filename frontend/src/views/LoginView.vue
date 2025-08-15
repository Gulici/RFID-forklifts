<template>
  <div class="login">
    <h2>Login</h2>
    <form @submit.prevent="submit">
      <input v-model="username" type="text" placeholder="Username" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <button type="submit">Zaloguj</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '../services/auth';

const username = ref<string>('');
const password = ref<string>('');
const { login } = useAuth();

const submit = async () => {
  await login(username.value, password.value);
};
</script>
