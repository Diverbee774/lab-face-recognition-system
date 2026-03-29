<template>
  <div class="student-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="openAddDialog">注册学生</el-button>
        </div>
      </template>

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

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑学生' : '注册学生'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item :label="isEdit ? '新人脸' : '照片'" prop="imageBase64">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
          >
            <el-button>{{ isEdit ? '重新上传' : '选择图片' }}</el-button>
            <span v-if="form.imageBase64" style="margin-left: 10px; color: #67c23a">已选择</span>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">{{ isEdit ? '更新' : '注册' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, registerStudent, updateStudent, deleteStudent } from '@/api/student'

const students = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  password: '',
  status: 1,
  imageBase64: ''
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
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
  Object.assign(form, { id: null, studentNo: '', name: '', password: '', status: 1, imageBase64: '' })
  showDialog.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, studentNo: row.studentNo, name: row.name, password: row.password, status: row.status, imageBase64: '' })
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

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!isEdit.value && !form.imageBase64) {
      ElMessage.warning('请选择图片')
      return
    }

    loading.value = true
    try {
      if (isEdit.value) {
        await updateStudent({
          id: form.id,
          name: form.name,
          password: form.password,
          status: form.status
        })
        ElMessage.success('更新成功')
      } else {
        await registerStudent(form)
        ElMessage.success('注册成功')
      }
      showDialog.value = false
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
</script>

<style scoped>
.student-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>