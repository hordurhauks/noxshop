import { createRouter, createWebHistory } from 'vue-router';

import Admin from '../pages/Admin.vue';
import AdminProducts from '../pages/AdminProduct.vue';
import AdminUsers from '../pages/AdminUser.vue';
import Home from '../pages/Home.vue';

const routes = [
  { path: '/', component: Home },
  { path: '/admin', component: Admin },
  { path: '/admin/products', name: 'AdminProducts', component: AdminProducts },
  { path: '/admin/users', name: 'AdminUsers', component: AdminUsers },
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
