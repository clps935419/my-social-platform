<template>
  <div class="post-content">{{ content }}</div>
</template>

<script setup lang="ts">
/**
 * PostContent component - Renders user-provided content as plain text
 * 
 * XSS Prevention:
 * - Uses Vue's default interpolation ({{ }}) which auto-escapes HTML
 * - NEVER uses v-html to prevent XSS attacks
 * - All user-provided content (posts, comments) should use this component
 */
defineProps<{
  content: string;
}>();
</script>

<style scoped>
.post-content {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
