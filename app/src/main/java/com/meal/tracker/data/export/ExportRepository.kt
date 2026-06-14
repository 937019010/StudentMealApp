package com.meal.tracker.data.export

import com.meal.tracker.data.entity.MealRecordEntity
import com.meal.tracker.data.preferences.SettingsManager
import com.meal.tracker.data.repository.MealRepository
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

/**
 * 负责把统计数据落盘为 CSV / Excel。
 * 抽取自原 StatisticsViewModel，避免 ViewModel 持有文件 IO 细节。
 */
class ExportRepository(
    private val mealRepository: MealRepository,
    private val settingsManager: SettingsManager
) {

    suspend fun exportCsv(startDate: String, endDate: String): File =
        withContext(Dispatchers.IO) {
            val (students, records) = mealRepository.loadStudentsAndRecordsInRange(startDate, endDate)
            val target = newExportFile(startDate, endDate, ext = "csv")

            CSVWriter(FileWriter(target)).use { writer ->
                writer.writeNext(arrayOf("姓名", "日期", "早餐", "午餐", "晚餐"))
                val grouped = records.groupBy { it.date }
                grouped.forEach { (date, dayRecords) ->
                    val byStudent = dayRecords.groupBy { it.studentId }
                    students.forEach { student ->
                        val studentRecords = byStudent[student.id].orEmpty()
                        writer.writeNext(
                            arrayOf(
                                student.name,
                                date,
                                mealMark(studentRecords, "breakfast"),
                                mealMark(studentRecords, "lunch"),
                                mealMark(studentRecords, "dinner")
                            )
                        )
                    }
                }
            }
            target
        }

    suspend fun exportExcel(startDate: String, endDate: String): File =
        withContext(Dispatchers.IO) {
            val (students, records) = mealRepository.loadStudentsAndRecordsInRange(startDate, endDate)
            val target = newExportFile(startDate, endDate, ext = "xlsx")

            val workbook = XSSFWorkbook()
            try {
                val sheet = workbook.createSheet("就餐统计")
                val header = sheet.createRow(0)
                listOf("姓名", "早餐", "午餐", "晚餐", "总计").forEachIndexed { i, title ->
                    header.createCell(i).setCellValue(title)
                }

                students.forEachIndexed { index, student ->
                    val studentRecords = records.filter { it.studentId == student.id }
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(student.name)
                    row.createCell(1).setCellValue(studentRecords.count { it.mealType == "breakfast" && it.isMarked }.toString())
                    row.createCell(2).setCellValue(studentRecords.count { it.mealType == "lunch" && it.isMarked }.toString())
                    row.createCell(3).setCellValue(studentRecords.count { it.mealType == "dinner" && it.isMarked }.toString())
                    row.createCell(4).setCellValue(studentRecords.count { it.isMarked }.toString())
                }
                FileOutputStream(target).use { fos -> workbook.write(fos) }
            } finally {
                workbook.close()
            }
            target
        }

    private fun mealMark(records: List<MealRecordEntity>, type: String): String =
        if (records.any { it.mealType == type && it.isMarked }) "✓" else ""

    private fun newExportFile(startDate: String, endDate: String, ext: String): File {
        val dir = settingsManager.resolveExportDir()
        val name = "就餐统计_${startDate}_$endDate.$ext"
        return File(dir, name)
    }
}
