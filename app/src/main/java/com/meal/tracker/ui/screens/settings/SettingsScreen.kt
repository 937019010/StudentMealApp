package com.meal.tracker.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meal.tracker.ui.theme.*
import com.meal.tracker.viewmodel.SettingsResult
import com.meal.tracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val exportPath by viewModel.exportPath.collectAsState()
    val context = LocalContext.current

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showPathEditDialog by remember { mutableStateOf(false) }
    var showPathDetailDialog by remember { mutableStateOf(false) }

    val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.restoreData(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.operationResult.collect { result ->
            val msg = when (result) {
                is SettingsResult.Success -> result.message
                is SettingsResult.Error -> result.message
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingsSection("数据管理") }

            // —— 导出路径：点击行展开「详情 / 复制 / 重置」面板 ——
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("导出路径", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = exportPath,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 2
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showPathDetailDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("查看")
                            }
                            OutlinedButton(
                                onClick = { copyToClipboard(context, exportPath) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("复制")
                            }
                            OutlinedButton(
                                onClick = { showPathEditDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("修改")
                            }
                        }
                    }
                }
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "数据备份",
                    subtitle = "导出学生数据到文件",
                    onClick = { viewModel.backupData() }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Restore,
                    title = "数据恢复",
                    subtitle = "从备份文件恢复数据",
                    onClick = { restoreLauncher.launch("application/json") }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "清除所有数据",
                    subtitle = "删除所有学生和就餐记录",
                    onClick = { showClearConfirmDialog = true },
                    isDestructive = true
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)); SettingsSection("关于") }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    subtitle = "v1.2 (code 3)",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.School,
                    title = "适配版本",
                    subtitle = "Android 16 (API 35)",
                    onClick = { }
                )
            }
        }
    }

    // —— 路径详情弹窗：解释默认路径位置 + 一键复制 ——
    if (showPathDetailDialog) {
        AlertDialog(
            onDismissRequest = { showPathDetailDialog = false },
            title = { Text("导出路径说明") },
            text = {
                Column {
                    Text("当前路径：", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exportPath, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "说明：",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "默认存放在 App 专属外部目录（无需存储权限），" +
                                "路径为 /Android/data/com.meal.tracker/files/exports/。\n" +
                                "卸载 App 后该目录会一并删除。\n" +
                                "如需保存到公共目录（Download/ 文档），请用文件管理器把导出文件移动过去。",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPathDetailDialog = false }) { Text("知道了") }
            }
        )
    }

    // —— 路径修改弹窗：手动输入 + 重置按钮 ——
    if (showPathEditDialog) {
        PathEditDialog(
            currentPath = exportPath,
            onConfirm = { newPath ->
                runCatching { viewModel.setExportPath(newPath) }
                    .onSuccess { Toast.makeText(context, "路径已更新", Toast.LENGTH_SHORT).show() }
                    .onFailure { e -> Toast.makeText(context, e.message ?: "保存失败", Toast.LENGTH_LONG).show() }
                showPathEditDialog = false
            },
            onReset = {
                viewModel.resetExportPath()
                Toast.makeText(context, "已重置为默认路径", Toast.LENGTH_SHORT).show()
                showPathEditDialog = false
            },
            onDismiss = { showPathEditDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("确认清除") },
            text = { Text("确定要清除所有数据吗？此操作不可恢复！") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Primary)
                ) { Text("确定清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun PathEditDialog(
    currentPath: String,
    onConfirm: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentPath) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改导出路径") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("路径") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "提示：填写 App 可访问的目录绝对路径，例如 /sdcard/Download/MealExport。",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("保存") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) { Text("重置默认") }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) Primary else OnSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("export_path", text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}
