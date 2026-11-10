import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

// Authentication is held by the server session, never by localStorage.
localStorage.removeItem('token')
localStorage.removeItem('userInfo')

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  async function refresh() {
    try {
      const { data } = await request.get('/auth/me', { silent: true })
      userInfo.value = data
      return true
    } catch (error) {
      userInfo.value = null
      if (error.response?.status === 401) return false
      throw error
    }
  }
  async function login(username, password) {
    const { data } = await request.post('/auth/login', new URLSearchParams({ username, password }))
    userInfo.value = data
  }
  async function logout() {
    await request.post('/auth/logout')
    userInfo.value = null
  }
  return { userInfo, refresh, login, logout }
})
