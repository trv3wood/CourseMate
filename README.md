# CourseMate

面向高校教学场景的 Android 客户端。围绕「课程 — 作业 — 讨论」三条主线，把课程信息、作业提交与批阅、帖子问答集中在一个 App 内，并按 `student` / `teacher` / `admin` 三种角色控制可见操作。

后端为独立的 REST 服务（FastAPI 风格，路径见下方「接口一览」），本仓库只包含 Android 客户端。

> 本项目为个人课程作业，用于教学演示与练习，未做生产化考量。

## 功能特性

**通用**

- 登录 / 注册，JWT 持久化在 DataStore，`AuthInterceptor` 自动附带 `Authorization: Bearer <token>`
- 冷启动自动恢复会话：本地有 token 时静默拉取 `users/me`，成功直接进主页，失败回登录页
- 离线优先：列表与详情先落 Room，UI 只订阅 DAO 的 `Flow`，刷新动作走网络后回写本地
- 底部四个 Tab：课程 / 作业 / 讨论 / 个人，统一下拉刷新（`PullRefreshContainer`）
- Markdown 渲染：帖子与作业详情支持 CommonMark 子集（标题、列表、引用、代码块、链接自动识别）

**课程**

- 课程卡片列表、新建 / 编辑 / 删除
- 课程详情聚合该课程的作业与讨论入口，并在详情内直接新建帖子和作业

**作业**

- 作业列表、新建 / 编辑 / 删除（教师、管理员）
- 学生提交作业（正文 + 附件链接）并查看自己的历史提交，教师、管理员查看某次作业的全部提交
- 截止时间紧迫度提示，超期高亮

**讨论**

- 帖子列表、发帖 / 编辑 / 删除，帖子详情含回复列表
- 回复的编辑 / 删除，以及「采纳回复」（作者或教师）
- 权限判定集中在 `utils/RoleUtils.kt`：`canManagePost` / `canAcceptReply` / `canViewHomeworkSubmissions` 等

**个人**

- 当前用户信息与统计（课程数、作业数、讨论数）
- 通知列表与发布通知（教师、管理员）

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / UI | Kotlin 2.0.21、Jetpack Compose（Material 3）、Material Icons Extended |
| 架构 | 单 Activity + Compose 导航状态机、MVVM、Repository |
| 网络 | Retrofit 2.11 + Gson + OkHttp 4.12（含日志拦截器） |
| 本地存储 | Room 2.7.2（KSP 生成代码）、DataStore Preferences 1.1.1 |
| 异步 | Kotlin Coroutines / Flow |
| 构建 | AGP 8.13.2、Gradle 8.13、KSP、Version Catalog（`gradle/libs.versions.toml`） |
| 测试 | JUnit4、Robolectric、MockWebServer、Turbine、Room Testing、Compose UI Test |

依赖注入为手写容器（`di/AppContainer.kt`），由 `CourseMateApplication` 持有，未引入 Hilt。

## 架构

数据自下而上单向流动，UI 层不直接接触网络与数据库：

```
ui/ (Compose)  →  viewmodel/  →  repository/  →  network/ (Retrofit)
                                      ↓
                                  local/ (Room DAO)
```

- **`repository/`** 是唯一的数据出入口。`refreshXxx()` 走网络后 `upsert` 进 Room，`observeXxx()` 暴露 DAO 的 `Flow` 供 UI 订阅，因此写库即刷新界面。
- **`utils/AppResult.kt`** 统一返回类型：`safeCall { }` 把异常收敛为 `AppResult.Error`，避免每个 ViewModel 重复 try/catch。
- **`repository/Mappers.kt`** 承担 DTO（`network/dto`）↔ Entity（`local/entity`）↔ Model（`model`）三层转换，网络字段变更不会泄漏到 UI。
- **`network/retrofit/`** 中的 `RetrofitModule.create(tokenReader)` 组装 OkHttp 与各 `*Api`，`AuthInterceptor` 通过 `TokenReader` 读取 DataStore 中的 token。

## 目录结构

