<template>
  <nav class="navbar">
      <div class="navbar-left">
        <div class="logo">🌐 Forklift RFID</div>
      </div>
      <div class="navbar-right">
        <button class="menu-toggle" @click="toggleMenu">☰</button>
        <ul v-if="menuOpen" class="dropdown-menu">
          <template v-if="auth.isLoggedIn">
            <li>
              <router-link to="/dashboard" @click="closeMenu">Devices</router-link>
            </li>
            <li v-if="auth.isAdmin">
              <router-link to="/locations" @click="closeMenu">Locations</router-link>
            </li>
            <li>
              <router-link to="/history" @click="closeMenu">Devices history</router-link>
            </li>
            <li>
              <button @click="auth.logout">Logout</button>
            </li>
          </template>

          <li v-else>
            <router-link to="/" @click="closeMenu">Login</router-link>
          </li>
        </ul>
      </div>
    </nav>
</template>

<script setup lang="ts">

import { ref } from 'vue';
import { useAuth } from '../services/auth';

const auth = useAuth();
const menuOpen = ref(false);

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value;
};

const closeMenu = () => {
  menuOpen.value = false;
}

</script>
