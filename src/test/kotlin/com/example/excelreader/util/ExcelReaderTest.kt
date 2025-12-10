package com.example.excelreader.util

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime

class ExcelReaderTest {

    data class Employee(
        val name: String?,
        val age: Int?,
        val department: String?,
        val position: String?,
        val salary: Double?,
        val hireDate: LocalDate?,
        val lastLogin: LocalDateTime?,
        val isActive: Boolean?
    )

    @Test
    fun `정상적인 엑셀 파일을 읽을 수 있다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", 32.0, "개발팀", "대리", 5500.0, LocalDate.of(2020, 3, 15), LocalDateTime.of(2025, 12, 8, 9, 30), true),
                listOf("이영희", 28.0, "마케팅팀", "사원", 4200.0, LocalDate.of(2022, 6, 1), LocalDateTime.of(2025, 12, 8, 8, 45), true)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(2, result.size)
        assertEquals("김철수", result[0].name)
        assertEquals(32, result[0].age)
        assertEquals("개발팀", result[0].department)
        assertEquals(5500.0, result[0].salary)
        assertEquals(LocalDate.of(2020, 3, 15), result[0].hireDate)
        assertEquals(true, result[0].isActive)
    }

    @Test
    fun `null 값이 포함된 엑셀 파일을 읽을 수 있다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", null, "개발팀", "대리", null, null, null, null),
                listOf(null, 30.0, null, null, 5000.0, LocalDate.of(2021, 1, 1), LocalDateTime.of(2025, 1, 1, 0, 0), true)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(2, result.size)
        assertEquals("김철수", result[0].name)
        assertNull(result[0].age)
        assertNull(result[0].salary)
        assertNull(result[0].hireDate)

