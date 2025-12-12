<script setup>
import { computed } from 'vue';
const props = defineProps({
  resource: Object,
  getCoverUrl: Function,
  getResourceDownloadUrl: Function,
  formatSize: Function,
  handleImageError: Function,
});
const emit = defineEmits(['play', 'read', 'audio']);
const isVideo = computed(() => {
  return props.resource && props.resource.fileType === '视频';
});
const isAudio = computed(() => {
  const type = props.resource ? props.resource.fileType : '';
  const key = props.resource ? props.resource.fileKey : '';
  return type === '音乐' || key.toLowerCase().endsWith('.flac') || key.toLowerCase().endsWith('.mp3');
});
function triggerPlay() {
  emit('play', props.resource);
}
function triggerAudioPlay() {
  emit('audio', props.resource);
}

const downloadUrl = computed(() => {
  return props.getResourceDownloadUrl(props.resource);
});
const isReadableDoc = computed(() => {
  return props.resource && props.resource.fileType === '文档';
});
function triggerRead() {
  emit('read', props.resource);
}
</script>

<template>
  <div class="resource-card">
    <img v-if="resource.cover" :src="getCoverUrl(resource.cover)" @error="handleImageError" class="card-cover"
      alt="封面" />
    <div class="card-content">
      <h3 class="card-title">{{ resource.name }}</h3>
      <p class="card-desc">{{ resource.description || '暂无描述' }}</p>
      <div class="card-meta">
        <span class="file-type">{{ resource.fileType || '其他' }}</span>
        <span class="size">{{ formatSize(resource.size) }}</span>
        <span class="download-count">📥 {{ resource.times || 0 }}</span>
      </div>

      <div class="card-actions">
        <button v-if="isAudio" @click.stop="triggerAudioPlay" class="audio-btn">
          🎧 在线听歌
        </button>
        <button v-if="isVideo" @click.stop="triggerPlay" class="play-btn">
          ▶️ 在线播放
        </button>
        <button v-if="isReadableDoc" @click.stop="triggerRead" class="read-btn">
          📖 在线阅读
        </button>
        <a :href="downloadUrl" target="_blank" @click.stop class="download-btn"
          :style="!isVideo ? { flex: 1, margin: 0 } : {}">
          📥 下载
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 12px 12px 0 0;
  background: #f5f7ff;
}

.resource-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
  height: fit-content;
  display: flex;
  flex-direction: column;
}

.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(106, 90, 249, 0.2);
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #222;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
}

.file-type {
  background: #eef2ff;
  color: #4f46e5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.size {
  color: #666;
  font-size: 12px;
  text-align: center;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 0;
}

.read-btn,
.play-btn,
.audio-btn,
.download-btn {
  flex: 1;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
  text-decoration: none;
  border: none;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}

.read-btn {
  background: #00bcd4;
  color: white;
}

.read-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 188, 212, 0.4);
}

.play-btn {
  background: linear-gradient(90deg, #5af979, #7bff9d);
  color: white;
}

.play-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.download-btn {
  background: linear-gradient(90deg, #6a5af9, #8a7bff);
  color: white;
}

.download-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(106, 90, 249, 0.3);
}

.audio-btn {
  background: linear-gradient(90deg, #ffc107, #ff9800);
  /* 橙色系 */
  color: white;
}

.audio-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
}

.read-btn:focus,
.play-btn:focus,
.audio-btn:focus,
.download-btn:focus,
.read-btn:focus-visible,
.play-btn:focus-visible,
.audio-btn:focus-visible,
.download-btn:focus-visible {
  outline: none;
  box-shadow: none;
}
</style>