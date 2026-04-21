<template>
  <div class="recognize-page">
    <div class="header">
      <h1>🧪 实验室人脸识别通行</h1>
    </div>

    <div class="main-content">
      <div class="lab-info-section">
        <el-select v-model="selectedLabId" placeholder="选择实验室" size="large" style="width: 300px">
          <el-option
            v-for="lab in labs"
            :key="lab.id"
            :value="lab.id"
          >
            <span>{{ lab.name }}</span>
            <el-tag v-if="lab.accessMode === 1" type="success" size="small" style="margin-left: 8px">白名单</el-tag>
            <el-tag v-else-if="lab.accessMode === 2" type="primary" size="small" style="margin-left: 8px">开放</el-tag>
            <el-tag v-else-if="lab.accessMode === 3" type="danger" size="small" style="margin-left: 8px">维护</el-tag>
          </el-option>
        </el-select>

        <div v-if="selectedLab" class="selected-lab-card">
          <div class="lab-header">
            <div class="lab-icon">🏫</div>
            <div class="lab-title">
              <div class="lab-name">{{ selectedLab.name }}</div>
              <div class="lab-location">{{ selectedLab.location }}</div>
            </div>
            <el-tag v-if="selectedLab.accessMode === 1" type="success">白名单模式</el-tag>
            <el-tag v-else-if="selectedLab.accessMode === 2" type="primary">开放访问</el-tag>
            <el-tag v-else-if="selectedLab.accessMode === 3" type="danger">维护中</el-tag>
          </div>
        </div>
      </div>

      <div class="video-section">
        <div class="video-wrapper">
          <canvas ref="canvasRef"></canvas>
          <div v-if="!selectedLabId" class="overlay">
            <p>请先选择实验室</p>
          </div>
          <div v-else-if="!hasFace" class="overlay">
            <p>请将面部对准摄像头</p>
          </div>
        </div>
      </div>

      <div class="result-section">
        <div class="result-card">
          <div v-if="!hasFace" class="result waiting">
            <div class="icon">📷</div>
            <p>等待识别...</p>
          </div>
          <div v-else-if="matched" class="result success">
            <div class="avatar-wrapper">
              <img v-if="matched.imageBase64" :src="'data:image/jpeg;base64,' + matched.imageBase64" class="avatar" />
              <div v-else class="avatar-placeholder">{{ matched.name?.[0] || '?' }}</div>
            </div>
            <div class="info">
              <p class="name">{{ matched.name }}</p>
              <p class="student-no">{{ matched.studentNo }}</p>
            </div>
            <div class="status" :class="matched.hasAccess ? 'allowed' : 'denied'">
              {{ matched.hasAccess ? '✓ 允许通行' : '✗ 无权限' }}
            </div>
          </div>
          <div v-else class="result failed">
            <div class="icon">⚠️</div>
            <p>未识别到白名单人员</p>
          </div>
        </div>

        <div class="logs-card">
          <h3>通行记录</h3>
          <div class="log-list">
            <div v-for="(log, i) in logs" :key="i" class="log-item">
              <span class="time">{{ log.time }}</span>
              <span class="name">{{ log.name }}</span>
              <span class="status" :class="log.hasAccess ? 'allowed' : 'denied'">
                {{ log.hasAccess ? '通过' : '拒绝' }}
              </span>
            </div>
            <div v-if="logs.length === 0" class="no-logs">暂无记录</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { recognizeFace } from '@/api/access'
import { getLabList } from '@/api/lab'

const selectedLab = computed(() => labs.value.find(l => l.id === selectedLabId.value))

const canvasRef = ref(null)

const selectedLabId = ref(null)
const labs = ref([])
const matched = ref(null)
const hasFace = ref(false)
const logs = ref([])

let video = null
let stream = null
let drawInterval = null
let recognizeInterval = null

function initCamera() {
  video = document.createElement('video')
  video.autoplay = true
  video.playsInline = true

  navigator.mediaDevices.getUserMedia({ video: { width: 640, height: 480 } })
    .then(s => {
      stream = s
      video.srcObject = s
      video.onloadedmetadata = () => {
        canvasRef.value.width = video.videoWidth
        canvasRef.value.height = video.videoHeight
        startDraw()
      }
    })
    .catch(err => {
      console.error('摄像头打开失败:', err)
    })
}

function startDraw() {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')

  drawInterval = setInterval(() => {
    if (!video || video.readyState < 2) return

    ctx.drawImage(video, 0, 0)

    if (matched.value && matched.value.faceLocation) {
      const loc = matched.value.faceLocation
      ctx.strokeStyle = matched.value.hasAccess ? '#00ff00' : '#ff0000'
      ctx.lineWidth = 3
      ctx.strokeRect(loc.left, loc.top, loc.right - loc.left, loc.bottom - loc.top)
    }
  }, 50)
}

