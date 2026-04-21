import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import AdminLayout from '@/views/AdminLayout.vue'
import StudentView from '@/views/StudentView.vue'
import RecognizeView from '@/views/RecognizeView.vue'
import LabView from '@/views/LabView.vue'
import PublicRecognizeView from '@/views/PublicRecognizeView.vue'

const routes = [
  {
    path: '/login',
    component: LoginView
  },
  {
    path: '/',
    component: PublicRecognizeView
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/student',
    children: [
      {
        path: 'student',
        component: StudentView
      },
      {
        path: 'lab',
        component: LabView
      },
      {
        path: 'recognize',
        component: RecognizeView
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login' || to.path === '/') {
    next()
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router