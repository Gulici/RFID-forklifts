<template>
  <div>
    <Navbar />
    <div class="container">
      <h2>Locations</h2>
      <div>
        <form @submit.prevent="addLocation">
          <input v-model="name" placeholder="Name" required />
          <input v-model="zoneId" type="string" placeholder="Zone ID hex" required />
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
            <th v-if="auth.isAdmin">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="location in paginatedLocations" :key="location.id">
            <td>{{ location.name }}</td>
            <td>
              <span v-if="location.zoneId !== null">
                {{ location.zoneId }} (0x{{ location.zoneId.toString(16).toUpperCase() }})
              </span>
              <span v-else>-</span>
            </td>
            <td>{{ location.x }} {{ location.y }}</td>
            <td v-if="auth.isAdmin">
              <button @click="deleteLocation(location.id)">Delete</button>
            </td>
          </tr>
        </tbody>
        </table>
      </div>
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
import { computed, onMounted, ref } from 'vue';
import type { LocationDto } from '../types/dto';
import { useAuth } from '../services/auth';

const auth = useAuth();

const name = ref<string>('');
const zoneId = ref<string>('');
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
  try {
    const hexRegex = /^[0-9A-Fa-f]{1,2}$/;
    if (!hexRegex.test(zoneId.value)) {
      alert('Zone ID must be a valid 1-2 digit hexadecimal number (00-FF)');
      return;
    }

    const parsedZoneId = parseInt(zoneId.value, 16);
    if (isNaN(parsedZoneId)) {
      alert('Invalid format Zone ID (enter hex number, ex. "1A")');
      return;
    }

    const payload: Omit<LocationDto, 'id'> = {
      name: name.value,
      zoneId: parsedZoneId,
      x: x.value!,
      y: y.value!
    };

    await axios.post('/locations', payload);
    alert('Location added');

    await fetchLocations();

  } catch (error: any) {
    console.error(error);

    if(error.response) {
      const data = error.response.data;

      alert(data.detail || data.title || 'Error during location adding.');
    } else {
      alert('Error durring location adding.');
    }
  }
};

const deleteLocation = async (id: string) => {
  if (!confirm('Are you sure you want to delete this device?')) return;

  try {
    await axios.delete(`/locations/${id}`);
    locations.value = locations.value.filter(d => d.id !== id);
  } catch (error: any) {
    if (error.response && error.response.status === 409) {
      const detail = error.response.data.detail || error.response.data.title;
      alert(`Cannot delete location: ${detail}`);
    } else {
      alert('Error deleting location');
    }
    console.error(error);
  }
};

const fetchLocations = async () => {
    const res = await axios.get<LocationDto[]>('/locations');
    locations.value = res.data;
}

onMounted(fetchLocations);

</script>
