<template>
  <div class="dashboard">
    <h1>Admin Dashboard</h1>
    <div class="cards">
      <div class="card" @click="goTo('products')">
        <div class="icon">📦</div>
        <h2>Manage Products</h2>
      </div>
      <div class="card" @click="goTo('users')">
        <div class="icon">👤</div>
        <h2>View Users</h2>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
const router = useRouter()
function goTo(section) {
  router.push(`/admin/${section}`)
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: 2rem;
  background-color: #f3f4f6;
  text-align: center;
  font-family: Arial, sans-serif;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 2rem;
}

.cards {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2rem;
}

.card {
  background-color: white;
  border-radius: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 220px;
  padding: 2rem;
  cursor: pointer;
  transition: box-shadow 0.3s ease;
  user-select: none;
}

.card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

h2 {
  font-size: 1.25rem;
  margin: 0;
}

@media (max-width: 500px) {
  .cards {
    flex-direction: column;
  }

  .card {
    width: 100%;
  }
}
</style>
