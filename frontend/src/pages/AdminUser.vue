<template>
    <div class="admin-users">
        <h1>Users</h1>
        <div class="container">
            <ul class="user-list">
                <li v-for="user in users" :key="user.id" @click="selectUser(user)"
                    :class="{ selected: selectedUser?.id === user.id }">
                    {{ user.name }} ({{ user.email }})
                </li>
            </ul>

            <div class="purchase-list" v-if="selectedUser">
                <h2>Purchases for {{ selectedUser.name }}</h2>
                <ul>
                    <li v-for="purchase in purchases" :key="purchase.id">
                        {{ purchase.productName }} — {{ purchase.price }} kr. — {{ purchase.timestamp }}
                    </li>
                </ul>
                <p v-if="purchases.length === 0">No purchases found.</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import axios from 'axios';
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import { onMounted, ref } from 'vue';

const user = ref(null)
const users = ref([])
const selectedUser = ref(null)
const purchases = ref([])

async function getAuthHeader() {
    const user = getAuth().currentUser
    if (!user) throw new Error("User not logged in")
    const token = await user.getIdToken()
    return { Authorization: `Bearer ${token}` }
}

async function fetchUsers() {
    try {
        const headers = await getAuthHeader()
        const res = await axios.get('/api/admin/users', { headers })
        users.value = res.data
    } catch (error) {
        console.error('Error fetching users:', error)
    }
}

async function fetchPurchases(userId) {
    try {
        const headers = await getAuthHeader()
        const res = await axios.get(`/api/admin/users/${userId}/purchases`, { headers })
        purchases.value = res.data
        console.log(purchases.value)
    } catch (error) {
        console.error('Error fetching purchases:', error)
        purchases.value = []
    }
}

function selectUser(user) {
    selectedUser.value = user
    fetchPurchases(user.uid)
}

onMounted(() => {
    const auth = getAuth()
    onAuthStateChanged(auth, (u) => {
        if (u) {
            fetchUsers()
        }
    })

})
</script>

<style scoped>
.container {
    display: flex;
    gap: 2rem;
}

.user-list {
    flex: 1;
    max-width: 300px;
    list-style: none;
    padding: 0;
    border-right: 1px solid #ccc;
}

.user-list li {
    padding: 0.5rem 1rem;
    cursor: pointer;
    border-radius: 4px;
}

.user-list li.selected,
.user-list li:hover {
    background-color: #d3eaff;
}

.purchase-list {
    flex: 2;
}
</style>