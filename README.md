# 学生就餐统计 APP

一款适配 Android 16 的学生就餐打卡统计应用，采用现代化 Material Design 3 设计。

## 功能特性

### 1. 日报页面
- 📅 按日期查看学生就餐打卡记录
- 🔘 每行显示3名学生，每人有早餐/午餐/晚餐三个标记按钮
- 📊 实时统计已圈/未圈餐数
- 🏷️ 支持「全部/早餐/午餐/晚餐」标签筛选
- ← → 箭头切换日期

### 2. 学生管理
- ➕ 手动添加学生
- 📋 批量导入学生（每行一个姓名）
- ✏️ 修改学生姓名
- 🗑️ 删除学生
- ↕️ 拖拽排序学生列表

### 3. 统计页面
- 📅 按日期范围筛选数据
- 👥 按学生维度统计就餐次数
- 🥐 按餐次维度（早餐/午餐/晚餐）统计
- 📈 简单图表展示统计数据

### 4. 数据导出
- 📄 导出 CSV 格式
- 📊 导出 Excel 格式
- 📁 自定义导出目录
- 📝 文件命名带日期

### 5. 设置页面
- 📂 自定义导出路径
- 💾 数据备份（JSON 格式）
- 🔄 数据恢复
- 🗑️ 清除所有数据

### 6. 技术特点
- 🎨 Material Design 3 设计语言
- 🌓 高对比度配色方案
- 📱 完全适配 Android 16
- 🔒 数据本地存储，不上传服务器
- ⚡ 流畅的 Jetpack Compose UI

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **数据库**: Room
- **依赖注入**: 手工注入
- **Excel导出**: Apache POI
- **CSV导出**: OpenCSV

## 项目结构

```
StudentMealApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/mealapp/
│   │   │   ├── data/
│   │   │   │   ├── dao/          # Room DAO
│   │   │   │   ├── database/     # Room Database
│   │   │   │   ├── entity/       # Room Entity
│   │   │   │   └── repository/   # Repository
│   │   │   ├── domain/
│   │   │   │   ├── model/        # Domain Models
│   │   │   │   └── usecase/      # Use Cases
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Reusable UI Components
│   │   │   │   ├── screens/      # Screen Composables
│   │   │   │   └── theme/        # Theme
│   │   │   └── viewmodel/        # ViewModels
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 编译运行

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 35 (Android 16)

### 编译步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd StudentMealApp
   ```

2. **使用 Android Studio 打开**
   - File → Open → 选择项目根目录
   - 等待 Gradle sync 完成

3. **同步 Gradle**
   - Android Studio 会自动提示同步
   - 或点击 File → Sync Project with Gradle Files

4. **运行应用**
   - 连接 Android 设备（Android 16）或模拟器
   - 点击 Run ▶️ 按钮
   - 或使用快捷键 Shift + F10

5. **构建 APK**
   ```bash
   ./gradlew assembleDebug
   ```
   
   APK 文件位置: `app/build/outputs/apk/debug/app-debug.apk`

## 使用说明

### 首次使用

1. 安装 APK 后打开应用
2. 进入「学生」页面，点击右下角 + 按钮添加学生
3. 或使用「批量导入」功能一次性添加多名学生

### 日常使用

1. 打开应用默认进入「日报」页面
2. 选择日期后，点击学生的早/午/晚按钮标记就餐
3. 已标记的按钮显示红色，可再次点击取消标记
4. 使用顶部筛选标签查看特定餐次

### 数据导出

1. 进入「统计」页面
2. 设置日期范围
3. 点击右上角导出按钮
4. 选择 CSV 或 Excel 格式

## 权限说明

应用需要以下权限：
- `WRITE_EXTERNAL_STORAGE`: 保存导出文件（仅 Android 9 以下）
- `READ_EXTERNAL_STORAGE`: 读取备份文件（仅 Android 12 以下）

Android 10 及以上使用应用私有目录，无需存储权限。

## 隐私声明

本应用：
- ✅ 所有数据存储在本地设备
- ✅ 不收集任何个人信息
- ✅ 不上传任何数据到服务器
- ✅ 不包含任何广告或追踪器

## License

MIT License
