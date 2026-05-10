import { createRouter, createWebHistory } from 'vue-router'
import { usePatientStore } from '@/store'
import { pinia } from '@/pinia'
import { ElMessageBox } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/patient',
    name: 'PatientLayout',
    component: () => import('@/views/patient/PatientLayout.vue'),
    meta: { requiresAuth: true, role: 'PATIENT' },
    children: [
      {
        path: '',
        redirect: '/patient/home'
      },
      {
        path: 'home',
        name: 'PatientHome',
        component: () => import('@/views/patient/Home.vue')
      },
      {
        path: 'patient-info',
        name: 'PatientInfo',
        component: () => import('@/views/patient/PatientInfo.vue')
      },
      {
        path: 'assessment',
        redirect: '/patient/assessment/abe'
      },
      {
        path: 'assessment/abe',
        redirect: '/patient/assessment/abe/cat'
      },
      {
        path: 'assessment/abe/cat',
        name: 'CatAssessment',
        component: () => import('@/views/patient/CatAssessment.vue'),
        meta: { requiresPatientInfo: true }
      },
      {
        path: 'assessment/abe/mmrc',
        name: 'MmrcAssessment',
        component: () => import('@/views/patient/MmrcAssessment.vue'),
        meta: { requiresPatientInfo: true }
      },
      {
        path: 'assessment/dyspnoea12',
        name: 'Dyspnoea12Assessment',
        component: () => import('@/views/patient/Dyspnoea12Assessment.vue'),
        meta: { requiresPatientInfo: true }
      },
      // 兼容旧路径
      {
        path: 'assessment/cat',
        redirect: '/patient/assessment/abe/cat'
      },
      {
        path: 'assessment/mmrc',
        redirect: '/patient/assessment/abe/mmrc'
      },
      {
        path: 'result',
        name: 'PatientResult',
        component: () => import('@/views/patient/Result.vue')
      },
      {
        path: 'history',
        name: 'PatientHistory',
        component: () => import('@/views/patient/History.vue')
      },
      {
        path: 'profile',
        name: 'PatientProfile',
        component: () => import('@/views/patient/Profile.vue')
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'patients',
        name: 'PatientList',
        component: () => import('@/views/admin/PatientList.vue')
      },
      {
        path: 'patients/:id',
        name: 'PatientDetail',
        component: () => import('@/views/admin/PatientDetail.vue')
      },
      {
        path: 'patients/edit/:id',
        name: 'PatientEdit',
        component: () => import('@/views/admin/PatientEdit.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/admin/Statistics.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== userRole) {
      if (userRole === 'ADMIN') {
        next('/admin')
      } else {
        next('/patient')
      }
    } else {
      // 患者端评估入口：未填写信息则说明并跳转
      if (userRole === 'PATIENT' && to.meta.requiresPatientInfo) {
        const patientStore = usePatientStore(pinia)
        try {
          await patientStore.getMyPatientInfo()
        } catch (e) {
          // 网络异常时不阻断路由，让页面自行处理
          next()
          return
        }

        if (!patientStore.hasPatientInfo) {
          sessionStorage.setItem('patientInfoRedirectReason', '您还未填写患者基本信息，无法进行评估。请先完善信息后再开始问卷。')
          sessionStorage.setItem('patientInfoRedirectTo', to.fullPath)

          // 说明弹窗（字号跟随 CSS 变量）
          try {
            await ElMessageBox.alert(
              '您还未填写患者基本信息，无法进行评估。\n\n请先填写患者信息后再开始问卷。',
              '提示',
              {
                type: 'warning',
                confirmButtonText: '去填写',
                showClose: false,
                closeOnClickModal: false,
                customClass: 'patient-fontsize-messagebox'
              }
            )
          } finally {
            next({ path: '/patient/patient-info', query: { redirect: to.fullPath } })
          }
          return
        }
      }

      next()
    }
  } else {
    next()
  }
})

export default router
