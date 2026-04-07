<template>
  <div class="recognize-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>人脸识别通行</span>
          <el-select v-model="selectedLabId" placeholder="选择实验室" style="width: 200px">
            <el-option
              v-for="lab in labs"
              :key="lab.id"
              :label="lab.name"
              :value="lab.id"
            />
          </el-select>
        </div>
      </template>

      <div class="camera-container">
        <div class="video-wrapper">
          <canvas ref="canvasRef"></canvas>
        </div>

        <div class="result-panel">
          <h3>识别结果</h3>
          <div v-if="!matched" class="no-result">
            <el-icon :size="60"><User /></el-icon>
            <p>{{ hasFace ? '等待匹配...' : '等待识别...' }}</p>
          </div>
          <div v-else class="matched">
            <el-avatar :size="80" :src="'data:image/jpeg;base64,' + matched.imageBase64">
              {{ matched.name }}
            </el-avatar>
            <div class="info">
              <p class="name">{{ matched.name }}</p>
              <p class="student-no">{{ matched.studentNo }}</p>
              <el-tag :type="matched.hasAccess ? 'success' : 'danger'" size="large">
                {{ matched.hasAccess ? '允许通行' : '无权限' }}
              </el-tag>
            </div>
          </div>
          <div class="log">
            <h4>通行日志</h4>
            <div class="log-list">
              <div v-for="(item, index) in logs" :key="index" class="log-item">
                <span class="time">{{ item.time }}</span>
                <span class="name">{{ item.name }}</span>
                <el-tag :type="item.hasAccess ? 'success' : 'danger'" size="small">
                  {{ item.hasAccess ? '通过' : '拒绝' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { User } from '@element-plus/icons-vue'
import { recognizeFace } from '@/api/access'
import { getLabList } from '@/api/lab'

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
  if (!canvasRef.value || !video || video.readyState < 2) return

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

  if (logs.value.length > 10) {
    logs.value.pop()
  }
}

async function fetchLabs() {
  try {
    const res = await getLabList()
    labs.value = res.data || []
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
.recognize-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.camera-container {
  display: flex;
  gap: 20px;
}

.video-wrapper {
  width: 640px;
  height: 480px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-wrapper canvas {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-panel {
  flex: 1;
  min-width: 300px;
}

.result-panel h3 {
  margin: 0 0 20px 0;
}

.no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
}

.no-result p {
  margin-top: 10px;
}

.matched {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.matched .info .name {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 5px 0;
}

.matched .info .student-no {
  color: #666;
  margin: 0 0 10px 0;
}

.log h4 {
  margin: 0 0 10px 0;
}

.log-list {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-bottom: 1px solid #eee;
}

.log-item .time {
  color: #999;
  font-size: 12px;
}

.log-item .name {
  flex: 1;
}
</style>