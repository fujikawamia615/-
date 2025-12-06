<script setup>
import { ref, onMounted, computed ,h} from 'vue';
import axios from 'axios';
import live2d from "vue3-live2d";
import ResourceCard from './components/ResourceCard.vue';
import { NInput, NButton, NForm, NFormItem,NConfigProvider,NAvatar, NDropdown, NModal, NCard, NDataTable, NEmpty } from 'naive-ui';
onMounted(() => {
  document.documentElement.classList.add('loaded');
});
const loginInputOverrides = {
    Input: {   
        heightLarge: '48px', 
        paddingLarge: '0 18px', 
        fontSizeLarge: '16px', 
        borderRadius: '12px', 
        color: 'rgba(255, 255, 255, 0.8)', 
        border: '2px solid #e2e8f0', 
        borderHover: '2px solid #e2e8f0', 
        borderFocus: '2px solid #6a5af9', 
        boxShadowFocus: '0 0 0 3px rgba(106, 90, 249, 0.1)',
        colorFocus: 'white', 
    }
};
const showDropdown = ref(false);
const dropdownOptions = ref([
    {
        label: '更改密码',
        key: 'change-password',
        icon: () => '🔑' 
    },
    {
        label: '下载记录',
        key: 'download-history',
        icon: () => '📜'
    }
]);
const showDocReaderModal = ref(false);    // 控制阅读器模态框显示
const currentDocUrl = ref('');           // 文档的下载 URL
const currentDocName = ref('');          // 文档名称
const currentDocContent = ref('正在加载文档内容...'); // 新增状态：存放获取到的 TXT 内容
const showVideoPlayerModal = ref(false); // 控制播放器模态框显示
const currentVideoUrl = ref('');         // 当前播放视频的URL
const currentVideoName = ref('');
const tips = ref({ visibilitychange: [{ selector: 'document', texts: ['哇，你终于回来了～'] }] });
const searchResults = ref([]);
const isSearching = ref(false);
const showAudioPlayerModal = ref(false); // 控制音乐播放器模态框显示
const currentAudioUrl = ref('');         // 当前播放音乐的 URL
const currentAudioName = ref('');
const username = ref('');
const password = ref('');
const loginError = ref('');
const isLoggedIn = ref(false);
const showRegisterView = ref(false);
const showSearchView = ref(false);
const showRankingView = ref(false);
const searchQuery = ref('');
const width = ref(400);
const height = ref(400);
const currentFilter = ref(null);
const filteredResources = ref([]);
const showAllResources = ref(false);
const resources = ref([]);
const loading = ref(false);
const error = ref(null);
const API_BASE = 'http://39.105.154.74:8080';
const featuredResources = computed(() => {
  if (!resources.value || resources.value.length === 0) return [];
  return [...resources.value]
    .sort((a, b) => (b.times || 0) - (a.times || 0))
    .slice(0, 6);
});

const latestResources = computed(() => {
  if (!resources.value || resources.value.length === 0) return [];
  return [...resources.value]
    .sort((a, b) => {
      const timeA = new Date(a.uploadTime).getTime();
      const timeB = new Date(b.uploadTime).getTime();
      return timeB - timeA;
    })
    .slice(0, 6);
});
const rankingResources = computed(() => {
  return [...resources.value]
    .sort((a, b) => (b.times || 0) - (a.times || 0))
    .slice(0, 25);
});
function playAudio(resource) {
    // 音乐播放使用流媒体接口
    currentAudioUrl.value = getResourceStreamUrl(resource);
    currentAudioName.value = resource.name;
    
    showAudioPlayerModal.value = true;
}
function readDocument(resource) {
    const docUrl = getResourceDownloadUrl(resource);
    
    currentDocUrl.value = docUrl;
    currentDocName.value = resource.name;
    currentDocContent.value = '正在加载文档内容...'; 
    showDocReaderModal.value = true;

    // 1. 使用 fetch 获取原始 Blob 数据
    fetch(docUrl)
        .then(response => {
            if (!response.ok) {
                throw new Error('网络请求失败: ' + response.statusText);
            }
            return response.blob(); // <-- 获取 Blob 原始数据
        })
        .then(blob => {
            // 2. 使用 FileReader API 读取 Blob
            const reader = new FileReader();
            
            // ❗ 核心：指定 GBK 编码进行读取 ❗
            reader.readAsText(blob, 'GBK'); 

            // 3. 监听读取完成事件
            reader.onload = function(event) {
                if (event.target.readyState === FileReader.DONE) {
                    // 成功解码后的文本
                    currentDocContent.value = event.target.result;
                }
            };

            // 4. 监听读取错误事件
            reader.onerror = function() {
                throw new Error('FileReader 读取文件失败');
            };
        })
        .catch(error => {
            console.error("加载文档失败:", error);
            currentDocContent.value = '加载文档内容失败，请检查URL或编码设置是否为GBK。';
        });
}
function getResourceStreamUrl(resource) {
    if (!resource.fileType || !resource.fileKey) return '#';
    const encodedType = encodeURIComponent(resource.fileType);
    const encodedKey = encodeURIComponent(resource.fileKey);
    // ❗ 对应后端新增的 /stream 接口 ❗
    return `${API_BASE}/api/stream/resource/${encodedType}/${encodedKey}`;
}

