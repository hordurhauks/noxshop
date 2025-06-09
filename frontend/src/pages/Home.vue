<template>
  <main class="home">

    <div v-if="loading" class="loading-indicator">
      <div class="spinner"></div>
    </div>

    <div v-else-if="!user">
      <h2>Welcome to the Nox Shop</h2>
      <button @click="signIn">Sign in with Google</button>
    </div>

    <div v-else>
      <h2>Hi, {{ user.displayName }}!</h2>
      <button @click="signOut">Sign Out</button>

      <section class="product-grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" @click="buy(product.id)" />
      </section>



      <section class="purchase-history" v-if="purchases.length">
        <h3>Your Purchases</h3>
        <ul>
          <li v-for="purchase in purchases" :key="purchase.id">
            {{ formatDate(purchase.timestamp) }} – {{ purchase.productName }} – {{ purchase.price.toLocaleString() }}
            ISK
          </li>
        </ul>
      </section>
    </div>
  </main>
</template>

<script setup>
import axios from 'axios'
import { signOut as doSignOut, getAuth, GoogleAuthProvider, onAuthStateChanged, signInWithPopup } from 'firebase/auth'
import { ref } from 'vue'
import ProductCard from '../components/ProductCard.vue'

const auth = getAuth()
const user = ref(null)
const products = ref([])
const purchases = ref([])
const loading = ref(true)

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
  alert('Purchase successful!')
}

function formatDate(iso) {
  return new Date(iso).toLocaleString('is-IS')
}
</script>

<style scoped>
.home {
  padding: 1rem;
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
}


.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
  margin-top: 2rem;
}

.card {
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: 0.2s ease-in-out;
}

.card:hover {
  transform: scale(1.05);
  background: #fff;
}

.purchase-history {
  margin-top: 3rem;
}

.purchase-history h3 {
  margin-bottom: 1rem;
}

.purchase-history ul {
  list-style: none;
  padding: 0;
}

.purchase-history li {
  padding: 0.5rem 0;
  border-bottom: 1px solid #ddd;
}

.loading-indicator {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50vh;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 5px solid #ccc;
  border-top-color: #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Animation */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
