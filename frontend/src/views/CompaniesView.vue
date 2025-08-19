<template>
<div>
    <Navbar />
    <div class="companies">
      <h2>Companies</h2>
  
      <form @submit.prevent="addCompany">
        <input v-model="newCompany.firmName" placeholder="Company name" required />
        <input v-model="newCompany.adminName" placeholder="Admin username" required />
        <input v-model="newCompany.adminEmail" placeholder="Admin email" type="email" required />
        <input v-model="newCompany.password" placeholder="Admin password" type="password" required />
        <button type="submit">Add</button>
      </form>
  
      <ul>
        <li v-for="c in companies" :key="c.id">{{ c.firmName }}</li>
      </ul>
  
      <p v-if="error" class="error">{{ error }}</p>
    </div>
</div>  
</template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue'
import { ref, onMounted } from 'vue'
import axios from '../services/api'
import type { FirmDto, FirmRegisterDto } from '../types/dto'

const companies = ref<FirmDto[]>([])
const error = ref<string | null>(null)

const newCompany = ref<FirmRegisterDto>({
  firmName: '',
  adminName: '',
  adminEmail: '',
  password: ''
})

async function loadCompanies() {
  try {
    const res = await axios.get<FirmDto[]>('/firms')
    companies.value = res.data
  } catch (err: any) {
    if (err.response?.status === 403) {
      error.value = 'Access denied. You need ROOT role.'
    } else {
      error.value = 'Failed to load companies.'
    }
  }
}

async function addCompany() {
  try {
    const res = await axios.post<FirmDto>('/firms', newCompany.value)
    companies.value.push(res.data)
    newCompany.value = { firmName: '', adminName: '', adminEmail: '', password: '' }
    error.value = null
  } catch (err: any) {
    if (err.response?.status === 403) {
      error.value = 'Access denied. You need ROOT role.'
    } else {
      error.value = 'Failed to create company.'
    }
  }
}

onMounted(loadCompanies)
</script>

<style scoped>
.error {
  color: red;
  margin-top: 10px;
}
</style>
