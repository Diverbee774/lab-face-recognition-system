import { createRouter, createWebHistory } from 'vue-router'
import StudentView from '@/views/StudentView.vue'

const routes = [
  {
    path: '/student',
    component: StudentView
  },
  {
    path: '/',
    redirect: '/student'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router