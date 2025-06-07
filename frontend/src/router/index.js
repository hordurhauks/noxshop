import { createRouter, createWebHistory } from 'vue-router';
import Admin from '../pages/Admin.vue';
import Home from '../pages/Home.vue';

const routes = [
  { path: '/', component: Home },
  { path: '/admin', component: Admin }
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
