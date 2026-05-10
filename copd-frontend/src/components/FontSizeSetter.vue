<template>
  <div class="font-size-setter">
    <el-popover placement="bottom" :width="200" trigger="click">
      <template #reference>
        <el-button :size="buttonSize" circle>
          <span class="font-icon">A</span>
        </el-button>
      </template>
      <div class="font-size-panel">
        <div class="font-size-title">调节字体大小</div>
        <div class="font-size-buttons">
          <el-button
            :size="buttonSize"
            :type="currentSize === 14 ? 'primary' : 'default'"
            @click="setSize(14)"
          >
            小
          </el-button>
          <el-button
            :size="buttonSize"
            :type="currentSize === 18 ? 'primary' : 'default'"
            @click="setSize(18)"
          >
            中
          </el-button>
          <el-button
            :size="buttonSize"
            :type="currentSize === 22 ? 'primary' : 'default'"
            @click="setSize(22)"
          >
            大
          </el-button>
          <el-button
            :size="buttonSize"
            :type="currentSize === 26 ? 'primary' : 'default'"
            @click="setSize(26)"
          >
            特大
          </el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { usePatientStore } from '@/store'

const props = defineProps({
  buttonSize: {
    type: String,
    default: 'default'
  }
})

const patientStore = usePatientStore()

const currentSize = computed(() => patientStore.fontSize)

function setSize(size) {
  patientStore.setFontSize(size)
}
</script>

<style scoped>
.font-size-setter {
  display: inline-flex;
  align-items: center;
}

.font-icon {
  font-weight: bold;
  font-size: 16px;
}

.font-size-panel {
  padding: 8px 0;
}

.font-size-title {
  text-align: center;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}

.font-size-buttons {
  display: flex;
  justify-content: space-around;
  gap: 4px;
}

.font-size-buttons .el-button {
  padding: 8px 12px;
}
</style>
