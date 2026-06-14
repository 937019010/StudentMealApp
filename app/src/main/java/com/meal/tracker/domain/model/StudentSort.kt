package com.meal.tracker.domain.model

import java.text.Collator
import java.util.Locale

/**
 * 学生列表的统一排序规则。
 *
 * 优先级（高 → 低）：
 *   1. **未画圈**的排前（按 [filter] 动态判定，见 [isUnmarked]）
 *     - 全部：至少一个餐没圈
 *     - 早餐：早餐没圈
 *     - 午餐：午餐没圈
 *     - 晚餐：晚餐没圈
 *   2. 名字含数字前缀 1-99 → 按数字值升序（学号 100+ 视为非数字）
 *   3. 其余按字符串比较，中文走 [Collator] + [Locale.CHINA] 按拼音，
 *      其余字符按自然字符串
 *
 * 举例（filter = BREAKFAST，"✓" 已画圈早餐，"·" 未画圈早餐）：
 *   输入顺序：张三, 1李四, 王五, 2张三, 李四, 10赵六
 *   假设：李四✓ 张三· 1李四· 2张三· 王五✓ 10赵六·  张三·(注:用·表示未画)
 *
 *   BREAKFAST 期望输出：
 *     1李四·  2张三·  10赵六·  李四·  张三·  王五✓  李四✓
 *     └─数字升序─┘   └ 其它按拼音 ┘  └ 已画圈（后）┘
 */
object StudentSort {

    private val nameCollator: Collator = Collator.getInstance(Locale.CHINA)
    private val leadingNumberRegex = Regex("^(\\d+)")

    fun sort(
        students: List<StudentWithMeals>,
        filter: MealFilter
    ): List<StudentWithMeals> = students.sortedWith(Comparator { a, b ->
        // —— 1. 未画圈优先（true 排前，false 排后）——
        val aUnmarked = isUnmarked(a, filter)
        val bUnmarked = isUnmarked(b, filter)
        if (aUnmarked != bUnmarked) return@Comparator if (aUnmarked) -1 else 1

        // —— 2. 数字前缀优先 ——
        val aNum = leadingNumber(a.student.name)
        val bNum = leadingNumber(b.student.name)
        val aHasNum = aNum != null
        val bHasNum = bNum != null
        if (aHasNum != bHasNum) return@Comparator if (aHasNum) -1 else 1
        if (aHasNum && bHasNum && aNum != bNum) {
            return@Comparator aNum!!.compareTo(bNum!!)
        }

        // —— 3. 同 key 内：中文按拼音，英文按自然字符串 ——
        nameCollator.compare(a.student.name, b.student.name)
    })

    private fun isUnmarked(item: StudentWithMeals, filter: MealFilter): Boolean = when (filter) {
        MealFilter.ALL -> !item.breakfastMarked && !item.lunchMarked && !item.dinnerMarked
        MealFilter.BREAKFAST -> !item.breakfastMarked
        MealFilter.LUNCH -> !item.lunchMarked
        MealFilter.DINNER -> !item.dinnerMarked
    }

    /**
     * 提取名字开头的 1-99 数字。范围外的数字（如学号 100+）视为非数字前缀。
     */
    fun leadingNumber(name: String): Int? =
        leadingNumberRegex.find(name.trim())
            ?.groupValues?.get(1)
            ?.toIntOrNull()
            ?.takeIf { it in 1..99 }
}
