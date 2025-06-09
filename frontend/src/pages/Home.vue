<template>
  <main class="home">
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>

    <div v-else-if="!user">
      <h2>Welcome to the Nox Shop</h2>
      <button @click="signIn">Sign in with Google</button>
    </div>

    <div v-else>
      <header class="home-header">
        <div class="user-info">
          <img :src="user?.photoURL || '/placeholder-user.png'" alt="avatar" class="avatar"
            @click="menuOpen = !menuOpen" />
          <div v-if="menuOpen" class="dropdown">
            <p>{{ user.displayName }}</p>
            <button @click="signOut">Sign Out</button>
          </div>
        </div>
        <h2>Welcome, {{ user.displayName }}!</h2>
      </header>

      <div class="tabs">
        <button :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">🛒 Products</button>
        <button :class="{ active: activeTab === 'purchases' }" @click="activeTab = 'purchases'">💰 My Purchases</button>
      </div>

      <section v-if="activeTab === 'products'" class="product-grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" @select-product="buy(product.id)" />
      </section>

      <section v-if="activeTab === 'purchases' && groupedPurchases" class="purchase-history">
        <details v-for="(list, month) in groupedPurchases" :key="month">
          <summary>{{ month }}</summary>
          <ul>
            <li v-for="purchase in list" :key="purchase.id">
              {{ formatDate(purchase.timestamp) }} – {{ purchase.productName }} – {{ purchase.price.toLocaleString() }}
              kr.
            </li>
          </ul>
        </details>
      </section>

      <div v-if="toast" class="toast">{{ toast }}</div>
    </div>
  </main>
</template>

<script setup>
import axios from 'axios'
import {
  signOut as doSignOut,
  getAuth,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup
} from 'firebase/auth'
import { computed, ref } from 'vue'
import ProductCard from '../components/ProductCard.vue'

const auth = getAuth()
const user = ref(null)
const products = ref([])
const purchases = ref([])
const loading = ref(true)
const menuOpen = ref(false)
const toast = ref(null)
const activeTab = ref('products')

function showToast(message) {
  toast.value = message
  setTimeout(() => (toast.value = null), 3000)
}

onAuthStateChanged(auth, async (u) => {
  loading.value = true
  user.value = u
  if (u) {
    try {
      const token = await u.getIdToken()
      await axios.post('/api/auth/login', null, {
        headers: { Authorization: `Bearer ${token}` }
      })
      await Promise.all([loadProducts(), loadPurchases()])
    } catch (error) {
      console.error('Error during backend sync or data load:', error)
    }
  }
  loading.value = false
})

function signIn() {
  const provider = new GoogleAuthProvider()
  signInWithPopup(auth, provider)
}

function signOut() {
  doSignOut(auth)
}

async function loadProducts() {
  const res = await axios.get('/api/products')
  products.value = res.data
}

async function loadPurchases() {
  try {
    const token = await auth.currentUser.getIdToken()
    const res = await axios.get('/api/purchases', {
      headers: { Authorization: `Bearer ${token}` }
    })
    purchases.value = res.data
  } catch (e) {
    console.error('Failed to load purchases', e.response?.data || e.message)
  }
}

async function buy(productId) {
  const product = products.value.find(p => p.id === productId)
  const confirmed = confirm(`Do you want to buy "${product.name}" for ${product.price.toLocaleString()} ISK?`)
  if (!confirmed) return

  const token = await auth.currentUser.getIdToken()
  await axios.post(`/api/buy?productId=${productId}`, null, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
  showToast('Purchase successful!')
  await loadPurchases()
}

function formatDate(iso) {
  return new Date(iso).toLocaleString('is-IS')
}

const groupedPurchases = computed(() => {
  const groups = {}
  for (const p of purchases.value) {
    const date = new Date(p.timestamp)
    const month = date.toLocaleDateString('is-IS', { month: 'long', year: 'numeric' })
    if (!groups[month]) groups[month] = []
    groups[month].push(p)
  }
  return groups
})
</script>

<style scoped>
.home {
  padding: 1rem;
  max-width: 1000px;
  margin: 0 auto;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  position: relative;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
}

.dropdown {
  position: absolute;
  background: white;
  border: 1px solid #ddd;
  right: 0;
  top: 50px;
  padding: 0.5rem;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  margin-top: 1rem;
}

.tabs button {
  flex: 1;
  padding: 0.5rem;
  border: none;
  background: #eee;
  cursor: pointer;
}

.tabs button.active {
  background: #ccc;
  font-weight: bold;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
  margin-top: 2rem;
}

.purchase-history {
  margin-top: 2rem;
}

.toast {
  position: fixed;
  bottom: 1rem;
  right: 1rem;
  background: #4caf50;
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  animation: fadeInOut 3s ease-in-out;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }

  10% {
    opacity: 1;
    transform: translateY(0);
  }

  90% {
    opacity: 1;
    transform: translateY(0);
  }

  100% {
    opacity: 0;
    transform: translateY(20px);
  }
}

.loading-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #ccc;
  border-top-color: #333;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
