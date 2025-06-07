import axios from 'axios'
import { createApp } from 'vue'
import App from './App.vue'
import './firebase'
import router from './router'
import './style.css'

axios.defaults.withCredentials = true;

createApp(App).use(router).mount('#app')
