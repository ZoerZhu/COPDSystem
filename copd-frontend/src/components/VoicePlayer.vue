<template>
  <div class="voice-player">
    <el-button
      :size="buttonSize"
      :type="playing ? 'success' : 'primary'"
      circle
      @click="togglePlay"
    >
      <el-icon v-if="!playing" :size="iconSize"><Microphone /></el-icon>
      <el-icon v-else :size="iconSize"><VideoPause /></el-icon>
    </el-button>
    <span v-if="showLabel" class="voice-label">{{ playing ? '播放中...' : '播放语音' }}</span>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { Microphone, VideoPause } from '@element-plus/icons-vue'

const props = defineProps({
  text: {
    type: String,
    required: true
  },
  buttonSize: {
    type: String,
    default: 'large'
  },
  showLabel: {
    type: Boolean,
    default: true
  }
})

const playing = ref(false)
let utterance = null

const iconSize = computed(() => {
  const sizeMap = { large: 24, default: 20, small: 16 }
  return sizeMap[props.buttonSize] || 24
})

function togglePlay() {
  if (playing.value) {
    stop()
  } else {
    play()
  }
}

function play() {
  if (!props.text) return

  // 停止之前的播放
  if (utterance) {
    window.speechSynthesis.cancel()
  }

  utterance = new SpeechSynthesisUtterance(props.text)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.8  // 放慢语速，适合老年人
  utterance.pitch = 1

  utterance.onstart = () => {
    playing.value = true
  }

  utterance.onend = () => {
    playing.value = false
  }

  utterance.onerror = () => {
    playing.value = false
  }

  window.speechSynthesis.speak(utterance)
}

function stop() {
  window.speechSynthesis.cancel()
  playing.value = false
}

watch(() => props.text, () => {
  if (playing.value) {
    stop()
  }
})

onUnmounted(() => {
  stop()
})

defineExpose({ play, stop })
</script>

<style scoped>
.voice-player {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.voice-label {
  font-size: 14px;
  color: #606266;
}
</style>