// -----------------------------------------------------------------
// 新增函数 2: 处理 ResourceCard 发出的 'play' 事件
// -----------------------------------------------------------------
function playVideo(resource) {
    // 1. 获取流媒体 URL
    currentVideoUrl.value = getResourceStreamUrl(resource);
    currentVideoName.value = resource.name;
    
    // 2. 显示播放器模态框
    showVideoPlayerModal.value = true;
}
function handleDropdownSelect(key) {
    showDropdown.value = false; // 关闭菜单
    switch (key) {
        case 'change-password':
            alert('功能待实现：跳转到更改密码页面或弹出模态框。');
            // 实际操作：showModal.value = true
            break;
        case 'download-history':
            alert('功能待实现：显示下载记录列表。');
            // 实际操作：showDownloadHistoryView.value = true
            break;
        case 'logout':
            logout(); // 调用您已有的退出登录函数
            break;
    }
}
async function handleRegister() {
    loginError.value = '';
    if (!username.value || !password.value) {
        loginError.value = '用户名和密码不能为空！';
        return;
    }
    try {
        const response = await axios.post(`${API_BASE}/api/register`, {
            username: username.value,
            password: password.value 
        });
        alert('注册成功！请使用您的新账户登录。');
        showRegisterView.value = false;
        loginError.value = ''; 
        password.value = '';
        
    } catch (err) {
        console.error('注册失败:', err);
        if (err.response && err.response.data) {
            loginError.value = err.response.data;
        } else {
            loginError.value = '注册失败，请稍后再试。';
        }
    }
}
function toggleView() {
    showRegisterView.value = !showRegisterView.value;
    username.value = ''; 
    password.value = '';
    loginError.value = '';
}
async function handleLogin() {
    loginError.value = '';

    if (!username.value || !password.value) {
        loginError.value = '用户名和密码不能为空！';
        return;
    }
    

    if (showRegisterView.value) return; 

    try {
      
        const response = await axios.post(`${API_BASE}/api/login`, {
            username: username.value,
            password: password.value 
        });

 
        if (response.status === 200) {
            loginError.value = '';
   
            isLoggedIn.value = true;
   
            fetchResources();
        } 
    } catch (err) {
        console.error('登录失败:', err);
        if (err.response && err.response.data) {
            loginError.value = err.response.data;
        } else {
            loginError.value = '登录失败，无法连接到服务器。';
        }
    }
}
function logout() {
  isLoggedIn.value = false;
  username.value = '';
  password.value = '';
  resources.value = [];
  showSearchView.value = false;
  currentFilter.value = null;
  showAllResources.value = false;
  showRankingView.value = false;
}

async function fetchResources() {
  loading.value = true;
  error.value = null;
  try {
    const response = await axios.get(`${API_BASE}/api/resources`);
    resources.value = response.data;
  } catch (err) {
    console.error('请求失败:', err);
    error.value = '无法连接到后端，请检查网络或服务器是否运行。';
  } finally {
    loading.value = false;
  }
}

function getCoverUrl(coverName) {
  return `${API_BASE}/api/download/cover/${encodeURIComponent(coverName)}`;
}

function getResourceDownloadUrl(resource) {
  if (!resource.fileType || !resource.fileKey) return '#';
  return `${API_BASE}/api/download/resource/${encodeURIComponent(resource.fileType)}/${encodeURIComponent(resource.fileKey)}`;
}

function handleImageError(e) {
  e.target.style.display = 'none';
}

