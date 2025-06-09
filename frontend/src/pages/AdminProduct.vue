<template>
    <div class="admin-dashboard">
        <section>
            <h1>Products</h1>
            <button @click="showAddModal = true">Add New Product</button>

            <div v-if="showAddModal" class="modal-backdrop" @click.self="cancelAdd">
                <div class="modal">
                    <h3>Add New Product</h3>
                    <form @submit.prevent="confirmAddProduct">
                        <input v-model="newProduct.name" placeholder="Product name" required />
                        <input v-model.number="newProduct.price" placeholder="Price" type="number" min="0" step="1"
                            required />
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
    </div>
</template>

<script setup>
import axios from 'axios'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import { onMounted, ref } from 'vue'

const user = ref(null)
const products = ref([])
const userSpend = ref({})
const showAddModal = ref(false)
const newProduct = ref({ name: '', price: 0, imageUrl: null })
const selectedFile = ref(null)
const uploadMessage = ref('')

const getAuthHeader = async () => {
    const currentUser = getAuth().currentUser
    if (!currentUser) throw new Error('User not logged in')
    const token = await currentUser.getIdToken()
    return { Authorization: `Bearer ${token}` }
}

const fetchProducts = async () => {
    const res = await axios.get('/api/products')
    products.value = res.data
}

const fetchUserSpend = async () => {
    const headers = await getAuthHeader()
    const res = await axios.get('/api/admin/user-spend', { headers })
    userSpend.value = res.data
}

const deleteProduct = async (id) => {
    const headers = await getAuthHeader()
    await axios.delete(`/api/admin/products/${id}`, { headers })
    await fetchProducts()
    await fetchUserSpend()
}

const onFileChange = (event) => {
    selectedFile.value = event.target.files[0] || null
    uploadMessage.value = ''
}

const cancelAdd = () => {
    showAddModal.value = false
    selectedFile.value = null
    uploadMessage.value = ''
    newProduct.value = { name: '', price: 0, imageUrl: null }
    const input = document.getElementById('imageInput')
    if (input) input.value = ''
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
        const uploadRes = await axios.post('/api/admin/upload-image', formData, { headers })
        const imageUrl = uploadRes.data.imageUrl

        const productPayload = {
            name: newProduct.value.name,
            price: newProduct.value.price,
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

.modal input[type='file'] {
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