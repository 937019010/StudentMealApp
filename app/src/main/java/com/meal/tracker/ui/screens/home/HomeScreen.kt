package com.meal.tracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meal.tracker.domain.model.MealFilter
import com.meal.tracker.ui.components.MealFilterChips
import com.meal.tracker.ui.components.StudentMealCard
import com.meal.tracker.ui.theme.*
import com.meal.tracker.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val mealFilter by viewModel.mealFilter.collectAsState()
    val filteredStudents by viewModel.filteredStudents.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.exportResult.collect { result ->
            val msg = when (result) {
                is ExportResult.Success -> "保存成功: ${result.path}"
                is ExportResult.Error -> result.message
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学生就餐统计") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    TextButton(
                        onClick = { viewModel.exportToday() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("保存")
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
            // Date selector
            DateSelector(
                date = selectedDate,
                onPreviousDay = { viewModel.goToPreviousDay() },
                onNextDay = { viewModel.goToNextDay() }
            )
            
            // Filter chips
            MealFilterChips(
                selectedFilter = mealFilter,
                onFilterSelected = { viewModel.setMealFilter(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Statistics summary
            statistics?.let { stats ->
                StatisticsSummary(
                    date = selectedDate,
                    totalStudents = stats.totalStudents,
                    markedCount = stats.markedCount,
                    unmarkedCount = stats.unmarkedCount
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Student grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredStudents) { index, studentWithMeals ->
                    StudentMealCard(
                        studentWithMeals = studentWithMeals,
                        index = index,
                        mealFilter = mealFilter,
                        onMealToggle = { mealType ->
                            viewModel.toggleMealMark(studentWithMeals.student.id, mealType)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateSelector(
    date: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前一天",
                tint = Primary
            )
        }
        
        Text(
            text = HomeViewModel.formatDisplayDate(date),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground
        )
        
        IconButton(onClick = onNextDay) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "后一天",
                tint = Primary
            )
        }
    }
}

@Composable
private fun StatisticsSummary(
    date: String,
    totalStudents: Int,
    markedCount: Int,
    unmarkedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "日期：$date",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "已圈 $markedCount 餐",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "未圈 $unmarkedCount 餐",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "共 $totalStudents 人",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "每行3个孩子",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
