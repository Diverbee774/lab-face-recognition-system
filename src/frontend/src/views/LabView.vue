<template>
  <div class="lab-view">
    <div class="page-header">
      <h2>实验室管理</h2>
      <el-button type="primary" @click="openAddDialog">添加实验室</el-button>
    </div>

    <el-card class="table-card">
      <div class="search-bar">
        <el-input v-model="search" placeholder="搜索名称或位置" style="width: 200px" clearable @change="fetchLabs" />
      </div>
      <el-table :data="labs" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="accessMode" label="模式" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.accessMode === 1" type="success" size="small">白名单</el-tag>
            <el-tag v-else-if="row.accessMode === 2" type="primary" size="small">开放</el-tag>
            <el-tag v-else-if="row.accessMode === 3" type="danger" size="small">维护中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openWhiteDialog(row)">白名单</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[2, 10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchLabs"
        @current-change="fetchLabs"
        style="margin-top: 20px"
      />
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑实验室' : '添加实验室'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="模式" prop="accessMode">
          <el-select v-model="form.accessMode">
            <el-option :value="1" label="白名单模式" />
            <el-option :value="2" label="开放模式" />
            <el-option :value="3" label="维护中" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">{{ isEdit ? '更新' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showWhiteDialog" :title="`白名单管理 - ${currentLab?.name}`" width="700px" destroy-on-close>
      <div class="white-dialog-content">
        <div class="white-list">
          <h4>白名单学生</h4>
          <el-table :data="whiteStudents" border stripe max-height="300" size="small">
            <el-table-column prop="studentNo" label="学号" />
            <el-table-column prop="name" label="姓名" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="removeFromWhite(row.id)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="add-student">
          <h4>添加学生</h4>
          <el-select v-model="selectedStudentId" placeholder="选择学生" filterable style="width: 100%; margin-bottom: 10px">
            <el-option
              v-for="s in allStudents"
              :key="s.id"
              :label="`${s.studentNo || ''} - ${s.name}`"
              :value="s.id"
            />
          </el-select>
          <el-button type="primary" @click="addToWhite" :disabled="!selectedStudentId">添加</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLabList, addLab, updateLab, deleteLab, getLabStudents, addStudentToLab, removeStudentFromLab, getAllStudents } from '@/api/lab'

const labs = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref()
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const search = ref('')

const showWhiteDialog = ref(false)
const currentLab = ref(null)
const whiteStudents = ref([])
const allStudents = ref([])
const selectedStudentId = ref(null)

const form = reactive({
  id: null,
  name: '',
  location: '',
  accessMode: 1
})

const rules = {
  name: [{ required: true, message: '请输入实验室名称', trigger: 'blur' }]
}

async function fetchLabs() {
  try {
    loading.value = true
    const res = await getLabList({ page: page.value, pageSize: pageSize.value, search: search.value })
    labs.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取实验室列表失败')
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', location: '', accessMode: 1 })
  showDialog.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, location: row.location, accessMode: row.accessMode })
  showDialog.value = true
}

async function openWhiteDialog(row) {
  currentLab.value = row
  selectedStudentId.value = null
  try {
    const [wlRes, allRes] = await Promise.all([getLabStudents(row.id), getAllStudents()])
    whiteStudents.value = wlRes.data || []
    const whiteIds = new Set(whiteStudents.value.map(s => s.id))
    allStudents.value = (allRes.data || []).filter(s => !whiteIds.has(s.id))
    showWhiteDialog.value = true
  } catch (e) {
    ElMessage.error('获取白名单失败')
  }
}

async function addToWhite() {
  if (!selectedStudentId.value) return
  try {
    await addStudentToLab(currentLab.value.id, selectedStudentId.value)
    ElMessage.success('添加成功')
    const wlRes = await getLabStudents(currentLab.value.id)
    whiteStudents.value = wlRes.data || []
    const whiteIds = new Set(whiteStudents.value.map(s => s.id))
    allStudents.value = allStudents.value.filter(s => s.id !== selectedStudentId.value)
    selectedStudentId.value = null
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

async function removeFromWhite(studentId) {
  try {
    await removeStudentFromLab(currentLab.value.id, studentId)
    ElMessage.success('移除成功')
    const res = await getLabStudents(currentLab.value.id)
    whiteStudents.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '移除失败')
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value) {
        await updateLab(form)
        ElMessage.success('更新成功')
      } else {
        await addLab(form)
        ElMessage.success('添加成功')
      }
      showDialog.value = false
      fetchLabs()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该实验室?', '提示', { type: 'warning' })
    await deleteLab(id)
    ElMessage.success('删除成功')
    fetchLabs()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchLabs()
})
</script>

<style scoped>
.lab-view {
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

.search-bar {
  margin-bottom: 15px;
}

.white-dialog-content {
  display: flex;
  gap: 20px;
}

.white-list {
  flex: 1;
}

.white-list h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.add-student {
  width: 220px;
  border-left: 1px solid #eee;
  padding-left: 20px;
}

.add-student h4 {
  margin: 0 0 10px 0;
  color: #333;
}
</style>