<template>
  <div class="admin-dashboard">
    <h1>Admin Dashboard</h1>

    <section>
      <h2>Products</h2>
      <form @submit.prevent="addProduct">
        <input v-model="newProduct.name" placeholder="Product name" required />
        <input v-model.number="newProduct.price" placeholder="Price" type="number" required />
        <button type="submit">Add Product</button>
      </form>
      <ul>
        <li v-for="product in products" :key="product.id">
          {{ product.name }} - {{ product.price.toFixed(2) }} kr
          <button @click="deleteProduct(product.id)">Remove</button>
        </li>
      </ul>
    </section>

    <section>
      <h2>User Purchases (Monthly)</h2>
      <ul>
        <li v-for="(amount, userId) in userSpend" :key="userId">
          <strong>{{ userId }}</strong>: {{ amount.toFixed(2) }} kr
        </li>
      </ul>
    </section>
  </div>
</template>

<script>
import axios from 'axios'
import { getAuth, onAuthStateChanged } from 'firebase/auth'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      user: null,
      products: [],
      userSpend: {},
      newProduct: {
        name: '',
        price: 0,
      },
    }
  },
  methods: {
    async getAuthHeader() {
    const user = getAuth().currentUser
    if (!user) throw new Error("User not logged in")
    const token = await user.getIdToken()
    return { Authorization: `Bearer ${token}` }
  },
  async fetchProducts() {
    const res = await axios.get('http://localhost:8085/api/products')
    this.products = res.data
  },

  async fetchUserSpend() {
    const headers = await this.getAuthHeader()
    const res = await axios.get('http://localhost:8085/api/admin/user-spend', { headers })
    this.userSpend = res.data
  },

  async addProduct() {
    const headers = await this.getAuthHeader()
    await axios.post('http://localhost:8085/api/admin/products', this.newProduct, { headers })
    this.newProduct.name = ''
    this.newProduct.price = 0
    this.fetchProducts()
    this.fetchUserSpend()
  },

  async deleteProduct(id) {
    const headers = await this.getAuthHeader()
    await axios.delete(`http://localhost:8085/api/admin/products/${id}`, { headers })
    this.fetchProducts()
    this.fetchUserSpend()
  },
},
  mounted() {
    const auth = getAuth()
    onAuthStateChanged(auth, (u) => {
      this.user = u
      if (u) {
        this.fetchProducts()
        this.fetchUserSpend()
      } 
    })
  },
}
</script>

<style scoped>
.admin-dashboard {
  padding: 2rem;
  max-width: 600px;
  margin: auto;
}
form input {
  margin-right: 0.5rem;
}
button {
  margin-left: 0.5rem;
}
section {
  margin-bottom: 2rem;
}
</style>