```
app/src/main/java/com/example/coursemate/
├── CourseMateApplication.kt      # 持有 AppContainer
├── MainActivity.kt               # 单 Activity，setContent { CourseMateTheme { CourseMateApp() } }
├── di/AppContainer.kt            # 手写依赖容器
├── datastore/TokenDataStore.kt   # token 持久化
├── local/                        # Room：database / entity / dao
├── model/                        # UI 领域模型
├── network/                      # api（接口）/ dto（传输对象）/ retrofit（OkHttp 组装）
├── repository/                   # 数据仓库 + Mappers
├── ui/
│   ├── app/CourseMateApp.kt      # 登录态分支 + Scaffold + 底部导航
│   ├── common/                   # PullRefreshContainer、Markdown 渲染
│   ├── course/ homework/ discussion/ profile/ login/
│   └── theme/                    # Color / Type / Theme
├── utils/                        # AppResult、RoleUtils、UiFormatters
└── viewmodel/                    # Auth / Course / Homework / Discussion / Notification
```

页面内不再使用 TopAppBar，刷新一律走下拉刷新；课程与讨论详情页的返回入口位于内容区内部。

## 环境要求

- JDK 17（AGP 8.13 要求）
- Android SDK：`compileSdk` / `targetSdk` 36，`minSdk` 30
- Android Studio（建议最新稳定版）或命令行 Gradle

## 快速开始

1. 在项目根目录创建 `local.properties`，指向本机 SDK（该文件已被 git 忽略）：

   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```

2. 配置后端地址。默认写在 `app/build.gradle.kts` 中：

   ```kotlin
   buildConfigField("String", "COURSE_MATE_BASE_URL", "\"http://10.0.2.2:8000/\"")
   ```

   `10.0.2.2` 是 Android 模拟器访问宿主机 `localhost` 的固定地址；若使用真机或远程后端，改成对应 IP / 域名即可。明文 HTTP 已通过 manifest 的 `usesCleartextTraffic="true"` 放行。

3. 编译并安装：

   ```bash
   ./gradlew :app:assembleDebug
   ./gradlew :app:installDebug      # 需要已连接设备或模拟器
   ```

## 构建与测试

```bash
./gradlew :app:assembleDebug        # 构建 debug APK
./gradlew :app:testDebugUnitTest    # 单元测试（JVM / Robolectric）
./gradlew :app:connectedDebugAndroidTest  # 仪器测试（需设备）
./gradlew lint                      # Android Lint
```

单元测试位于 `app/src/test/`，覆盖 DAO、Repository、ViewModel、Retrofit 接口（MockWebServer 驱动，桩数据在 `src/test/resources/json/`）与 Markdown 解析器；仪器测试位于 `app/src/androidTest/`，覆盖登录注册页与主流程。

## 接口一览

Base URL 之下均为相对路径，列表接口统一支持 `skip` / `limit` 分页。

| 模块 | 方法 | 路径 |
| --- | --- | --- |
| 认证 | `POST` | `auth/register`、`auth/login` |
| 用户 | `GET` | `users/me` |
| 课程 | `GET` `POST` | `courses` |
| 课程 | `GET` `PUT` `DELETE` | `courses/{course_id}` |
| 作业 | `GET` `POST` | `homework` |
| 作业 | `GET` `PUT` `DELETE` | `homework/{homework_id}` |
| 作业提交 | `POST` `GET` | `homework/{homework_id}/submissions` |
| 作业提交 | `GET` | `homework/submissions/me` |
| 帖子 | `GET` `POST` | `posts` |
| 帖子 | `GET` `PUT` `DELETE` | `posts/{post_id}` |
| 回复 | `POST` | `posts/{post_id}/reply` |
| 回复 | `PUT` `DELETE` | `reply/{reply_id}` |
| 回复 | `POST` | `reply/{reply_id}/accept` |
| 通知 | `GET` `POST` | `notifications` |

## 设计规范

`DESIGN.md` 是本项目的设计系统事实来源，以 Material Design 3 为基底，定义了完整的 color token、Inter 字体阶梯、8dp 栅格与圆角规范。改 UI 前请先对照该文件；`ui/theme/` 中的 `Color.kt` / `Type.kt` 即由其中的 token 落地而来。

`docs/stitch/` 保留了课程列表、课程详情、登录注册三张早期的 HTML 设计稿与截图，供视觉比对参考，不参与构建。

## 说明

- 应用 ID 与包名仍为脚手架默认的 `com.example.coursemate`。
- 数据库当前 `version = 1` 且 `exportSchema = false`，尚无 Migration；修改 schema 时需自行处理版本升级。
