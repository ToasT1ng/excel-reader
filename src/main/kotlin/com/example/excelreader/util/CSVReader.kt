package com.example.excelreader.util

import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object CSVReader {

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

    fun <T : Any> readCSV(
        file: MultipartFile,
        kClass: KClass<T>,
        delimiter: Char = ',',
        skipRows: Int = 1,
        skipEmptyRows: Boolean = true
    ): List<T> {
        return file.inputStream.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.lineSequence()
                .drop(skipRows)
                .filter { line ->
                    !skipEmptyRows || line.isNotBlank()
                }
                .mapNotNull { line ->
                    try {
                        val values = parseCSVLine(line, delimiter)
                        // Skip rows where all values are empty
                        if (skipEmptyRows && values.all { it.isBlank() }) {
                            return@mapNotNull null
                        }
                        mapRowToObject(values, kClass)
                    } catch (e: Exception) {
                        println("CSV 행 처리 중 오류: ${e.message}")
                        null
                    }
                }
                .toList()
        }
    }

    private fun parseCSVLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        current.append('"')
                        i++
                    } else {
                        // Toggle quote mode
                        inQuotes = !inQuotes
                    }
                }
                char == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }

        result.add(current.toString().trim())
        return result
    }

    private fun <T : Any> mapRowToObject(values: List<String>, kClass: KClass<T>): T {
        val constructor = kClass.primaryConstructor
            ?: throw IllegalArgumentException("Primary constructor not found")

        val params = constructor.parameters.mapIndexed { index, param ->
            val value = if (index < values.size) values[index] else null
            param to convertValue(value, param.type.classifier as KClass<*>)
        }.toMap()

        return constructor.callBy(params)
    }

    private fun convertValue(value: String?, targetType: KClass<*>): Any? {
        if (value.isNullOrBlank()) return null

        return when {
            targetType == String::class -> value
            targetType == Int::class -> parseStringToInt(value)
            targetType == Long::class -> parseStringToLong(value)
            targetType == Double::class -> parseStringToDouble(value)
            targetType == LocalDate::class -> parseStringToLocalDate(value)
            targetType == LocalDateTime::class -> parseStringToLocalDateTime(value)
            targetType == Boolean::class -> parseStringToBoolean(value)
            else -> value
        }
    }

    private fun parseStringToInt(value: String): Int? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null

        val numericString = trimmed.replace(Regex("[^0-9-]"), "")
        return try {
            numericString.toIntOrNull()
        } catch (e: Exception) {
            println("Int 파싱 실패: $value")
            null
        }
    }

    private fun parseStringToLong(value: String): Long? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null

        val numericString = trimmed.replace(Regex("[^0-9-]"), "")
        return try {
            numericString.toLongOrNull()
        } catch (e: Exception) {
            println("Long 파싱 실패: $value")
            null
        }
    }

    private fun parseStringToDouble(value: String): Double? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null

        val numericString = trimmed.replace(",", "").replace(Regex("[^0-9.-]"), "")
        return try {
            numericString.toDoubleOrNull()
        } catch (e: Exception) {
            println("Double 파싱 실패: $value")
            null
        }
    }

    private fun parseStringToBoolean(value: String): Boolean? {
        val trimmed = value.trim().lowercase()
        return when (trimmed) {
            "true", "yes", "y", "1" -> true
            "false", "no", "n", "0" -> false
            else -> {
                println("Boolean 파싱 실패: $value")
                null
            }
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
}
