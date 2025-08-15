<template>
  <div>
    <Navbar />
    <h2>Device list</h2>
    <table>
      <thead>
        <tr>
          <th>Device name</th>
          <th>Location</th>
          <!-- <th>Last update</th> -->
        </tr>
      </thead>
      <tbody>
        <tr v-for="device in devices" :key="device.id">
          <td>{{ device.name }}</td>
          <td>{{ device.location?.name }}</td>
          <!-- <td>{{ device.location?.timestamp || '-' }}</td> -->
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue';
import axios from '../services/api';
import { ref, onMounted } from 'vue';
import type { DeviceDto } from '../types/dto';

const devices = ref<DeviceDto[]>([]);

onMounted(async () => {
  const res = await axios.get<DeviceDto[]>('/devices');
  devices.value = res.data;
});
</script>
