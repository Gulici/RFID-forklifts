<template>
  <Navbar />
  <div class="container">

    <!-- <button @click="goBack" class="back-btn">⬅ Back to list</button> -->

    <div v-if="loading">Loading device details...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
      <h2>Device: {{ device?.name }}</h2>
      <p><strong>ID:</strong> {{ device?.id }}</p>
      <p><strong>Current location:</strong> 
        {{ device?.location?.name }} id: {{ device?.location?.zoneId }} (0x{{ device?.location?.zoneId.toString(16).toUpperCase() }})
      </p>
      <p><strong>Last timestamp:</strong> {{ device?.timestamp }}</p>
    </div>

    <br></br>

    <table>
      <thead>
        <tr>
          <th>Location</th>
          <th>Time</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="h in hist" :key="h.timestamp">
          <td>{{ h.locationDto.name }} id: {{ h.locationDto.zoneId }} (0x{{ h.locationDto.zoneId.toString(16).toUpperCase() }})</td>
          <td>{{ h.timestamp }}</td>
        </tr>
      </tbody>
    </table>
  </div>

</template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue';
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '../services/api';
import type { DeviceDto, LocationHistoryDto } from '../types/dto';

const route = useRoute();
const router = useRouter();
const deviceId = route.params.id as string;

const device = ref<DeviceDto | null>(null);
const hist = ref<LocationHistoryDto[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

function goBack() {
  router.back();
}

// const sortedHist = computed(() => {
//   return [...hist.value].sort((a, b) => 
//     new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
//   );
// });

onMounted(async () => {
  try {
    const [ devRes, histRes] = await Promise.all([
      axios.get<DeviceDto>(`/devices/${deviceId}`),
      axios.get<LocationHistoryDto[]>('/locations/history')
    ])
    device.value = devRes.data;
    hist.value = histRes.data.filter(h => h.deviceDto.id === deviceId);
  } catch (e) {
    console.error(e);
    error.value = "Failed to load device details.";
  } finally {
    loading.value = false;
  }
});
</script>