async function recognize() {
  if (!canvasRef.value || !video || video.readyState < 2 || !selectedLabId.value) return

  const canvas = canvasRef.value
  const imageBase64 = canvas.toDataURL('image/jpeg').split(',')[1]

  try {
    const data = await recognizeFace({
      imageBase64,
      labId: selectedLabId.value
    })

    if (data.code === 200 && data.data) {
      hasFace.value = data.data.hasFace
      if (data.data.hasFace) {
        matched.value = data.data
        if (data.data.matched) {
          addLog(data.data)
        }
      } else {
        matched.value = null
      }
    } else {
      hasFace.value = false
      matched.value = null
    }
  } catch (err) {
    console.error('识别失败:', err)
  }
}

function addLog(student) {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`

  const lastLog = logs.value[0]
  if (lastLog && lastLog.name === `${student.name} (${student.studentNo})` && lastLog.time === time) {
    return
  }

  logs.value.unshift({
    time,
    name: student ? `${student.name} (${student.studentNo})` : '未识别',
    hasAccess: student ? student.hasAccess : false
  })

  if (logs.value.length > 20) {
    logs.value.pop()
  }
}

async function fetchLabs() {
  try {
    const res = await getLabList({ page: 1, pageSize: 100 })
    labs.value = res.data.list || []
    if (labs.value.length > 0) {
      selectedLabId.value = labs.value[0].id
    }
  } catch (err) {
    console.error('获取实验室列表失败:', err)
  }
}

onMounted(async () => {
  await fetchLabs()
  initCamera()
  recognizeInterval = setInterval(recognize, 500)
})

onUnmounted(() => {
  if (drawInterval) clearInterval(drawInterval)
  if (recognizeInterval) clearInterval(recognizeInterval)
  if (stream) stream.getTracks().forEach(track => track.stop())
})
</script>

<style scoped>
.recognize-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(0,0,0,0.2);
}

.header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.lab-select {
  width: 200px;
}

.main-content {
  display: flex;
  gap: 40px;
  padding: 40px;
  justify-content: center;
}

.lab-info-section {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.selected-lab-card {
  background: rgba(255,255,255,0.1);
  border-radius: 16px;
  padding: 25px;
  backdrop-filter: blur(10px);
}

.lab-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.lab-icon {
  font-size: 40px;
}

.lab-title {
  flex: 1;
}

.lab-title .lab-name {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 5px;
}

.lab-title .lab-location {
  font-size: 14px;
  color: rgba(255,255,255,0.6);
}

.video-section {
  flex-shrink: 0;
}

.video-wrapper {
  width: 640px;
  height: 480px;
  background: #000;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.video-wrapper canvas {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 18px;
}

.result-section {
  width: 350px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-card {
  background: rgba(255,255,255,0.1);
  border-radius: 16px;
  padding: 30px;
  backdrop-filter: blur(10px);
}

.result {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.result.waiting .icon,
.result.failed .icon {
  font-size: 60px;
  margin-bottom: 15px;
}

.result p {
  margin: 0;
  font-size: 16px;
  color: rgba(255,255,255,0.8);
}

.result.success .avatar-wrapper {
  margin-bottom: 15px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #00ff00;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  border: 4px solid #00ff00;
}

.result.success .info {
  margin-bottom: 15px;
}

.result.success .name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #fff;
}

.result.success .student-no {
  font-size: 14px;
  color: rgba(255,255,255,0.7);
  margin: 0;
}

.status {
  padding: 10px 30px;
  border-radius: 30px;
  font-size: 18px;
  font-weight: 600;
}

.status.allowed {
  background: rgba(0, 255, 0, 0.2);
  color: #00ff00;
  border: 2px solid #00ff00;
}

.status.denied {
  background: rgba(255, 0, 0, 0.2);
  color: #ff4444;
  border: 2px solid #ff4444;
}

.logs-card {
  background: rgba(255,255,255,0.1);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(10px);
  flex: 1;
}

.logs-card h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: rgba(255,255,255,0.9);
}

.log-list {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.log-item:last-child {
  border-bottom: none;
}

.log-item .time {
  color: rgba(255,255,255,0.5);
  font-size: 12px;
}

.log-item .name {
  flex: 1;
  color: rgba(255,255,255,0.9);
  font-size: 14px;
}

.log-item .status {
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
}

.log-item .status.allowed {
  background: rgba(0, 255, 0, 0.2);
  color: #00ff00;
}

.log-item .status.denied {
  background: rgba(255, 0, 0, 0.2);
  color: #ff4444;
}

.no-logs {
  text-align: center;
  color: rgba(255,255,255,0.5);
  padding: 30px 0;
}
</style>