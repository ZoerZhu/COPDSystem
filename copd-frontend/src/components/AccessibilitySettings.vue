<template>
  <div class="accessibility-settings">
    <el-drawer
      v-model="visible"
      title="无障碍设置"
      direction="rtl"
      size="320px"
    >
      <div class="settings-content">
        <div class="setting-item">
          <div class="setting-header">
            <el-icon :size="20"><Sunny /></el-icon>
            <span>字号大小</span>
          </div>
          <el-radio-group v-model="settings.fontSize" @change="handleFontSizeChange">
            <el-radio value="normal">标准</el-radio>
            <el-radio value="large">大</el-radio>
            <el-radio value="extra-large">特大</el-radio>
          </el-radio-group>
        </div>

        <el-divider />

        <div class="setting-item">
          <div class="setting-header">
            <el-icon :size="20"><Brush /></el-icon>
            <span>高对比度</span>
          </div>
          <el-switch
            v-model="settings.highContrast"
            @change="handleHighContrastChange"
          />
        </div>

        <div class="setting-item">
          <div class="setting-header">
            <el-icon :size="20"><MagicStick /></el-icon>
            <span>简化界面</span>
          </div>
          <el-switch
            v-model="settings.simplified"
            @change="handleSimplifiedChange"
          />
        </div>

        <el-divider />

        <div class="setting-item">
          <div class="setting-header">
            <el-icon :size="20"><Keyboard /></el-icon>
            <span>键盘快捷键</span>
          </div>
          <el-switch
            v-model="settings.keyboardNav"
            @change="handleKeyboardNavChange"
          />
        </div>

        <el-divider />

        <div class="quick-tips">
          <h4>快捷键说明</h4>
          <ul>
            <li><kbd>Tab</kbd> 切换焦点</li>
            <li><kbd>Enter</kbd> 确认选择</li>
            <li><kbd>Esc</kbd> 关闭弹窗</li>
            <li><kbd>↑</kbd><kbd>↓</kbd> 移动选项</li>
          </ul>
        </div>

        <el-button class="reset-btn" @click="handleReset">恢复默认设置</el-button>
      </div>
    </el-drawer>

    <!-- 快捷入口按钮 -->
    <Teleport to="body">
      <Transition name="fade">
        <button
          v-if="showFab"
          class="accessibility-fab"
          @click="visible = true"
          aria-label="打开无障碍设置"
        >
          <el-icon :size="24"><Setting /></el-icon>
        </button>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Sunny, Brush, MagicStick, Keyboard, Setting } from '@element-plus/icons-vue'
import { usePatientStore } from '@/store'

const visible = ref(false)
const showFab = ref(true)

const patientStore = usePatientStore()

const settings = reactive({
  fontSize: 'normal',
  highContrast: false,
  simplified: false,
  keyboardNav: true
})

const STORAGE_KEY = 'accessibility_settings'

onMounted(() => {
  loadSettings()
  applySettings()
  window.addEventListener('keydown', handleKeyboardShortcuts)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyboardShortcuts)
})

function loadSettings() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      Object.assign(settings, parsed)
    }
  } catch (e) {
    console.error(e)
  }
}

function saveSettings() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
  } catch (e) {
    console.error(e)
  }
}

function applySettings() {
  const root = document.documentElement

  // 字号
  const fontSizes = {
    normal: '16px',
    large: '18px',
    'extra-large': '20px'
  }
  const fontSizePx = fontSizes[settings.fontSize] || fontSizes.normal
  root.style.setProperty('--base-font-size', fontSizePx)
  root.style.setProperty('--patient-font-size', fontSizePx)

  // 同步到患者端字号状态（CAT/mMRC 等页面依赖该变量）
  const parsed = parseInt(fontSizePx, 10)
  if (!Number.isNaN(parsed) && patientStore.fontSize !== parsed) {
    patientStore.setFontSize(parsed)
  }

  // 高对比度
  if (settings.highContrast) {
    root.classList.add('high-contrast')
  } else {
    root.classList.remove('high-contrast')
  }

  // 简化界面
  if (settings.simplified) {
    root.classList.add('simplified')
  } else {
    root.classList.remove('simplified')
  }
}

function handleFontSizeChange() {
  saveSettings()
  applySettings()
}

function handleHighContrastChange() {
  saveSettings()
  applySettings()
}

function handleSimplifiedChange() {
  saveSettings()
  applySettings()
}

function handleKeyboardNavChange() {
  saveSettings()
}

function handleReset() {
  settings.fontSize = 'normal'
  settings.highContrast = false
  settings.simplified = false
  settings.keyboardNav = true
  saveSettings()
  applySettings()
}

function handleKeyboardShortcuts(e) {
  // Ctrl+Shift+A 打开设置
  if (e.ctrlKey && e.shiftKey && e.key === 'A') {
    e.preventDefault()
    visible.value = !visible.value
  }
}
</script>

<style scoped>
.settings-content {
  padding: 0 0.5rem;
}

.setting-item {
  margin-bottom: 1.5rem;
}

.setting-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.setting-item :deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.quick-tips {
  background: var(--bg-page);
  padding: 1rem;
  border-radius: var(--radius-sm);
  margin-bottom: 1.5rem;
}

.quick-tips h4 {
  font-size: 0.875rem;
  margin: 0 0 0.75rem 0;
  color: var(--text-primary);
}

.quick-tips ul {
  margin: 0;
  padding-left: 1rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.quick-tips li {
  margin-bottom: 0.5rem;
}

.quick-tips kbd {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 0.125rem 0.375rem;
  font-family: monospace;
  font-size: 0.75rem;
}

.reset-btn {
  width: 100%;
}

.accessibility-fab {
  position: fixed;
  bottom: 80px;
  right: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--primary);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-fast);
  z-index: 1000;
}

.accessibility-fab:hover {
  transform: scale(1.1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .fade-enter-active,
  .fade-leave-active {
    transition: none;
  }
}
</style>
