import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'posts',
      component: () => import('./pages/PostsPage.vue'),
    },
    {
      path: '/posts/:postId',
      name: 'post-detail',
      component: () => import('./pages/PostDetailPage.vue'),
      props: true,
    },
  ],
});

export default router;
