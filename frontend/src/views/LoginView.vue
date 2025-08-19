<template>
  <div>
    <Navbar />
    <main class="main-container">
      <div class="login-panel">
        <h2>Login</h2>
        <form @submit.prevent="submit">
          <input v-model="username" type="text" placeholder="Username" required />
          <input v-model="password" type="password" placeholder="Password" required />
          <button type="submit">Login</button>
        </form>
        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      </div>
    </main>
  </div>
</template>


<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '../services/auth';
import Navbar from '../components/Navbar.vue';

const username = ref<string>('');
const password = ref<string>('');
const errorMessage = ref<string | null>(null);
const { login } = useAuth();

const submit = async () => {
  errorMessage.value = null;
  try {
    await login(username.value, password.value);
  } catch (err: any) {
    if (err.response?.status === 401) {
      errorMessage.value = 'Incorrect username or password';
    } else {
      errorMessage.value = 'An error occurred. Please try again.';
    }
  }
};
</script>

<style scoped>
.error {
  color: red;
  margin-top: 10px;
}
</style>
