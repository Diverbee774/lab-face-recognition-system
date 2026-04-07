import { createRouter, createWebHistory } from 'vue-router'
import StudentView from '@/views/StudentView.vue'
import RecognizeView from '@/views/RecognizeView.vue'
import LabView from '@/views/LabView.vue'

const routes = [
  {
    path: '/student',
    component: StudentView
  },
  {
    path: '/recognize',
    component: RecognizeView
  },
  {
    path: '/lab',
    component: LabView
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