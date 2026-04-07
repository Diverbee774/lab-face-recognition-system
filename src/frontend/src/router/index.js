import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import AdminLayout from '@/views/AdminLayout.vue'
import StudentView from '@/views/StudentView.vue'
import RecognizeView from '@/views/RecognizeView.vue'
import LabView from '@/views/LabView.vue'

const routes = [
  {
    path: '/login',
    component: LoginView
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
    path: '/',
    redirect: '/admin/student'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
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