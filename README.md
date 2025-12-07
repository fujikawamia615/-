# 🌟 Resource Station (资源站练习项目)
## 📚 个人全栈学习与实践的资源分享平台


---

## ✨ 项目简介 (Project Overview)

本项目是一个基于前后端分离的全栈**资源分享/学习交流平台**的练习项目。旨在通过实战掌握现代 Web 开发技术栈，并提供一个简洁、美观的资源展示与搜索界面。

### 🚀 技术栈 (Tech Stack)

| 模块 | 技术 | 描述 |
| :--- | :--- | :--- |
| **前端 (Frontend)** | **Vue 3** | 核心框架，用于构建响应式用户界面。 |
| | **Naive UI** | 基于 Vue 3 的组件库，提供美观且高性能的 UI 组件。 |
| | **vue-live2d** | 引入 Live2D 模型，增加网站的互动性和趣味性。 |
| **后端 (Backend)** | **Spring Boot** | 用于构建健壮、易于部署的 RESTful API 服务。 |
| **数据 (Database)** | **MySQL** | 存储资源信息、用户数据等。 |

---

## 🛠️ 安装与部署 (Installation & Setup)

本项目包含**前端**和**后端**两个部分，需分别启动。

### 1. 后端 (Spring Boot) 配置

1.  **数据库配置：**
    * 在本地安装mysql，并创建数据库 `resources_db`。
    * 修改 `src/main/resources/application.properties` (或 `.yml`) 中的数据库连接信息：
        ```properties
        # 示例配置
        spring.datasource.url=[您的数据库连接 URL]
        spring.datasource.username=[您的用户名]
        spring.datasource.password=[您的密码]
        ```
2.  **启动服务：**
    * 使用 IDE（如 IntelliJ IDEA）打开项目并运行 `DemoApplication.java`。
    * 或使用 Maven/Gradle 命令打包并运行。

### 2. 前端 (Vue 3) 配置

1.  **进入前端目录并安装依赖：**
    ```bash
    cd frontend  # 假设前端代码在 frontend 文件夹内
    npm install  # 或 yarn install
    ```
2.  **配置 API 地址：**
    * 修改app.vue下的API_BASE属性，指向您的后端服务地址：
        ```bash
        API_BASE=http://localhost:8080/api  # 假设后端运行在 8080 端口
        ```
3.  **启动前端服务：**
    ```bash
    npm run dev  # 或 yarn dev
    ```

---

## 🚀 快速使用 (Quick Start)

项目启动后，您可以通过以下方式访问和使用核心功能：

1.  **访问地址：** 浏览器打开 `http://localhost:5173` (Vue 默认端口)。
2.  **核心功能：**
    * **资源分类浏览：** [说明如何通过 Naive UI 的侧边栏或 Tab 切换分类]
    * **Live2D 互动：** 访问页面右下角，与可爱的 Live2D 角色进行互动。
    * **搜索功能：** [说明如何使用搜索栏根据关键词查找资源]

## 🎯 目标与挑战 (Goals & Challenges)

本项目主要目标是：
* **熟悉 Vue 3 的 Composition API** 和生态工具链。
* **掌握 Naive UI** 组件的高效使用。
* **实践 Spring Boot** 的 RESTful API 设计和数据层交互。

---

## 📄 许可证 (License)

本项目采用 **MIT** 许可证。详情请参阅项目根目录下的 `LICENSE` 文件。

## 📞 联系方式 (Contact)

[您的昵称/姓名] - fujikawamia615
