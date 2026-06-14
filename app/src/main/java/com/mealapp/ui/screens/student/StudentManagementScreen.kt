package com.mealapp.ui.screens.student

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.mealapp.domain.model.Student
import com.mealapp.ui.theme.*
import com.mealapp.viewmodel.OperationResult
import com.mealapp.viewmodel.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    viewModel: StudentViewModel = viewModel()
) {
    val students by viewModel.students.collectAsState()
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showBatchImportDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.operationResult.collect { result ->
            when (result) {
                is OperationResult.Success -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                is OperationResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学生管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showBatchImportDialog = true },
                    containerColor = Secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = "批量导入", tint = Color.White)
                }
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加学生", tint = Color.White)
                }
            }
        },
        containerColor = Background
    ) { padding ->
        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无学生，点击下方按钮添加", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(students) { index, student ->
                    StudentItem(
                        index = index + 1,
                        student = student,
                        onEdit = { editingStudent = student },
                        onDelete = { viewModel.deleteStudent(student.id) }
                    )
                }
            }
        }
    }
    
    // Add dialog
    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addStudent(name)
                showAddDialog = false
            }
        )
    }
    
    // Batch import dialog
    if (showBatchImportDialog) {
        BatchImportDialog(
            onDismiss = { showBatchImportDialog = false },
            onConfirm = { names ->
                viewModel.addStudents(names)
                showBatchImportDialog = false
            }
        )
    }
    
    // Edit dialog
    editingStudent?.let { student ->
        EditStudentDialog(
            student = student,
            onDismiss = { editingStudent = null },
            onConfirm = { newName ->
                viewModel.updateStudent(student.id, newName)
                editingStudent = null
            }
        )
    }
}

@Composable
private fun StudentItem(
    index: Int,
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.width(32.dp)
            )
            
            Text(
                text = student.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Primary)
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Primary)
            }
        }
    }
}

@Composable
private fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加学生") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditStudentDialog(
    student: Student,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改学生") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun BatchImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var namesText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("批量导入学生") },
        text = {
            Column {
                Text("每行一个姓名", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = namesText,
                    onValueChange = { namesText = it },
                    label = { Text("姓名列表") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                val names = namesText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                onConfirm(names)
            }) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
