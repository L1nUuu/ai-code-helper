<template>
  <div class="rag-compare">
    <div class="toolbar">
      <label class="toggle">
        <input type="checkbox" v-model="useRagDefault" />
        <span>默认开启知识库增强</span>
      </label>
      <input type="file" ref="fileInput" accept=".md,.txt" class="hidden" @change="onFileSelected" />
      <button class="btn" :disabled="uploading" @click="triggerFileSelect">{{ uploading ? '上传中...' : '上传文档' }}</button>
      <span v-if="uploadMsg" class="msg">{{ uploadMsg }}</span>
      <button class="btn" @click="refreshDocList" style="margin-left:8px;">刷新文档列表</button>
      <button class="btn" @click="clearDocStore" style="margin-left:4px;background:#dc3545;">清空知识库</button>
    </div>

    <div class="doc-box">
      <div class="doc-title">当前知识库文档（{{ docList.length }}）</div>
      <ul class="doc-list">
        <li v-for="n in docList" :key="n">{{ n }}</li>
        <li v-if="docList.length===0" style="color:#666;">暂无文档，请上传</li>
      </ul>
    </div>

    <div class="prompt-box">
      <div class="prompt-title">系统提示词（用于对比演示）</div>
      <textarea v-model="sysPrompt" rows="4" class="prompt-input"></textarea>
      <div class="tip">提示词约束模型仅基于上下文回答；关闭增强时无上下文，将输出“无相关信息”。</div>
    </div>

    <div class="question-box">
      <textarea v-model="question" rows="4" class="question-input" placeholder="输入要提的问题"></textarea>
      <div class="actions">
        <button class="btn primary" @click="runCompare">对比回答</button>
      </div>
    </div>

    <div class="result-grid">
      <div class="result-col">
        <div class="col-title">不使用 RAG（纯模型）</div>
        <div class="result" v-html="noRagHtml"></div>
      </div>
      <div class="result-col">
        <div class="col-title">使用 RAG（检索增强）</div>
        <div class="result" v-html="ragHtml"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { marked } from 'marked'

const API_BASE = 'http://localhost:8081/api'

const memoryId = ref(null)
const useRagDefault = ref(true)
const uploading = ref(false)
const uploadMsg = ref('')
const question = ref('请基于我上传的文档回答一个问题')
const sysPrompt = ref(
  '你是一个严格基于“提供的上下文”回答的机器人。不得使用外部知识；若没有上下文则回答“无相关信息”。回答尽量引用上下文的原文并标注来源（如文件名）。'
)

const ragHtml = ref('')
const noRagHtml = ref('')

const fileInput = ref(null)
const docList = ref([])

function generateMemoryId () {
  return Math.floor(Math.random() * 1000000)
}

function triggerFileSelect () {
  fileInput.value?.click()
}

function onFileSelected (e) {
  const f = e.target.files?.[0]
  if (!f) return
  uploadDocument(f)
}

async function uploadDocument (file) {
  uploading.value = true
  uploadMsg.value = ''
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch(`${API_BASE}/doc/upload`, { method: 'POST', body: fd })
    if (!res.ok) throw new Error('上传失败')
    uploadMsg.value = '上传成功，文档已纳入知识库'
    await refreshDocList()
  } catch (e) {
    uploadMsg.value = '上传失败，请重试'
    console.error(e)
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function refreshDocList () {
  try {
    const res = await fetch(`${API_BASE}/doc/list`)
    if (!res.ok) throw new Error('获取失败')
    const arr = await res.json()
    docList.value = Array.isArray(arr) ? arr : []
  } catch (e) {
    console.error(e)
  }
}

async function clearDocStore () {
  try {
    const res = await fetch(`${API_BASE}/doc/clear`, { method: 'POST' })
    if (!res.ok) throw new Error('清空失败')
    docList.value = []
  } catch (e) {
    console.error(e)
  }
}

function openSSE ({ memoryId, message, useRag, prompt, onChunk, onEnd }) {
  const params = new URLSearchParams({
    memoryId: String(memoryId),
    message: message,
    useRag: useRag ? 'true' : 'false',
    prompt: prompt
  })
  const es = new EventSource(`${API_BASE}/ai/chat-alt?${params}`)
  es.onmessage = (ev) => onChunk?.(ev.data)
  es.onerror = () => { es.close(); onEnd?.() }
  es.onopen = () => {}
  es.onclose = () => onEnd?.()
  return es
}

function runCompare () {
  ragHtml.value = ''
  noRagHtml.value = ''

  const q = question.value.trim()
  if (!q) return

  const id = memoryId.value
  const prompt = sysPrompt.value

  // 不使用 RAG
  const esNo = openSSE({
    memoryId: id,
    message: q,
    useRag: false,
    prompt,
    onChunk: (txt) => {
      noRagHtml.value += marked.parse(txt)
    },
    onEnd: () => {}
  })

  // 使用 RAG
  const esRag = openSSE({
    memoryId: id + 1, // 分离会话
    message: q,
    useRag: true,
    prompt,
    onChunk: (txt) => {
      ragHtml.value += marked.parse(txt)
    },
    onEnd: () => {}
  })
}

onMounted(() => {
  memoryId.value = generateMemoryId()
  refreshDocList()
})
</script>

<style scoped>
.rag-compare { padding: 16px; }
.toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
.toggle { display: flex; gap: 8px; align-items: center; }
.hidden { display: none; }
.btn { padding: 6px 12px; border: none; background: #007bff; color: #fff; border-radius: 6px; cursor: pointer; }
.btn.primary { background: #28a745; }
.msg { color: #28a745; }
.doc-box { background: #fff; border: 1px solid #e1e5e9; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.doc-title { font-weight: bold; margin-bottom: 8px; }
.doc-list { margin: 0; padding-left: 20px; }
.prompt-box { background: #fff; border: 1px solid #e1e5e9; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.prompt-title { font-weight: bold; margin-bottom: 8px; }
.prompt-input { width: 100%; border: 1px solid #ddd; border-radius: 6px; padding: 8px; }
.tip { color: #666; font-size: 12px; margin-top: 6px; }
.question-box { background: #fff; border: 1px solid #e1e5e9; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.question-input { width: 100%; border: 1px solid #ddd; border-radius: 6px; padding: 8px; }
.actions { margin-top: 10px; }
.result-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.result-col { background: #fff; border: 1px solid #e1e5e9; border-radius: 8px; padding: 12px; }
.col-title { font-weight: bold; margin-bottom: 8px; }
.result { font-size: 14px; line-height: 1.5; }
</style>
