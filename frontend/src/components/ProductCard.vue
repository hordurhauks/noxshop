<template>
    <div class="product-card" @click="$emit('click')">
        <img :src="product.imageUrl ? `/uploads/products/${product.imageUrl}` : '/placeholder-store.png'"
            alt="Product image" class="product-card-image" />
        <div class="product-card-info">
            <h3>{{ product.name }}</h3>
            <p>{{ product.price }} kr.</p>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    product: {
        type: Object,
        required: true
    }
})
</script>

<style scoped>
.product-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s ease;
    display: flex;
    flex-direction: column;
    height: 100%;
}

.product-card:hover {
    transform: scale(1.03);
}

.product-card-image {
    width: 100%;
    height: 160px;
    object-fit: cover;
}

.product-card-info {
    padding: 1rem;
    text-align: center;
}

.product-card-info h3 {
    margin: 0.5rem 0 0.25rem;
    font-size: 1.1rem;
}

.product-card-info p {
    margin: 0;
    color: #333;
}
</style>