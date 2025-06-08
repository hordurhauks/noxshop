<template>
  <div class="admin-dashboard">
    <h1>Admin Dashboard</h1>

    <section>
      <h2>Products</h2>

      <!-- Add New Button -->
      <button @click="showAddModal = true">Add New Product</button>

      <!-- Modal Dialog -->
      <div v-if="showAddModal" class="modal-backdrop" @click.self="cancelAdd">
        <div class="modal">
          <h3>Add New Product</h3>
          <form @submit.prevent="confirmAddProduct">
            <input v-model="newProduct.name" placeholder="Product name" required />

            <input v-model.number="newProduct.price" placeholder="Price" type="number" min="0" step="1" required />

            <label for="imageInput">Product Image:</label>
            <input id="imageInput" type="file" @change="onFileChange" accept="image/*" required />
            <p v-if="uploadMessage" class="upload-message">{{ uploadMessage }}</p>

            <div class="modal-buttons">
              <button type="submit"
                :disabled="!selectedFile || !newProduct.name || newProduct.price <= 0">Confirm</button>
              <button type="button" @click="cancelAdd">Cancel</button>
            </div>
          </form>
        </div>
      </div>

      <ul style="margin-top: 1rem;">
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
      showAddModal: false,
      newProduct: {
        name: '',
        price: 0,
        imageUrl: null,
      },
      selectedFile: null,
      newProductImage: null,
      uploadMessage: '',
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
      const res = await axios.get('/api/products')
      this.products = res.data
    },

    async fetchUserSpend() {
      const headers = await this.getAuthHeader()
      const res = await axios.get('/api/admin/user-spend', { headers })
      this.userSpend = res.data
    },

    async deleteProduct(id) {
      const headers = await this.getAuthHeader()
      await axios.delete(`/api/admin/products/${id}`, { headers })
      this.fetchProducts()
      this.fetchUserSpend()
    },

    onFileChange(event) {
      this.selectedFile = event.target.files[0] || null
      this.uploadMessage = ''
    },

    cancelAdd() {
      this.showAddModal = false
      this.selectedFile = null
      this.uploadMessage = ''
      this.newProduct = { name: '', price: 0, imageUrl: null }
      if (this.$refs.imageInput) this.$refs.imageInput.value = ''
    },

    async confirmAddProduct() {
      if (!this.selectedFile) {
        this.uploadMessage = 'Please select an image.'
        return
      }

      try {
        const formData = new FormData()
        formData.append('file', this.selectedFile)

        const headers = await this.getAuthHeader()

        // Upload image and get back URL
        const uploadRes = await axios.post('/api/admin/upload-image', formData, {
          headers: {
            ...headers,
          },
        })

        console.log(uploadRes)
        const imageUrl = uploadRes.data.imageUrl // assuming backend returns { imageUrl: "..." }
        console.log(imageUrl)

        // Now create product with image URL included
        const productPayload = {
          name: this.newProduct.name,
          price: this.newProduct.price,
          imageUrl,
        }

        await axios.post('/api/admin/products', productPayload, { headers })

        this.uploadMessage = 'Product added successfully!'

        // Refresh product list and user spend
        this.fetchProducts()
        this.fetchUserSpend()

        // Close modal & reset form
        this.cancelAdd()
      } catch (err) {
        this.uploadMessage = 'Error: ' + (err.response?.data?.message || err.message)
      }
    },

    async handleFileUpload(event) {
      const file = event.target.files[0]
      if (!file) return

      const formData = new FormData()
      formData.append("file", file)

      const headers = await this.getAuthHeader()
      const res = await axios.post('/api/admin/upload-image', formData, {
        headers,
        headers: {
          ...headers,
          "Content-Type": "multipart/form-data",
        }
      })

      this.newProduct.image = res.data // unique filename from backend
    },

    async addProductWithImage() {
      if (!this.newProduct.image) {
        alert("Please upload an image first.")
        return
      }

      const headers = await this.getAuthHeader()
      await axios.post('/api/admin/products', this.newProduct, { headers })

      // Reset state
      this.newProduct = { name: '', price: 0, image: '' }
      this.newProductImage = null
      this.showAddDialog = false

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

/* Modal Styles */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.modal input[type="file"] {
  margin-top: 0.5rem;
}

.modal-buttons {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.upload-message {
  margin-top: 0.5rem;
  color: red;
}
</style>
