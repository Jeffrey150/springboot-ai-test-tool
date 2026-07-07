import { createRouter, createWebHistory } from 'vue-router'
import TaskManagementView from '../views/TaskManagementView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      redirect: '/generate'
    },
    {
      path: '/generate',
      name: 'Generate',
      component: () => import('../views/DocumentGenerate/index.vue')
    },
     {
      path: '/tasks',
      name: 'Tasks',
      component: () => import('../views/TaskManagement/index.vue')
    },
    {
      path: '/tasks/:id',
      name: 'TaskDetail',
      component: () => import('../views/TaskDetail/index.vue')
    },
    {
      path: '/task-management',
      name: 'TaskManagement',
      component: TaskManagementView
    }
  ]
})

export default router
