import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

function getHttpErrorMessage(error) {
  const data = error?.response?.data
  if (!data) return error?.message || '请求失败'

  if (typeof data === 'string') return data
  if (typeof data.message === 'string' && data.message.trim()) return data.message

  const errors = data.errors
  if (Array.isArray(errors) && errors.length > 0) {
    const first = errors[0]
    if (typeof first === 'string' && first.trim()) return first
    if (typeof first?.defaultMessage === 'string' && first.defaultMessage.trim()) return first.defaultMessage
    if (typeof first?.message === 'string' && first.message.trim()) return first.message
  }

  if (typeof data.error === 'string' && data.error.trim()) return data.error
  return error?.message || '请求失败'
}

// 导出 api 供其他地方使用
export { api }

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const userRole = ref(localStorage.getItem('userRole') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userRole.value === 'ADMIN')
  const isPatient = computed(() => userRole.value === 'PATIENT')

  async function login(username, password) {
    const res = await api.post('/user/login', { username, password })
    if (res.code === 200) {
      token.value = res.data.token
      userRole.value = res.data.role
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userRole', res.data.role)
      return true
    }
    return false
  }

  async function register(userData) {
    try {
      const res = await api.post('/user/register', userData)
      return {
        success: res.code === 200,
        message: res.message || (res.data && res.data.message) || ''
      }
    } catch (error) {
      return {
        success: false,
        message: getHttpErrorMessage(error)
      }
    }
  }

  async function getUserInfo() {
    const res = await api.get('/user/info')
    if (res.code === 200) {
      userInfo.value = res.data
    }
    return userInfo.value
  }

  async function updateUserInfo(data) {
    const res = await api.put('/user/update', data)
    if (res.code === 200) {
      userInfo.value = { ...userInfo.value, ...data }
    }
    return res.code === 200
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    userRole.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
  }

  return {
    token,
    userInfo,
    userRole,
    isLoggedIn,
    isAdmin,
    isPatient,
    login,
    register,
    getUserInfo,
    updateUserInfo,
    logout,
    api: api
  }
})

export const usePatientStore = defineStore('patient', () => {
  const patientInfo = ref(null)
  const hasPatientInfo = ref(false)

  // 字体大小设置
  const fontSize = ref(parseInt(localStorage.getItem('patientFontSize')) || 18)

  function setFontSize(size) {
    fontSize.value = size
    localStorage.setItem('patientFontSize', size.toString())
    // 更新CSS变量
    document.documentElement.style.setProperty('--base-font-size', size + 'px')
    document.documentElement.style.setProperty('--patient-font-size', size + 'px')
  }

  function initFontSize() {
    document.documentElement.style.setProperty('--base-font-size', fontSize.value + 'px')
    document.documentElement.style.setProperty('--patient-font-size', fontSize.value + 'px')
  }

  async function getMyPatientInfo() {
    try {
      const res = await api.get('/patient/my')
      if (res.code === 200 && res.data) {
        patientInfo.value = res.data
        hasPatientInfo.value = true
        return patientInfo.value
      }
      patientInfo.value = null
      hasPatientInfo.value = false
      return null
    } catch (e) {
      // 后端异常/网络异常时，不抛出错误，避免页面 mounted 钩子崩溃
      patientInfo.value = null
      hasPatientInfo.value = false
      return null
    }
  }

  async function savePatientInfo(data) {
    const res = await api.post('/patient/add', data)
    if (res.code === 200) {
      patientInfo.value = data
      hasPatientInfo.value = true
    }
    return res.code === 200
  }

  async function updatePatientInfo(data) {
    const res = await api.put('/patient/update', data)
    if (res.code === 200) {
      patientInfo.value = { ...patientInfo.value, ...data }
    }
    return res.code === 200
  }

  return {
    patientInfo,
    hasPatientInfo,
    fontSize,
    setFontSize,
    initFontSize,
    getMyPatientInfo,
    savePatientInfo,
    updatePatientInfo
  }
})
