<template>
  <div>
    <Navbar />
    <h2>Add lcoation</h2>
    <form @submit.prevent="addLocation">
      <input v-model="name" placeholder="Name" required />
      <input v-model.number="zoneId" type="number" placeholder="Zone ID" required />
      <input v-model.number="x" type="number" placeholder="X" required />
      <input v-model.number="y" type="number" placeholder="Y" required />
      <button type="submit">Add</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue';
import axios from '../services/api';
import { ref } from 'vue';
import type { LocationDto } from '../types/dto';

const name = ref<string>('');
const zoneId = ref<number | null>(null);
const x = ref<number | null>(null);
const y = ref<number | null>(null);

const addLocation = async () => {
  const payload: Omit<LocationDto, 'id'> = {
    name: name.value,
    zoneId: zoneId.value!,
    x: x.value!,
    y: y.value!
  };
  await axios.post('/locations', payload);
  alert('Location added');
};
</script>
