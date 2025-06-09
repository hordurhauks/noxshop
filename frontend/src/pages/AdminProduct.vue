<template>
    <div class="admin-dashboard">
        <h1>Admin Dashboard</h1>

        <section>
            <h2>Products</h2>

            <div v-if="loading" class="loading">Loading...</div>

            <!-- Add New Product as plus icon button -->
            <button class="add-product-btn" @click="openAddModal" aria-label="Add New Product">
                <span>＋</span>
            </button>

            <div class="product-grid">
                <ProductCard v-for="product in products" :key="product.id" :product="product"
                    @select-product="openEditModal" />
            </div>

            <!-- Add / Edit Modal -->
            <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
                <div class="modal">
                    <h3>{{ editingProduct ? 'Edit Product' : 'Add New Product' }}</h3>
                    <form @submit.prevent="editingProduct ? confirmEditProduct() : confirmAddProduct()">

                        <label for="productName">Product Name:</label>
                        <input id="productName" v-model="modalProduct.name" placeholder="Product name" required />

                        <label for="productPrice">Price (ISK):</label>
                        <input id="productPrice" v-model.number="modalProduct.price" placeholder="Price" type="number"
                            min="0" step="1" required />

                        <label for="imageInput">Product Image:</label>
                        <input id="imageInput" type="file" @change="onFileChange" accept="image/*" :key="fileInputKey"
                            :required="!editingProduct" />
                        <p v-if="uploadMessage" class="upload-message">{{ uploadMessage }}</p>

                        <div v-if="imagePreview || modalProduct.imageUrl" class="image-preview">
                            <img :src="imagePreview || modalProduct.imageUrl" alt="Preview" />
                        </div>

                        <div class="modal-buttons">
                            <button type="submit" :disabled="!modalProduct.name || modalProduct.price <= 0">
                                Confirm
                            </button>
                            <button v-if="editingProduct" type="button" class="remove-btn"
                                @click="confirmDeleteProduct(modalProduct.id)">
                                Remove
                            </button>
                            <button type="button" @click="closeModal">Cancel</button>
                        </div>
                    </form>
                </div>
            </div>
        </section>
    </div>
</template>

<script setup>
import axios from 'axios'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import { onMounted, reactive, ref } from 'vue'
import ProductCard from '../components/ProductCard.vue'

const user = ref(null)
const products = ref([])
const loading = ref(false)

const showModal = ref(false)
const editingProduct = ref(null) // null = adding new, else product object
const modalProduct = reactive({ id: null, name: '', price: 0, imageUrl: null })
const selectedFile = ref(null)
const imagePreview = ref(null)
const uploadMessage = ref('')
const fileInputKey = ref(Date.now())

const getAuthHeader = async () => {
    const user = getAuth().currentUser
    if (!user) throw new Error('User not logged in')
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

function openAddModal() {
    editingProduct.value = null
    modalProduct.id = null
    modalProduct.name = ''
    modalProduct.price = 0
    modalProduct.imageUrl = null
    selectedFile.value = null
    imagePreview.value = null
    uploadMessage.value = ''
    fileInputKey.value = Date.now()
    showModal.value = true
}

function openEditModal(product) {
    editingProduct.value = product
    modalProduct.id = product.id
    modalProduct.name = product.name
    modalProduct.price = product.price
    modalProduct.imageUrl = product.imageUrl
    selectedFile.value = null
    imagePreview.value = null
    uploadMessage.value = ''
    fileInputKey.value = Date.now()
    showModal.value = true
}

function closeModal() {
    showModal.value = false
    selectedFile.value = null
    imagePreview.value = null
    uploadMessage.value = ''
}

const onFileChange = (event) => {
    selectedFile.value = event.target.files[0] || null
    uploadMessage.value = ''
    if (selectedFile.value) {
        const reader = new FileReader()
        reader.onload = (e) => (imagePreview.value = e.target.result)
        reader.readAsDataURL(selectedFile.value)
    } else {
        imagePreview.value = null
    }
}

async function confirmAddProduct() {
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
            name: modalProduct.name,
            price: modalProduct.price,
            imageUrl,
        }

        await axios.post('/api/admin/products', productPayload, { headers })

        await fetchProducts()
        closeModal()
    } catch (err) {
        uploadMessage.value = 'Error: ' + (err.response?.data?.message || err.message)
    }
}

async function confirmEditProduct() {
    try {
        let imageUrl = modalProduct.imageUrl
        if (selectedFile.value) {
            const formData = new FormData()
            formData.append('file', selectedFile.value)
            const headers = await getAuthHeader()
            const uploadRes = await axios.post('/api/admin/upload-image', formData, { headers })
            imageUrl = uploadRes.data.imageUrl
        }

        const payload = {
            name: modalProduct.name,
            price: modalProduct.price,
            imageUrl,
        }

        const headers = await getAuthHeader()
        await axios.put(`/api/admin/products/${modalProduct.id}`, payload, { headers })

        await fetchProducts()
        closeModal()
    } catch (err) {
        uploadMessage.value = 'Error: ' + (err.response?.data?.message || err.message)
    }
}

async function confirmDeleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return
    const headers = await getAuthHeader()
    await axios.delete(`/api/admin/products/${id}`, { headers })
    await fetchProducts()
    closeModal()
}

onMounted(() => {
    const auth = getAuth()
    onAuthStateChanged(auth, (u) => {
        user.value = u
        if (u) {
            fetchProducts()
        }
    })
})
</script>

<style scoped>
.admin-dashboard {
    padding: 2rem;
    max-width: 700px;
    margin: auto;
}

.loading {
    font-style: italic;
    margin-bottom: 1rem;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 1rem;
    margin-top: 1rem;
}

.add-product-btn {
    font-size: 2rem;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s ease;
    margin-bottom: 1rem;
}

.add-product-btn:hover {
    background: #0056b3;
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

/* Stack inputs vertically with margin */
.modal label {
    display: block;
    margin-top: 1rem;
    font-weight: 600;
    font-size: 0.9rem;
}

.modal input[type='text'],
.modal input[type='number'],
.modal input[type='file'] {
    display: block;
    width: 100%;
    padding: 0.4rem 0.6rem;
    margin-top: 0.3rem;
    border-radius: 4px;
    border: 1px solid #ccc;
    box-sizing: border-box;
}

.modal input[type='file'] {
    padding: 0.3rem 0.4rem;
}

.modal-buttons {
    margin-top: 1.5rem;
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
}

.modal-buttons button {
    padding: 0.4rem 1rem;
    cursor: pointer;
}

.remove-btn {
    background: #dc3545;
    color: white;
    border: none;
    border-radius: 4px;
}

.remove-btn:hover {
    background: #c82333;
}

.image-preview {
    margin-top: 0.5rem;
    text-align: center;
}

.image-preview img {
    max-width: 100%;
    max-height: 120px;
    border-radius: 4px;
    object-fit: contain;
}

.upload-message {
    color: red;
    font-size: 0.9rem;
    margin-top: 0.3rem;
}
</style>
