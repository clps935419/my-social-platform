import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'posts',
      component: () => import('./pages/PostsPage.vue'),
    },
  ],
});

export default router;
