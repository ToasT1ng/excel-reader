package com.example.excelreader.util

import org.apache.poi.ss.usermodel.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object ExcelReader {

    // Reflection으로 패턴을 DateTimeFormatter 리스트로 변환
    private val dateTimeFormatters by lazy {
        DateTimePatterns::class.java.declaredFields
            .filter { it.name.startsWith("DATETIME_PATTERN") }
            .map { it.get(null) as String }
            .map { DateTimeFormatter.ofPattern(it) } + listOf(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ISO_DATE_TIME
            )
    }

    private val dateFormatters by lazy {
        DateTimePatterns::class.java.declaredFields
            .filter { it.name.startsWith("DATE_PATTERN") }
            .map { it.get(null) as String }
            .map { DateTimeFormatter.ofPattern(it) } + listOf(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ISO_DATE
            )
    }

    fun <T : Any> readExcel(
        file: MultipartFile,
        kClass: KClass<T>,
        sheetIndex: Int = 0,
        skipRows: Int = 1,
        skipEmptyRows: Boolean = true
    ): List<T> {
        return file.inputStream.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()

            if (sheetIndex >= workbook.numberOfSheets) {
                throw IllegalArgumentException(
                    "시트 인덱스 $sheetIndex 가 범위를 벗어났습니다. " +
                    "총 시트 개수: ${workbook.numberOfSheets}"
                )
            }

            val sheet = workbook.getSheetAt(sheetIndex)
            println("읽는 시트: ${sheet.sheetName}")

            sheet.drop(skipRows)
                .filter { row ->
                    !skipEmptyRows || !isRowEmpty(row)
                }
                .mapNotNull { row ->
                    try {
                        mapRowToObject(row, kClass, formulaEvaluator)
                    } catch (e: Exception) {
                        println("행 ${row.rowNum} 처리 중 오류: ${e.message}")
                        null
                    }
                }
        }
    }

    fun <T : Any> readExcelBySheetName(
        file: MultipartFile,
        kClass: KClass<T>,
        sheetName: String,
        skipRows: Int = 1,
        skipEmptyRows: Boolean = true
    ): List<T> {
        return file.inputStream.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()

            val sheet = workbook.getSheet(sheetName)
                ?: throw IllegalArgumentException("시트 '$sheetName' 을 찾을 수 없습니다.")

            println("읽는 시트: ${sheet.sheetName}")

            sheet.drop(skipRows)
                .filter { row ->
                    !skipEmptyRows || !isRowEmpty(row)
                }
                .mapNotNull { row ->
                    try {
                        mapRowToObject(row, kClass, formulaEvaluator)
                    } catch (e: Exception) {
                        println("행 ${row.rowNum} 처리 중 오류: ${e.message}")
                        null
                    }
                }
        }
    }

    fun getSheetNames(file: MultipartFile): List<String> {
        return file.inputStream.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            (0 until workbook.numberOfSheets).map { index ->
                workbook.getSheetAt(index).sheetName
            }
        }
    }

    private fun isRowEmpty(row: Row): Boolean {
        for (cellIndex in row.firstCellNum until row.lastCellNum) {
            val cell = row.getCell(cellIndex) ?: continue

            when (cell.cellType) {
                CellType.STRING -> {
                    if (cell.stringCellValue.isNotBlank()) {
                        return false
                    }
                }
                CellType.NUMERIC -> return false
                CellType.BOOLEAN -> return false
                CellType.FORMULA -> return false
                CellType.BLANK -> continue
                else -> continue
            }
        }
        return true
    }

    private fun <T : Any> mapRowToObject(
        row: Row,
        kClass: KClass<T>,
        formulaEvaluator: FormulaEvaluator
    ): T {
        val constructor = kClass.primaryConstructor
            ?: throw IllegalArgumentException("Primary constructor not found")

        val params = constructor.parameters.mapIndexed { index, param ->
            val cell = row.getCell(index)
            val value = getCellValue(cell, formulaEvaluator)
            param to convertValue(value, param.type.classifier as KClass<*>)
        }.toMap()

        return constructor.callBy(params)
    }

    private fun convertValue(value: Any?, targetType: KClass<*>): Any? {
        return when {
            value == null -> null
            targetType == String::class -> value.toString()
            targetType == Int::class -> (value as? Double)?.toInt() ?: value
            targetType == Long::class -> (value as? Double)?.toLong() ?: value
            targetType == Double::class -> value as? Double
            targetType == LocalDate::class -> {
                when (value) {
                    is LocalDateTime -> value.toLocalDate()
                    is LocalDate -> value
                    is String -> parseStringToLocalDate(value)
                    else -> null
                }
            }
            targetType == LocalDateTime::class -> {
                when (value) {
                    is LocalDateTime -> value
                    is String -> parseStringToLocalDateTime(value)
                    else -> null
                }
            }
            targetType == Boolean::class -> value as? Boolean
            else -> value
        }
    }

    private fun parseStringToLocalDateTime(value: String): LocalDateTime? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null

        for (formatter in dateTimeFormatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter)
            } catch (e: DateTimeParseException) {
                // 다음 포맷터 시도
            }
        }

        // LocalDateTime 파싱 실패 시 LocalDate로 파싱 후 시간 00:00:00 추가
        val date = parseStringToLocalDate(trimmed)
        return date?.atStartOfDay()
    }

    private fun parseStringToLocalDate(value: String): LocalDate? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null

        for (formatter in dateFormatters) {
            try {
                return LocalDate.parse(trimmed, formatter)
            } catch (e: DateTimeParseException) {
                // 다음 포맷터 시도
            }
        }

        println("날짜 파싱 실패: $value")
        return null
    }

    private fun getCellValue(cell: Cell?, formulaEvaluator: FormulaEvaluator): Any? {
        if (cell == null) return null

        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> handleNumericCell(cell)
            CellType.BOOLEAN -> cell.booleanCellValue
            CellType.FORMULA -> handleFormulaCell(cell, formulaEvaluator)
            CellType.BLANK -> null
            CellType.ERROR -> {
                println("셀 에러 발견: ${cell.address}")
                null
            }
            else -> null
        }
    }

    private fun handleFormulaCell(cell: Cell, formulaEvaluator: FormulaEvaluator): Any? {
        return try {
            val evaluatedCell = formulaEvaluator.evaluate(cell)

            when (evaluatedCell.cellType) {
                CellType.STRING -> evaluatedCell.stringValue
                CellType.NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cell.dateCellValue.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    } else {
                        evaluatedCell.numberValue
                    }
                }
                CellType.BOOLEAN -> evaluatedCell.booleanValue
                CellType.BLANK -> null
                CellType.ERROR -> {
                    println("함수 계산 에러: ${cell.cellFormula}")
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            println("함수 평가 실패 (${cell.cellFormula}): ${e.message}")
            null
        }
    }

    private fun handleNumericCell(cell: Cell): Any {
        return if (DateUtil.isCellDateFormatted(cell)) {
            cell.dateCellValue.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } else {
            cell.numericCellValue
        }
    }
}
