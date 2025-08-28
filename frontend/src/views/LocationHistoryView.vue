<template>
  <div>
    <Navbar />
    <div class="container">
      <h2>Location history</h2>

      <!-- <div class="controls">
        <label for="rowPerPage">Rows per page</label>
        <select id="rowsPerPage" v-model.number="rowsPerPage">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
        </select>
      </div> -->

      <!-- <label>Sort by device:</label>
      <select v-model="selectedDevice" @change="fetchHistory">
        <option value="">All</option>
        <option v-for="d in devices" :key="d.id" :value="d.id">{{ d.name }}</option>
      </select> -->

      <table>
        <thead>
          <tr>
            <th>Device</th>
            <th>Location</th>
            <th>Time</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="h in paginatedHistory" :key="h.timestamp">
            <td>{{ h.deviceDto.name }}</td>
            <td>{{ h.locationDto.name }} id: {{ h.locationDto.zoneId }} (0x{{ h.locationDto.zoneId.toString(16).toUpperCase() }})</td>
            <td>{{ h.timestamp }}</td>
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
import type { DeviceDto, LocationDto, LocationHistoryDto } from '../types/dto';


const devices = ref<DeviceDto[]>([]);
const history = ref<LocationHistoryDto[]>([]);
const selectedDevice = ref<string>('');
const rowsPerPage = ref<number>(10);
const currentPage = ref<number>(1);

const pageCount = computed(() =>
  Math.ceil(devices.value.length / rowsPerPage.value)
);

const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * rowsPerPage.value;
  const end = start + rowsPerPage.value;
  return history.value.slice(start, end);
});

const fetchHistory = async () => {
  let url = '/locations/history';
  // if (selectedDevice.value) {
  //   url += `?deviceId=${selectedDevice.value}`;
  // }
  const res = await axios.get<LocationHistoryDto[]>(url);
  history.value = res.data;
};

onMounted(async () => {
  const devRes = await axios.get<DeviceDto[]>('/devices');
  devices.value = devRes.data;
  await fetchHistory();
});
</script>
