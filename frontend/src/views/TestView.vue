<!-- <template>
  <div class="dashboard">
    <h2>Dashboard</h2>
    <button @click="logout">Logout</button>
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Location</th>
          <th>Zone</th>
          <th>Zone (x, y)</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="device in devices" :key="device.id">
          <td>{{ device.name }}</td>
          <td>{{ device.location.name }}</td>
          <td>{{ device.location.zoneId }}</td>
          <td>{{ device.location.x }}, {{ device.location.y }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="roles.includes('ROLE_ROOT')" class="firms-section">
      <h3>Firms</h3>
      <table>
        <thead>
          <tr>
            <th>Firm Name</th>
            <th>Users</th>
            <th>Locations</th>
            <th>Devices</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="firm in firms" :key="firm.id">
            <td>{{ firm.firmName }}</td>
            <td>
              <ul>
                <li v-for="user in firm.users" :key="user.id">{{ user.username }}</li>
              </ul>
            </td>
            <td>
              <ul>
                <li v-for="loc in firm.locations" :key="loc.id">{{ loc.name }}</li>
              </ul>
            </td>
            <td>
              <ul>
                <li v-for="dev in firm.devices" :key="dev.id">{{ dev.name }}</li>
              </ul>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import instance from '../services/api'
import { getUserRoles } from '../services/auth'
import { useRouter } from 'vue-router'
import { DeviceDto, FirmDto } from '../types/dto'

const devices = ref<DeviceDto[]>([])
const firms = ref<FirmDto[]>([])
const roles = ref(getUserRoles() || [])
const router = useRouter()

onMounted(async () => {
  try {
    const res = await instance.get('/devices')
    devices.value = res.data

    console.log('User roles: ' + roles.value)

    if (roles.value.includes('ROLE_ROOT')) {
      const resFirms = await instance.get<FirmDto[]>('/firms')
      firms.value = resFirms.data
    }

    console.log('Fetched firms: ' + firms.value)

  } catch (err) {
    alert('Error during fetching data.')
    router.push('/')
  }
})

const logout = () => {
  localStorage.removeItem('token')
  router.push('/')
}

const formatDate = (ts) => {
  return new Date(ts).toLocaleString()
}
</script> -->