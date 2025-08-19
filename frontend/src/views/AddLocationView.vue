<template>
  <div>
    <Navbar />
    <div class="container">
      <h2>Locations</h2>
      <div>
        <form @submit.prevent="addLocation">
          <input v-model="name" placeholder="Name" required />
          <input v-model.number="zoneId" type="number" placeholder="Zone ID" required />
          <input v-model.number="x" type="number" placeholder="X" required />
          <input v-model.number="y" type="number" placeholder="Y" required />
          <button type="submit">Add</button>
        </form>
      </div>
      <div>
        <table>
          <thead>
          <tr>
            <th>Location name</th>
            <th>Zone id</th>
            <th>(x, y)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="location in paginatedLocations" :key="location.id">
            <td>{{ location.name }}</td>
            <td>{{ location.zoneId }}</td>
            <td>{{ location.x }} {{ location.y }}</td>
          </tr>
        </tbody>
        </table>
      </div>
      <div class="pagination">
        <button
          v-for="page in pageCount"
          :key="page"
          :class="{ active: currentPage === page }"
          @click="currentPage = page"
          >
          {{ page }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue';
import axios from '../services/api';
import { computed, onMounted, ref } from 'vue';
import type { LocationDto } from '../types/dto';

const name = ref<string>('');
const zoneId = ref<number | null>(null);
const x = ref<number | null>(null);
const y = ref<number | null>(null);

const rowsPerPage = ref<number>(10);
const currentPage = ref<number>(1);

const locations = ref<LocationDto[]>([]);

const pageCount = computed(() =>
  Math.ceil(locations.value.length / rowsPerPage.value)
);

const paginatedLocations = computed(() => {
  const start = (currentPage.value - 1) * rowsPerPage.value;
  const end = start + rowsPerPage.value;
  return locations.value.slice(start, end);
});

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

onMounted(async () => {
  const res = await axios.get<LocationDto[]>('/locations');
  locations.value = res.data;
});

</script>
