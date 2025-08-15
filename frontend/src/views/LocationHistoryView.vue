<template>
  <div>
    <Navbar />
    <h2>Location history</h2>
    <label>Sort by device:</label>
    <select v-model="selectedDevice" @change="fetchHistory">
      <option value="">All</option>
      <option v-for="d in devices" :key="d.id" :value="d.id">{{ d.name }}</option>
    </select>

    <table>
      <thead>
        <tr>
          <th>Device</th>
          <th>Location</th>
          <th>Time</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="h in history" :key="h.timestamp">
          <td>{{ h.deviceDto.name }}</td>
          <td>{{ h.locationDto.name }}</td>
          <td>{{ h.timestamp }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue';
import axios from '../services/api';
import { ref, onMounted } from 'vue';
import type { DeviceDto, LocationDto } from '../types/dto';

interface LocationHistoryItem {
  deviceDto: DeviceDto,
  locationDto: LocationDto,
  timestamp: string
}

const devices = ref<DeviceDto[]>([]);
const history = ref<LocationHistoryItem[]>([]);
const selectedDevice = ref<string>('');

const fetchHistory = async () => {
  let url = '/locations/history';
  // if (selectedDevice.value) {
  //   url += `?deviceId=${selectedDevice.value}`;
  // }
  const res = await axios.get<LocationHistoryItem[]>(url);
  history.value = res.data;
};

onMounted(async () => {
  const devRes = await axios.get<DeviceDto[]>('/devices');
  devices.value = devRes.data;
  await fetchHistory();
});
</script>