        assertNull(result[1].name)
        assertEquals(30, result[1].age)
        assertEquals(5000.0, result[1].salary)
    }

    @Test
    fun `문자열 타입 변환이 올바르게 동작한다`() {
        // given - 나이가 문자열 "28세"
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", "28세", "개발팀", "대리", "5,500원", null, null, "Yes"),
                listOf("이영희", "  35  ", "마케팅팀", "사원", "  4200  ", null, null, "TRUE")
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(2, result.size)
        assertEquals(28, result[0].age)  // "28세" -> 28
        assertEquals(5500.0, result[0].salary)  // "5,500원" -> 5500.0
        assertEquals(true, result[0].isActive)  // "Yes" -> true

        assertEquals(35, result[1].age)  // "  35  " -> 35
        assertEquals(4200.0, result[1].salary)  // "  4200  " -> 4200.0
        assertEquals(true, result[1].isActive)  // "TRUE" -> true
    }

    @Test
    fun `Boolean 타입 변환이 올바르게 동작한다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("A", 30.0, "팀1", "직급1", 1000.0, null, null, true),
                listOf("B", 30.0, "팀2", "직급2", 1000.0, null, null, false),
                listOf("C", 30.0, "팀3", "직급3", 1000.0, null, null, "Yes"),
                listOf("D", 30.0, "팀4", "직급4", 1000.0, null, null, "No"),
                listOf("E", 30.0, "팀5", "직급5", 1000.0, null, null, "TRUE"),
                listOf("F", 30.0, "팀6", "직급6", 1000.0, null, null, "FALSE"),
                listOf("G", 30.0, "팀7", "직급7", 1000.0, null, null, "1"),
                listOf("H", 30.0, "팀8", "직급8", 1000.0, null, null, "0"),
                listOf("I", 30.0, "팀9", "직급9", 1000.0, null, null, 1.0),
                listOf("J", 30.0, "팀10", "직급10", 1000.0, null, null, 0.0)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(10, result.size)
        assertEquals(true, result[0].isActive)   // true
        assertEquals(false, result[1].isActive)  // false
        assertEquals(true, result[2].isActive)   // "Yes"
        assertEquals(false, result[3].isActive)  // "No"
        assertEquals(true, result[4].isActive)   // "TRUE"
        assertEquals(false, result[5].isActive)  // "FALSE"
        assertEquals(true, result[6].isActive)   // "1"
        assertEquals(false, result[7].isActive)  // "0"
        assertEquals(true, result[8].isActive)   // 1.0
        assertEquals(false, result[9].isActive)  // 0.0
    }

    @Test
    fun `음수와 큰 숫자를 올바르게 처리한다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("음수테스트", -5.0, "개발팀", "인턴", 0.0, null, null, true),
                listOf("큰숫자", 999.0, "임원", "CEO", 999999999.0, null, null, true)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(2, result.size)
        assertEquals(-5, result[0].age)
        assertEquals(0.0, result[0].salary)
        assertEquals(999, result[1].age)
        assertEquals(999999999.0, result[1].salary)
    }

    @Test
    fun `빈 행은 건너뛴다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", 32.0, "개발팀", "대리", 5500.0, null, null, true),
                listOf(null, null, null, null, null, null, null, null),  // 빈 행
                listOf("", "", "", "", "", null, null, null),  // 빈 문자열 행
                listOf("이영희", 28.0, "마케팅팀", "사원", 4200.0, null, null, true)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class, skipEmptyRows = true)

        // then
        assertEquals(2, result.size)
        assertEquals("김철수", result[0].name)
        assertEquals("이영희", result[1].name)
    }

    @Test
    fun `날짜 형식을 올바르게 파싱한다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", 30.0, "개발팀", "대리", 5000.0, LocalDate.of(2020, 3, 15), LocalDateTime.of(2025, 12, 8, 9, 30), true),
                listOf("이영희", 28.0, "마케팅팀", "사원", 4200.0, "2021-06-01", "2025-12-08 10:15:00", true),
                listOf("박민수", 35.0, "영업팀", "과장", 6000.0, "2019/07/15", "2025/12/07 18:00:00", true)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(3, result.size)
        assertEquals(LocalDate.of(2020, 3, 15), result[0].hireDate)
        assertEquals(LocalDateTime.of(2025, 12, 8, 9, 30), result[0].lastLogin)
        assertEquals(LocalDate.of(2021, 6, 1), result[1].hireDate)
        assertEquals(LocalDateTime.of(2025, 12, 8, 10, 15), result[1].lastLogin)
        assertEquals(LocalDate.of(2019, 7, 15), result[2].hireDate)
        assertEquals(LocalDateTime.of(2025, 12, 7, 18, 0), result[2].lastLogin)
    }

    @Test
    fun `잘못된 날짜 형식은 null을 반환한다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", 30.0, "개발팀", "대리", 5000.0, "invalid-date", "invalid-datetime", true)
            )
        )

        // when
        val result = ExcelReader.readExcel(file, Employee::class)

        // then
        assertEquals(1, result.size)
        assertNull(result[0].hireDate)
        assertNull(result[0].lastLogin)
    }

    @Test
    fun `시트 이름으로 읽기가 동작한다`() {
        // given
        val file = createTestExcelFileWithSheetName(
            sheetName = "직원정보",
            data = listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부"),
                listOf("김철수", 32.0, "개발팀", "대리", 5500.0, null, null, true)
            )
        )

        // when
        val result = ExcelReader.readExcelBySheetName(file, Employee::class, "직원정보")

        // then
        assertEquals(1, result.size)
        assertEquals("김철수", result[0].name)
    }

    @Test
    fun `존재하지 않는 시트 이름으로 읽으면 예외가 발생한다`() {
        // given
        val file = createTestExcelFileWithSheetName(
            sheetName = "직원정보",
            data = listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부")
            )
        )

        // when & then
        assertThrows<IllegalArgumentException> {
            ExcelReader.readExcelBySheetName(file, Employee::class, "존재하지않는시트")
        }
    }

    @Test
    fun `시트 이름 목록을 가져올 수 있다`() {
        // given
        val workbook = XSSFWorkbook()
        workbook.createSheet("Sheet1")
        workbook.createSheet("직원정보")
        workbook.createSheet("부서정보")

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        val file = MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        )

        // when
        val sheetNames = ExcelReader.getSheetNames(file)

        // then
        assertEquals(3, sheetNames.size)
        assertEquals("Sheet1", sheetNames[0])
        assertEquals("직원정보", sheetNames[1])
        assertEquals("부서정보", sheetNames[2])
    }

    @Test
    fun `범위를 벗어난 시트 인덱스는 예외를 발생시킨다`() {
        // given
        val file = createTestExcelFile(
            listOf(
                listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부")
            )
        )

        // when & then
        assertThrows<IllegalArgumentException> {
            ExcelReader.readExcel(file, Employee::class, sheetIndex = 10)
        }
    }

    // Helper functions
    private fun createTestExcelFile(data: List<List<Any?>>): MockMultipartFile {
        return createTestExcelFileWithSheetName("Sheet1", data)
    }

    private fun createTestExcelFileWithSheetName(sheetName: String, data: List<List<Any?>>): MockMultipartFile {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(sheetName)

        data.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex)
            rowData.forEachIndexed { cellIndex, value ->
                val cell = row.createCell(cellIndex)
                when (value) {
                    is String -> cell.setCellValue(value)
                    is Double -> cell.setCellValue(value)
                    is Int -> cell.setCellValue(value.toDouble())
                    is Boolean -> cell.setCellValue(value)
                    is LocalDate -> {
                        cell.setCellValue(java.util.Date.from(
                            value.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                        ))
                        val dateStyle = workbook.createCellStyle()
                        dateStyle.dataFormat = workbook.createDataFormat().getFormat("yyyy-mm-dd")
                        cell.cellStyle = dateStyle
                    }
                    is LocalDateTime -> {
                        cell.setCellValue(java.util.Date.from(
                            value.atZone(java.time.ZoneId.systemDefault()).toInstant()
                        ))
                        val dateTimeStyle = workbook.createCellStyle()
                        dateTimeStyle.dataFormat = workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
                        cell.cellStyle = dateTimeStyle
                    }
                    null -> {
                        // null 값은 빈 셀로 남김
                    }
                }
            }
        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        )
    }
}
