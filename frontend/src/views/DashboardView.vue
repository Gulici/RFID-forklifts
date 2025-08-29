<template>
  <div>
    <Navbar />
    <div class="container">
      <h2>Device List</h2>
      <!-- <div class="controls">
        <label for="rowPerPage">Rows per page</label>
        <select id="rowsPerPage" v-model.number="rowsPerPage">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
        </select>
      </div> -->

      <table>
        <thead>
          <tr>
            <th>Device Name</th>
            <th>Last location</th>
            <th>Last time</th>
            <th>Is active</th>
            <th v-if="auth.isAdmin">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in devices" :key="device.id">
            <td>
              <router-link :to="{ name: 'DeviceDetailView', params: { id: device.id }}">
                {{ device.name }}
              </router-link>
            </td>
            <td>
              <span v-if="device.location">
                {{ device.location.name }} id: {{ device.location.zoneId }} (0x{{ device.location?.zoneId.toString(16).toUpperCase() }})
              </span>
              <span v-else>-</span>
            </td>
            <td>{{ device.timestamp }}</td>
            <td>{{ device.alive }}</td>
            <td v-if="auth.isAdmin">
              <button @click="deleteDevice(device.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- <div class="pagination">
        <button
          v-for="page in pageCount"
          :key="page"
          :class="{ active: currentPage === page }"
          @click="currentPage = page"
          >
          {{ page }}
        </button>
      </div> -->

    </div>
  </div>
</template>

<script setup lang="ts">

import Navbar from '../components/Navbar.vue';
import axios from '../services/api';
import { ref, onMounted, computed } from 'vue';
import type { DeviceDto } from '../types/dto';
import { useAuth } from '../services/auth';

const auth = useAuth();
const devices = ref<DeviceDto[]>([]);
const rowsPerPage = ref<number>(10);
const currentPage = ref<number>(1);

// const pageCount = computed(() =>
//   Math.ceil(devices.value.length / rowsPerPage.value)
// );

// const paginatedDevices = computed(() => {
//   const start = (currentPage.value - 1) * rowsPerPage.value;
//   const end = start + rowsPerPage.value;
//   return devices.value.slice(start, end);
// });

const deleteDevice = async (id: string) => {
  if (!confirm('Are you sure you want to delete this device?')) return;

  try {
    await axios.delete(`/devices/${id}`);
    devices.value = devices.value.filter(d => d.id !== id);
  } catch (error) {
    console.error('Error deleting device:', error);
    alert('Failed to delete device.');
  }
};

onMounted(async () => {
  const res = await axios.get<DeviceDto[]>('/devices');
  devices.value = res.data;
});

</script>
