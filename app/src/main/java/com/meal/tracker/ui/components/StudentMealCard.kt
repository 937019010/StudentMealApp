package com.meal.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meal.tracker.domain.model.MealFilter
import com.meal.tracker.domain.model.MealType
import com.meal.tracker.domain.model.StudentWithMeals
import com.meal.tracker.ui.theme.*

@Composable
fun StudentMealCard(
    studentWithMeals: StudentWithMeals,
    index: Int,
    mealFilter: MealFilter = MealFilter.ALL,
    onMealToggle: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Student name
            Text(
                text = studentWithMeals.student.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Meal display based on filter
            when (mealFilter) {
                MealFilter.ALL -> {
                    // Show all three meal buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MealButton(
                            label = "早",
                            isMarked = studentWithMeals.breakfastMarked,
                            onClick = { onMealToggle(MealType.BREAKFAST) }
                        )
                        MealButton(
                            label = "午",
                            isMarked = studentWithMeals.lunchMarked,
                            onClick = { onMealToggle(MealType.LUNCH) }
                        )
                        MealButton(
                            label = "晚",
                            isMarked = studentWithMeals.dinnerMarked,
                            onClick = { onMealToggle(MealType.DINNER) }
                        )
                    }
                }
                MealFilter.BREAKFAST -> {
                    MealText(
                        label = "早",
                        isMarked = studentWithMeals.breakfastMarked,
                        onClick = { onMealToggle(MealType.BREAKFAST) }
                    )
                }
                MealFilter.LUNCH -> {
                    MealText(
                        label = "午",
                        isMarked = studentWithMeals.lunchMarked,
                        onClick = { onMealToggle(MealType.LUNCH) }
                    )
                }
                MealFilter.DINNER -> {
                    MealText(
                        label = "晚",
                        isMarked = studentWithMeals.dinnerMarked,
                        onClick = { onMealToggle(MealType.DINNER) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MealButton(
    label: String,
    isMarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isMarked) Primary else Color.White)
            .border(
                width = 1.dp,
                color = if (isMarked) Primary else UnmarkedColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isMarked) Color.White else TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isMarked) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MealText(
    label: String,
    isMarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isMarked) Primary else Color.White)
            .border(
                width = 2.dp,
                color = if (isMarked) Primary else UnmarkedColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isMarked) Color.White else TextSecondary,
            fontSize = 18.sp,
            fontWeight = if (isMarked) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun MealFilterChips(
    selectedFilter: MealFilter,
    onFilterSelected: (MealFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            MealFilter.ALL -> "全部"
                            MealFilter.BREAKFAST -> "早餐"
                            MealFilter.LUNCH -> "午餐"
                            MealFilter.DINNER -> "晚餐"
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
