<template>
  <nav class="navbar">
    <router-link to="/dashboard">Dashboard</router-link>
    <router-link to="/history">Locations history</router-link>
    <router-link v-if="isAdmin" to="/add-location">Add location</router-link>
    <button @click="logout">Logout</button>
  </nav>
</template>

<script setup lang="ts">

import { useAuth } from '../services/auth';

const { logout, isAdmin } = useAuth();

</script>

<style scoped>
.navbar {
  display: flex;
  gap: 10px;
  background: #ccc;
  padding: 10px;
}
</style>