function formatSize(bytes) {
  if (!bytes || bytes <= 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

async function performSearch() {
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    isSearching.value = false;
    return;
  }

  isSearching.value = true;
  try {
    const allResources = resources.value;
    searchResults.value = allResources.filter(resource =>
      resource.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (resource.description && resource.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
    );
  } catch (err) {
    console.error('搜索失败:', err);
    searchResults.value = [];
  } finally {
    isSearching.value = false;
  }
}

function showRanking() {
  showRankingView.value = true;
  if (resources.value.length === 0) fetchResources();
}
function toggleSearchView() {
  showSearchView.value = !showSearchView.value;
  if (!showSearchView.value) {
    searchQuery.value = '';
    searchResults.value = [];
  }
}

function closeSearchView() {
  showSearchView.value = false;
  searchQuery.value = '';
  searchResults.value = [];
}


function showFiltered(type) {
  currentFilter.value = type;
  showAllResources.value = false;
  filteredResources.value = resources.value.filter(r => r.fileType === type);
}

function clearFilter() {
  currentFilter.value = null;
  showAllResources.value = false;
}

function viewAllResources() {
  showAllResources.value = true;
  currentFilter.value = '全部';
  filteredResources.value = resources.value;
}

</script>

<template>
  <live2d class="live2d" v-model:width="width" v-model:height="height" v-model:resolution="resolution"
    homePage="https://github.com/fujikawamia615/frontend"
    :style="{ position: 'fixed', bottom: 0, right: 0, zIndex: 2999 }" api-path="./live2d-static-api/indexes"
    :model="['hk416_3401/hk416_3401', 'default']" :tips="tips" />
  <div v-if="!isLoggedIn" class="login-wrapper">
    <div class="login-box">
        <div class="login-header">
            <h1 class="title">✨ 319资源站</h1>
        </div>
        <n-config-provider :theme-overrides="loginInputOverrides">
        <n-form @submit.prevent="showRegisterView ? handleRegister() : handleLogin()" class="login-form">
            <n-form-item :show-feedback="false">
                <n-input
                    v-model:value="username"
                    round
                    type="text"
                    placeholder="用户名"
                    size="large"
                    clearable
                    :input-props="{ required: true }"
                />
            </n-form-item>
            <n-form-item :show-feedback="false" style="margin-bottom: 24px;">
                <n-input
                    round
                    v-model:value="password"
                    type="password"
                    placeholder="密码"
                    size="large"
                    show-password-on="click"
                    clearable
                    :input-props="{ required: true }"
                    @keyup.enter="showRegisterView ? handleRegister() : handleLogin()"
                />
            </n-form-item>
            <button type="submit" class="login-btn">
                {{ showRegisterView ? '注册' : '登录' }}
            </button>
            <p v-if="loginError" class="error">{{ loginError }}</p>
            <button type="button" class="switch-btn" @click="toggleView">
                {{ showRegisterView ? '已有账号？去登录' : '没有账号？去注册' }}
            </button>
        </n-form>
        </n-config-provider>
    </div>
</div>
  <div v-else class="main-layout">
    <div v-if="showSearchView" class="search-view">
      <header class="search-header">
        <div class="search-header-content">
          <button @click="closeSearchView" class="back-btn">←</button>
          <div class="search-bar">
            <input v-model="searchQuery" @input="performSearch" cursor: text type="text" placeholder="搜索资源..." class="search-input"
              autofocus />
            <button v-if="searchQuery" @click="searchQuery = ''; searchResults = []" class="clear-btn">×</button>
          </div>
        </div>
      </header>
      <div class="search-results">
        <div v-if="isSearching" class="loading-state">
          <div class="loading-spinner"></div>
          <p>搜索中...</p>
        </div>
        <div v-else-if="searchResults.length > 0" class="resource-grid">
          <ResourceCard v-for="resource in searchResults" :key="resource.id" :resource="resource"
            :getCoverUrl="getCoverUrl" :getResourceDownloadUrl="getResourceDownloadUrl" :formatSize="formatSize"
            :handleImageError="handleImageError" @play="playVideo" @read="readDocument" @audio="playAudio"/>
        </div>
        <div v-else-if="searchQuery && !isSearching" class="no-results">
          <p>没有找到相关资源</p>
        </div>
        <div v-else class="search-suggestions">
          <h3 class="suggestions-title">热门搜索</h3>
          <div class="suggestion-tags">
            <span class="suggestion-tag" @click="searchQuery = '乐园杂音'; performSearch()">乐园杂音</span>
            <span class="suggestion-tag" @click="searchQuery = 'vscode'; performSearch()">vscode</span>
            <span class="suggestion-tag" @click="searchQuery = 'mysql'; performSearch()">mysql</span>
            <span class="suggestion-tag" @click="searchQuery = 'node'; performSearch()">node</span>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="showRankingView" class="ranking-view">
      <header class="ranking-header">
        <div class="ranking-header-content">
          <button @click="showRankingView = false" class="back-btn">← 返回</button>
          <h2>🔥 下载排行榜</h2>
        </div>
      </header>

      <div class="ranking-list-container">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载排行榜...</p>
        </div>

        <div v-else-if="rankingResources.length === 0" class="no-ranking-data">
          暂无资源数据
        </div>

        <ul v-else class="ranking-list">
          <li v-for="(resource, index) in rankingResources" :key="resource.id" class="ranking-item">
            <span class="rank">{{ index + 1 }}</span>
            <span class="resource-name">{{ resource.name }}</span>
            <span class="download-count">📥 {{ resource.times || 0 }}</span>
          </li>
        </ul>
      </div>
    </div>
    <div v-else-if="currentFilter" class="filter-view">
      <header class="filter-header">
        <div class="filter-header-content">
          <button @click="clearFilter" class="back-btn">← 返回</button>
          <h2>{{ currentFilter }} 资源</h2>
        </div>
      </header>
      <div class="filter-results">
        <div class="resource-grid">
          <ResourceCard v-for="resource in filteredResources" :key="resource.id" :resource="resource"
            :getCoverUrl="getCoverUrl" :getResourceDownloadUrl="getResourceDownloadUrl" :formatSize="formatSize"
            :handleImageError="handleImageError" @play="playVideo" @read="readDocument" @audio="playAudio"/>
        </div>
      </div>
    </div>

    <div v-else class="normal-view">

      <header class="site-header">
        <div class="header-content">
          <div class="logo-area">
            <h1>✨ 319资源站</h1>
          </div>
          <nav class="main-nav">
            <a href="#" class="nav-link" @click.prevent="showFiltered('文档')">文档</a>
            <a href="#" class="nav-link" @click.prevent="showFiltered('音乐')">音乐</a>
            <a href="#" class="nav-link" @click.prevent="showFiltered('视频')">视频</a>
            <a href="#" class="nav-link" @click.prevent="showFiltered('应用')">应用</a>
            <a href="#" class="nav-link" @click.prevent="showRanking">排行榜</a>
            <a href="#" class="nav-link" @click.prevent="">社区</a>
          </nav>
          <div class="header-actions">
            <button @click="toggleSearchView" class="search-icon">🔍</button>
            <n-dropdown 
        trigger="click" 
        :options="dropdownOptions" 
        @select="handleDropdownSelect"
        :show="showDropdown"
        @update:show="showDropdown = $event">
        <n-avatar 
            size="small" 
            style="margin-right: 10px; margin-left: 5px; cursor: pointer;" 
            src="./touxiang.jpg"
            @click="showDropdown = !showDropdown"/>
          </n-dropdown>
            <button @click="logout" class="logout-btn">退出登录</button>
          </div>
        </div>
      </header>

      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门推荐</h2>
        </div>
        <div class="resource-grid">
          <div v-if="loading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>正在加载资源...</p>
          </div>
          <div v-else-if="error" class="error-state">
            <p class="error-text">{{ error }}</p>
            <button @click="fetchResources" class="retry-btn">重新加载</button>
          </div>
          <ResourceCard v-for="resource in featuredResources" :key="resource.id" :resource="resource"
            :getCoverUrl="getCoverUrl" :getResourceDownloadUrl="getResourceDownloadUrl" :formatSize="formatSize"
            :handleImageError="handleImageError" @play="playVideo" @read="readDocument" @audio="playAudio"/>
        </div>

        <div class="section-header">
          <h2 class="section-title">最新资源</h2>
          <a href="#" class="view-more" @click.prevent="viewAllResources">查看全部</a>
        </div>
        <div class="resource-grid">
          <ResourceCard v-for="resource in latestResources" :key="resource.id" :resource="resource"
            :getCoverUrl="getCoverUrl" :getResourceDownloadUrl="getResourceDownloadUrl" :formatSize="formatSize"
            :handleImageError="handleImageError" @play="playVideo" @read="readDocument" @audio="playAudio"/>
        </div>
      </div>
    </div>
  </div>
  <n-modal v-model:show="showVideoPlayerModal" preset="card" :mask-closable="false" :style="{ width: '90%', maxWidth: '1000px' }">
    <template #header>
        <h2>▶️ 正在播放：{{ currentVideoName }}</h2>
    </template>
    
    <div class="video-player-container">
        <video 
            v-if="showVideoPlayerModal" 
            :src="currentVideoUrl" 
            controls 
            autoplay 
            class="video-element"
            disablePictureInPicture 
            controlsList="nodownload" 
        >
            抱歉，您的浏览器不支持此视频格式。
        </video>
        <n-empty v-else description="视频播放器已卸载"> </n-empty>
    </div>
    
    <template #footer>
        <n-button @click="showVideoPlayerModal = false">关闭播放器</n-button>
    </template>
</n-modal>
<n-modal v-model:show="showDocReaderModal" preset="card" :style="{ width: '90%', maxWidth: '900px', height: '90vh' }">
    <template #header>
        <h2>📖 阅读：{{ currentDocName }}</h2>
    </template>
    
    <div class="doc-reader-container">
        <pre class="txt-content">{{ currentDocContent }}</pre>
    </div>
    
    <template #footer>
        <n-button @click="showDocReaderModal = false">关闭阅读器</n-button>
    </template>
</n-modal>
<n-modal v-model:show="showAudioPlayerModal" preset="card" :style="{ width: '90%', maxWidth: '600px' }">
    <template #header>
        <h2>🎧 正在播放：{{ currentAudioName }}</h2>
    </template>
    
    <div class="audio-player-container">
        <audio 
            v-if="showAudioPlayerModal" 
            :src="currentAudioUrl" 
            controls 
            autoplay 
            class="audio-element"
        >
            抱歉，您的浏览器不支持此音频格式或加载失败。
        </audio>
    </div>
    
    <template #footer>
        <n-button @click="showAudioPlayerModal = false">关闭播放器</n-button>
    </template>
</n-modal>
</template>
<style>
html,
body {
  overflow: hidden;
  height: 100%;
  margin: 0;
  padding: 0;
  -webkit-user-select: none; /* 针对 WebKit 内核浏览器 (Chrome, Safari) */
    -moz-user-select: none;    /* 针对 Firefox */
    -ms-user-select: none;     /* 针对 IE/Edge */
    user-select: none;
}
</style>
<style scoped>


* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  min-height: 100vh;
  width: 100vw;
  background:
    radial-gradient(circle at 10% 20%, rgba(106, 90, 249, 0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(255, 107, 203, 0.1) 0%, transparent 20%),
    linear-gradient(135deg, #f8f9ff 0%, #fefaff 100%);
  font-family: 'Microsoft YaHei', 'PingFang SC', -apple-system, BlinkMacSystemFont, sans-serif;
  color: #333;
  line-height: 1.6;
  transition: all 0.3s ease;
}

html.loaded {
  background:
    radial-gradient(circle at 10% 20%, rgba(106, 90, 249, 0.15) 0%, transparent 25%),
    radial-gradient(circle at 90% 80%, rgba(255, 107, 203, 0.15) 0%, transparent 25%),
    linear-gradient(135deg, #f8f9ff 0%, #fefaff 100%);
}

body {
  margin: 0;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  background: transparent;
}

.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: transparent;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(255, 255, 255, 0.2);
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header .title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 30px;
  background: linear-gradient(90deg, #6a5af9, #ff6bcb);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: -0.5px;
}

.login-form ::v-deep(.n-input__input-el),
.login-form ::v-deep(.n-input__placeholder) {
    text-align: left !important;
}
.login-form ::v-deep(.n-input) {
    text-align: left !important;
}
.login-form .login-btn {
  width: 40%;
  padding: 14px;
  background: linear-gradient(90deg, #6a5af9, #8a7bff);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin:14px 5%;
}

.login-form .login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(106, 90, 249, 0.3);
}

.login-form .error {
  color: #ef4444;
  font-size: 14px;
  margin-top: 12px;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 6px;
  border: 1px solid rgba(239, 68, 68, 0.2);
}


.main-layout {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.site-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.header-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 70px;
}

.logo-area h1 {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(90deg, #6a5af9, #ff6bcb);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.main-nav {
  display: flex;
  gap: 24px;
  margin-left: 40px;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
  padding: 8px 12px;
  border-radius: 6px;
}

.nav-link:hover {
  color: #6a5af9;
  background: rgba(106, 90, 249, 0.1);
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
}

/* 新增的切换按钮样式 */
.switch-btn {
    width: 90%;
    padding: 10px;
    margin: 10px 5%;
    background: none;
    color: #6a5af9;
    border: 1px solid #6a5af9;
    border-radius: 12px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    /* 关键修改：移除焦点边框 */
    outline: none; 
}

.switch-btn:hover {
    background: rgba(106, 90, 249, 0.1);
}
.logout-btn {
  background: linear-gradient(90deg, #6a5af9, #8a7bff);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(106, 90, 249, 0.3);
}


.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(90deg, #6a5af9, #ff6bcb);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.view-more {
  color: #6a5af9;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.view-more:hover {
  color: #8a7bff;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}


.ranking-view {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.ranking-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.ranking-header-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 70px;
  gap: 12px;
  padding: 0 24px;
}

.ranking-list-container {
  flex: 1;
  overflow-y: auto;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.ranking-list {
  list-style: none;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: white;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f2ff;
  transition: background 0.2s ease;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  background: #f9faff;
}

.rank {
  width: 40px;
  font-weight: bold;
  color: #6a5af9;
  font-size: 18px;
  text-align: center;
}

.resource-name {
  flex: 1;
  font-size: 16px;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-ranking-data,
.loading-state {
  text-align: center;
  padding: 60px 0;
  color: #888;
}

/* ===== 搜索视图 ===== */
.search-view,
.normal-view,
.filter-view {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.search-header,
.filter-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  width: 100%;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.search-header-content,
.filter-header-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 70px;
  gap: 12px;
  padding: 0 24px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  outline: none;
  /* 添加此行以移除焦点时的黑色边框 */
}

.back-btn:hover {
  background: rgba(106, 90, 249, 0.1);
  color: #6a5af9;
}

/* 可选：为了保持一致性，也可以给 .search-icon 添加 outline: none */
.search-icon {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  outline: none;
  /* 添加此行 */
}

.search-icon:hover {
  background: rgba(106, 90, 249, 0.1);
  color: #6a5af9;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 0 16px;
  border: 1px solid #e2e8f0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  font-size: 16px;
  outline: none;
}

.clear-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  border-radius: 50%;
  padding: 0;
}

.clear-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.search-results,
.container,
.filter-results {
  flex: 1;
  overflow-y: auto;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.suggestions-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #222;
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.suggestion-tag {
  padding: 8px 16px;
  background: #e2e8f0;
  color: #4a5568;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.suggestion-tag:hover {
  background: #cbd5e0;
  color: #2d3748;
}

.no-results,
.loading-state {
  text-align: center;
  padding: 60px 0;
}

.download-count {
  color: #6a5af9;
  font-weight: 500;
  font-size: 12px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #6a5af9;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.error-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
}

.error-text {
  color: #ef4444;
  margin-bottom: 16px;
}

.retry-btn {
  background: linear-gradient(90deg, #6a5af9, #8a7bff);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
}

.retry-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(106, 90, 249, 0.3);
}

@media (max-width: 768px) {

  .header-content,
  .container,
  .search-header-content,
  .filter-header-content,
  .search-results,
  .filter-results {
    max-width: none;
    margin: 0 auto;
    padding: 0 16px;
  }

  .live2d {
    display: none;
  }

  .header-content {
    height: auto;
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    padding: 12px 16px;
  }

  .main-nav {
    margin-left: 0;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .header-actions {
    margin-left: 0;
    width: 100%;
    justify-content: space-between;
    gap: 8px;
  }

  .resource-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
.video-player-container {
    width: 100%;
    /* 16:9 宽高比，确保播放器不会太高或太扁 */
    aspect-ratio: 16 / 9; 
    background: black;
    display: flex;
    justify-content: center;
    align-items: center;
}

.video-element {
    width: 100%;
    height: 100%;
    display: block;
}

/* 确保 n-modal 的内容区域没有不必要的 padding */
.n-modal.n-card .n-card__content {
    padding: 0;
}
.doc-reader-container {
    padding: 20px;
    background: #f8f8f8;
    /* 计算高度，确保滚动条只在内容区域出现 */
    height: calc(90vh - 140px); 
    overflow-y: auto; /* 允许垂直滚动 */
}

.txt-content {
    white-space: pre-wrap; /* 关键：保留空格和换行，但允许长行自动换行 */
    word-wrap: break-word;
    font-family: monospace; /* 等宽字体更适合阅读代码或纯文本 */
    font-size: 14px;
    line-height: 1.6;
    color: #333;
    margin: 0;
}
.audio-player-container {
    padding: 20px 0;
    text-align: center;
}
.audio-element {
    width: 100%; /* 填满模态框宽度 */
}
</style>