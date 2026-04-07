<template>
  <div class="student-view">
    <div class="page-header">
      <h2>学生管理</h2>
      <el-button type="primary" @click="openAddDialog">注册学生</el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="students" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hasFace" label="人脸" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.hasFace === 1" type="success" size="small">已录入</el-tag>
            <el-tag v-else type="warning" size="small">未录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑学生' : '注册学生'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item :label="isEdit ? '新人脸' : '照片'" prop="imageBase64">
          <div class="image-input-area">
            <div class="mode-switch">
              <el-radio-group v-model="imageMode">
                <el-radio-button label="upload">上传图片</el-radio-button>
                <el-radio-button label="camera">摄像头拍摄</el-radio-button>
              </el-radio-group>
            </div>

            <div v-if="imageMode === 'upload'" class="upload-area">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleImageChange"
              >
                <el-button>选择图片</el-button>
                <span v-if="form.imageBase64" style="margin-left: 10px; color: #67c23a">已选择</span>
              </el-upload>
            </div>

            <div v-else class="camera-area">
              <div class="camera-wrapper">
                <video ref="registerVideoRef" autoplay playsinline></video>
                <canvas ref="registerCanvasRef" class="hidden-canvas"></canvas>
              </div>
              <div class="camera-controls">
                <el-button v-if="!cameraActive" type="primary" @click="startCamera">打开摄像头</el-button>
                <el-button v-else type="success" @click="capturePhoto">拍照</el-button>
                <el-button v-if="cameraActive" @click="stopCamera">关闭</el-button>
                <span v-if="form.imageBase64 && imageMode === 'camera'" style="margin-left: 10px; color: #67c23a">已拍摄</span>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">{{ isEdit ? '更新' : '注册' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, registerStudent, updateStudent, deleteStudent } from '@/api/student'

const students = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref()
const imageMode = ref('upload')

const registerVideoRef = ref(null)
const registerCanvasRef = ref(null)

let cameraStream = null

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  status: 1,
  imageBase64: ''
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function fetchStudents() {
  try {
    const res = await getStudentList()
    students.value = res.data || []
  } catch (e) {
    ElMessage.error('获取学生列表失败')
  }
}

function openAddDialog() {
  isEdit.value = false
  imageMode.value = 'upload'
  stopCamera()
  Object.assign(form, { id: null, studentNo: '', name: '', status: 1, imageBase64: '' })
  showDialog.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  imageMode.value = 'upload'
  stopCamera()
  Object.assign(form, { id: row.id, studentNo: row.studentNo, name: row.name, status: row.status, imageBase64: '' })
  showDialog.value = true
}

function handleImageChange(file) {
  const reader = new FileReader()
  reader.onload = e => {
    const base64 = e.target.result
    form.imageBase64 = base64.includes(',') ? base64.split(',')[1] : base64
  }
  reader.readAsDataURL(file.raw)
}

async function startCamera() {
  try {
    cameraStream = await navigator.mediaDevices.getUserMedia({ video: true })
    registerVideoRef.value.srcObject = cameraStream
    cameraActive.value = true
  } catch (err) {
    ElMessage.error('摄像头打开失败')
  }
}

function stopCamera() {
  if (cameraStream) {
    cameraStream.getTracks().forEach(track => track.stop())
    cameraStream = null
  }
  cameraActive.value = false
}

function capturePhoto() {
  const video = registerVideoRef.value
  const canvas = registerCanvasRef.value
  const ctx = canvas.getContext('2d')

  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  ctx.drawImage(video, 0, 0)

  form.imageBase64 = canvas.toDataURL('image/jpeg').split(',')[1]
  ElMessage.success('拍照成功')
}

function handleDialogClose() {
  stopCamera()
  showDialog.value = false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!isEdit.value && !form.imageBase64) {
      ElMessage.warning('请选择或拍摄图片')
      return
    }

    loading.value = true
    try {
      if (isEdit.value) {
        await updateStudent({
          id: form.id,
          name: form.name,
          status: form.status
        })
        ElMessage.success('更新成功')
      } else {
        await registerStudent(form)
        ElMessage.success('注册成功')
      }
      handleDialogClose()
      fetchStudents()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该学生?', '提示', { type: 'warning' })
    await deleteStudent(id)
    ElMessage.success('删除成功')
    fetchStudents()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchStudents()
})

onUnmounted(() => {
  stopCamera()
})

const cameraActive = ref(false)
</script>

<style scoped>
.student-view {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.image-input-area {
  width: 100%;
}

.mode-switch {
  margin-bottom: 15px;
}

.upload-area {
  display: flex;
  align-items: center;
}

.camera-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.camera-wrapper {
  width: 320px;
  height: 240px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.camera-wrapper video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hidden-canvas {
  display: none;
}

.camera-controls {
  display: flex;
  align-items: center;
}
</style>