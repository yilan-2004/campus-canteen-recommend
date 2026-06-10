<template>
  <div class="dish-card skeleton-card">
    <div class="image-wrap skeleton"></div>
    <div class="body">
      <div class="skeleton" style="height: 18px; width: 70%"></div>
      <div class="skeleton" style="height: 22px; width: 40%"></div>
      <div class="skeleton" style="height: 12px; width: 90%"></div>
      <div style="display: flex; gap: 4px">
        <div class="skeleton" style="height: 20px; width: 40px; border-radius: 10px"></div>
        <div class="skeleton" style="height: 20px; width: 40px; border-radius: 10px"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-card {
  cursor: default;
  pointer-events: none;
}
</style>
