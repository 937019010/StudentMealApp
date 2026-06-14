package com.mealapp.ui.screens.statistics

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mealapp.domain.model.StudentStatistics
import com.mealapp.ui.theme.*
import com.mealapp.viewmodel.ExportResult
import com.mealapp.viewmodel.StatisticsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val studentStatistics by viewModel.studentStatistics.collectAsState()
    val context = LocalContext.current
    
    var showExportMenu by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.exportResult.collect { result ->
            when (result) {
                is ExportResult.Success -> Toast.makeText(context, "导出成功: ${result.path}", Toast.LENGTH_LONG).show()
                is ExportResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计数据") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "导出",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导出 CSV") },
                                onClick = {
                                    viewModel.exportToCSV(context)
                                    showExportMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.TableChart, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("导出 Excel") },
                                onClick = {
                                    viewModel.exportToExcel(context)
                                    showExportMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Description, null) }
                            )
                        }
                    }
                }
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date range selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "统计时间范围",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DateSelectorButton(
                            label = "开始",
                            date = startDate,
                            onClick = { showStartDatePicker = true }
                        )
                        Text("-", color = TextSecondary, fontSize = 18.sp)
                        DateSelectorButton(
                            label = "结束",
                            date = endDate,
                            onClick = { showEndDatePicker = true }
                        )
                    }
                }
            }
            
            // Statistics summary card
            if (studentStatistics.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "汇总统计",
                            fontSize = 14.sp,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem("总人数", "${studentStatistics.size} 人")
                            StatItem("早餐", "${studentStatistics.sumOf { it.breakfastMarked }} 餐")
                            StatItem("午餐", "${studentStatistics.sumOf { it.lunchMarked }} 餐")
                            StatItem("晚餐", "${studentStatistics.sumOf { it.dinnerMarked }} 餐")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Student statistics list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(studentStatistics) { stat ->
                    StudentStatCard(stat)
                }
            }
        }
    }
    
    // Start date picker dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            currentDate = startDate,
            onDateSelected = { date ->
                viewModel.setDateRange(date, endDate)
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    // End date picker dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            currentDate = endDate,
            onDateSelected = { date ->
                viewModel.setDateRange(startDate, date)
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun DateSelectorButton(
    label: String,
    date: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(140.dp)
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(date)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val initialDate = try {
        dateFormat.parse(currentDate) ?: Date()
    } catch (e: Exception) {
        Date()
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        val selectedDate = dateFormat.format(calendar.time)
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Primary,
                todayContentColor = Primary,
                todayDateBorderColor = Primary
            )
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StudentStatCard(stat: StudentStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.student.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "总计: ${stat.totalMarked} 餐",
                    fontSize = 14.sp,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MealStatChip("早餐", stat.breakfastMarked)
                MealStatChip("午餐", stat.lunchMarked)
                MealStatChip("晚餐", stat.dinnerMarked)
            }
        }
    }
}

@Composable
private fun MealStatChip(label: String, count: Int) {
    Surface(
        color = if (count > 0) PrimaryLight else Background,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "$count",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) Primary else TextSecondary
            )
        }
    }
}
