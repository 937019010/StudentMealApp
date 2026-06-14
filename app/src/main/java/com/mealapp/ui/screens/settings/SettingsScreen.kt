package com.mealapp.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import com.mealapp.ui.theme.*
import com.mealapp.viewmodel.SettingsResult
import com.mealapp.viewmodel.SettingsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val exportPath by viewModel.exportPath.collectAsState()
    val context = LocalContext.current
    
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    
    // SAF file picker for selecting export directory
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Get real path from URI
            val path = getPathFromUri(context, it)
            if (path != null) {
                viewModel.setExportPath(path)
            } else {
                // For Android 11+, use persistent permission
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
                viewModel.setExportPath(it.toString())
            }
        }
    }
    
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.restoreData(it) }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }
    
    LaunchedEffect(Unit) {
        viewModel.operationResult.collect { result ->
            when (result) {
                is SettingsResult.Success -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                is SettingsResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
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
            item {
                SettingsSection(title = "数据管理")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FolderOpen,
                    title = "导出路径",
                    subtitle = exportPath,
                    onClick = { 
                        // Open folder picker
                        folderPickerLauncher.launch(null)
                    }
                )
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
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "关于")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    subtitle = "v1.0.0",
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
                ) {
                    Text("确定清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
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
                tint = if (isDestructive) Primary else Primary,
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
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

// Helper function to convert URI to real path
private fun getPathFromUri(context: android.content.Context, uri: Uri): String? {
    // For primary external storage
    val docId = DocumentsContract.getTreeDocumentId(uri)
    val split = docId.split(":")
    val type = split[0]
    
    if ("primary".equals(type, ignoreCase = true)) {
        return Environment.getExternalStorageDirectory().absolutePath + "/" + (split.getOrNull(1) ?: "")
    }
    
    return null
}
