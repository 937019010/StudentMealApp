package com.meal.tracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meal.tracker.domain.model.MealFilter
import com.meal.tracker.domain.model.MealType
import com.meal.tracker.domain.model.StudentWithMeals
import com.meal.tracker.ui.theme.*

/**
 * 学生卡片（v1.2 视觉/动效升级）
 *
 * 升级点：
 * 1. 卡片化：圆角 12dp、elevation 1→2、未画圈态边框更轻
 * 2. 微动效：
 *    - 整卡按压：pressed 时 scale 0.97 + elevation 跳到 6dp
 *    - 圈选切换：背景色 animateColorAsState 平滑过渡
 *    - 圈选弹跳：从 0.85 spring 弹回 1.0
 *    - 触发 HapticFeedback.LongPress 震动反馈
 */
@Composable
fun StudentMealCard(
    studentWithMeals: StudentWithMeals,
    index: Int,
    mealFilter: MealFilter = MealFilter.ALL,
    onMealToggle: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardElevation by animateFloatAsState(
        targetValue = if (isPressed) 6f else 2f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardElevation"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,  // 关闭默认 ripple，由我们自己的 scale 接管
                onClick = { /* 整卡不响应点击，只把点击交由内层 MealButton */ }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MealButton(
                            label = "早",
                            isMarked = studentWithMeals.breakfastMarked,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMealToggle(MealType.BREAKFAST)
                            }
                        )
                        MealButton(
                            label = "午",
                            isMarked = studentWithMeals.lunchMarked,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMealToggle(MealType.LUNCH)
                            }
                        )
                        MealButton(
                            label = "晚",
                            isMarked = studentWithMeals.dinnerMarked,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMealToggle(MealType.DINNER)
                            }
                        )
                    }
                }
                MealFilter.BREAKFAST -> {
                    MealText(
                        label = "早",
                        isMarked = studentWithMeals.breakfastMarked,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMealToggle(MealType.BREAKFAST)
                        }
                    )
                }
                MealFilter.LUNCH -> {
                    MealText(
                        label = "午",
                        isMarked = studentWithMeals.lunchMarked,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMealToggle(MealType.LUNCH)
                        }
                    )
                }
                MealFilter.DINNER -> {
                    MealText(
                        label = "晚",
                        isMarked = studentWithMeals.dinnerMarked,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMealToggle(MealType.DINNER)
                        }
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
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1) 背景色平滑过渡
    val bgColor by animateColorAsState(
        targetValue = if (isMarked) Primary else Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMarked) Primary else UnmarkedColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "borderColor"
    )
    // 2) 弹跳缩放：未画圈 → 画圈 那一刻给个小弹跳（0.85 → 1.0）
    // 这里用 isMarked 作为驱动，目标值 1f；按下时 0.9 叠加
    val targetScale = when {
        isPressed -> 0.9f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
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
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor by animateColorAsState(
        targetValue = if (isMarked) Primary else Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "textBgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMarked) Primary else UnmarkedColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "textBorderColor"
    )
    val targetScale = if (isPressed) 0.92f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "textScale"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
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
