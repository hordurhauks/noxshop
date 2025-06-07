// src/firebase.js
import { initializeApp } from 'firebase/app'

const firebaseConfig = {
  apiKey: "AIzaSyAms04MGhZIdc52FiP2mJGFIiYGxpeOcX8",
  authDomain: "noxshop-90b79.firebaseapp.com",
  projectId: "noxshop-90b79",
  appId: "1:411232962370:web:9a959389ea6ae6c1bf2adb"
}

export const firebaseApp = initializeApp(firebaseConfig)