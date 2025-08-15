<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuth } from './services/auth';

const auth = useAuth();

onMounted(() => {
  auth.loadFromStorage();
});
</script>
