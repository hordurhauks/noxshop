<template>
    <div class="admin-dashboard">
        <h1>Admin Dashboard</h1>

        <section>
            <h2>Products</h2>

            <!-- Loading Indicator -->
            <div v-if="loading" class="loading">Loading...</div>

            <!-- Add New Button -->
            <button @click="showAddModal = true">Add New Product</button>

            <!-- Modal Dialog -->
            <div v-if="showAddModal" class="modal-backdrop" @click.self="cancelAdd">
                <div class="modal">
                    <h3>Add New Product</h3>
                    <form @submit.prevent="confirmAddProduct">
                        <input v-model="newProduct.name" placeholder="Product name" required />
                        <input v-model.number="newProduct.price" placeholder="Price" type="number" min="0" step="1"
                            required />

                        <label for="imageInput">Product Image:</label>
                        <input id="imageInput" type="file" @change="onFileChange" accept="image/*" required
                            :key="fileInputKey" />
                        <p v-if="uploadMessage" class="upload-message">{{ uploadMessage }}</p>

                        <div v-if="imagePreview" class="image-preview">
                            <img :src="imagePreview" alt="Preview" />
                        </div>

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
                    <button @click="confirmDeleteProduct(product.id)">Remove</button>
                </li>
            </ul>
        </section>
    </div>
</template>

<script setup>
import axios from 'axios'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import { onMounted, reactive, ref } from 'vue'

const user = ref(null)
const products = ref([])
const userSpend = ref({})
const showAddModal = ref(false)
const newProduct = reactive({ name: '', price: 0, imageUrl: null })
const selectedFile = ref(null)
const imagePreview = ref(null)
const uploadMessage = ref('')
const loading = ref(false)
const fileInputKey = ref(Date.now())

const getAuthHeader = async () => {
    const user = getAuth().currentUser
    if (!user) throw new Error("User not logged in")
    const token = await user.getIdToken()
    return { Authorization: `Bearer ${token}` }
}

const fetchProducts = async () => {
    loading.value = true
    try {
        const res = await axios.get('/api/products')
        products.value = res.data
    } finally {
        loading.value = false
    }
}

const fetchUserSpend = async () => {
    const headers = await getAuthHeader()
    const res = await axios.get('/api/admin/user-spend', { headers })
    userSpend.value = res.data
}

const confirmDeleteProduct = async (id) => {
    if (!confirm('Are you sure you want to delete this product?')) return
    const headers = await getAuthHeader()
    await axios.delete(`/api/admin/products/${id}`, { headers })
    await fetchProducts()
    await fetchUserSpend()
}

const onFileChange = (event) => {
    selectedFile.value = event.target.files[0] || null
    uploadMessage.value = ''
    if (selectedFile.value) {
        const reader = new FileReader()
        reader.onload = e => imagePreview.value = e.target.result
        reader.readAsDataURL(selectedFile.value)
    } else {
        imagePreview.value = null
    }
}

const cancelAdd = () => {
    showAddModal.value = false
    selectedFile.value = null
    imagePreview.value = null
    uploadMessage.value = ''
    newProduct.name = ''
    newProduct.price = 0
    newProduct.imageUrl = null
    fileInputKey.value = Date.now()
}

const confirmAddProduct = async () => {
    if (!selectedFile.value) {
        uploadMessage.value = 'Please select an image.'
        return
    }

    try {
        const formData = new FormData()
        formData.append('file', selectedFile.value)

        const headers = await getAuthHeader()

        const uploadRes = await axios.post('/api/admin/upload-image', formData, {
            headers,
        })

        const imageUrl = uploadRes.data.imageUrl

        const productPayload = {
            name: newProduct.name,
            price: newProduct.price,
            imageUrl,
        }

        await axios.post('/api/admin/products', productPayload, { headers })

        uploadMessage.value = 'Product added successfully!'

        await fetchProducts()
        await fetchUserSpend()

        cancelAdd()
    } catch (err) {
        uploadMessage.value = 'Error: ' + (err.response?.data?.message || err.message)
    }
}

onMounted(() => {
    const auth = getAuth()
    onAuthStateChanged(auth, (u) => {
        user.value = u
        if (u) {
            fetchProducts()
            fetchUserSpend()
        }
    })
})
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

.loading {
    font-style: italic;
    margin-bottom: 1rem;
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

.image-preview {
    margin-top: 1rem;
}

.image-preview img {
    max-width: 100%;
    max-height: 200px;
    display: block;
    margin: auto;
}
</style>