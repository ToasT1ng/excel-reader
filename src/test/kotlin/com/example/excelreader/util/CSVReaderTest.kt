package com.example.excelreader.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDate
import java.time.LocalDateTime

class CSVReaderTest {

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
    fun `정상적인 CSV 파일을 읽을 수 있다`() {
        // given
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            김철수,32,개발팀,대리,5500,2020-03-15,2025-12-08 09:30:00,true
            이영희,28,마케팅팀,사원,4200,2022-06-01,2025-12-08 08:45:00,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

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
    fun `null 값이 포함된 CSV 파일을 읽을 수 있다`() {
        // given
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            김철수,,개발팀,대리,,,,
            ,30,,,,2021-01-01,2025-01-01 00:00:00,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

        // then
        assertEquals(2, result.size)
        assertEquals("김철수", result[0].name)
        assertNull(result[0].age)
        assertNull(result[0].salary)
        assertNull(result[0].hireDate)
        assertNull(result[0].isActive)

        assertNull(result[1].name)
        assertEquals(30, result[1].age)
        assertEquals(LocalDate.of(2021, 1, 1), result[1].hireDate)
    }

    @Test
    fun `문자열 타입 변환이 올바르게 동작한다`() {
        // given
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            김철수,28세,개발팀,대리,"5,500원",,,Yes
            이영희,  35  ,마케팅팀,사원,  4200  ,,,TRUE
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

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
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            A,30,팀1,직급1,1000,,,true
            B,30,팀2,직급2,1000,,,false
            C,30,팀3,직급3,1000,,,Yes
            D,30,팀4,직급4,1000,,,No
            E,30,팀5,직급5,1000,,,TRUE
            F,30,팀6,직급6,1000,,,FALSE
            G,30,팀7,직급7,1000,,,1
            H,30,팀8,직급8,1000,,,0
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

        // then
        assertEquals(8, result.size)
        assertEquals(true, result[0].isActive)   // true
        assertEquals(false, result[1].isActive)  // false
        assertEquals(true, result[2].isActive)   // "Yes"
        assertEquals(false, result[3].isActive)  // "No"
        assertEquals(true, result[4].isActive)   // "TRUE"
        assertEquals(false, result[5].isActive)  // "FALSE"
        assertEquals(true, result[6].isActive)   // "1"
        assertEquals(false, result[7].isActive)  // "0"
    }

    @Test
    fun `음수와 큰 숫자를 올바르게 처리한다`() {
        // given
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            음수테스트,-5,개발팀,인턴,0,,,true
            큰숫자,999,임원,CEO,999999999,,,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

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
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            김철수,32,개발팀,대리,5500,,,true

            ,,,,,,
            이영희,28,마케팅팀,사원,4200,,,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class, skipEmptyRows = true)

        // then
        assertEquals(2, result.size)
        assertEquals("김철수", result[0].name)
        assertEquals("이영희", result[1].name)
    }

    @Test
    fun `날짜 형식을 올바르게 파싱한다`() {
        // given
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            김철수,30,개발팀,대리,5000,2020-03-15,2025-12-08 09:30:00,true
            이영희,28,마케팅팀,사원,4200,2021-06-01,2025-12-08 10:15:00,true
            박민수,35,영업팀,과장,6000,2019/07/15,2025/12/07 18:00:00,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

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
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            김철수,30,개발팀,대리,5000,invalid-date,invalid-datetime,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

        // then
        assertEquals(1, result.size)
        assertNull(result[0].hireDate)
        assertNull(result[0].lastLogin)
    }

    @Test
    fun `쉼표가 포함된 필드를 올바르게 처리한다`() {
        // given - 따옴표로 감싸진 필드는 내부 쉼표를 보존
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            이쉼표,30,"개발팀, 마케팅팀",대리,5500,2020-01-01,2025-12-08 09:00:00,true
            박따옴표,32,"영업팀, 기획팀, 디자인팀",과장,6000,2019-01-01,2025-12-08 10:00:00,true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

        // then
        assertEquals(2, result.size)
        assertEquals("이쉼표", result[0].name)
        assertEquals("개발팀, 마케팅팀", result[0].department)
        assertEquals("박따옴표", result[1].name)
        assertEquals("영업팀, 기획팀, 디자인팀", result[1].department)
    }

    @Test
    fun `다른 구분자를 사용할 수 있다`() {
        // given - 세미콜론을 구분자로 사용
        val csvContent = """
            이름;나이;부서;직급;연봉;입사일;최근 로그인;재직여부
            김철수;32;개발팀;대리;5500;2020-03-15;2025-12-08 09:30:00;true
            이영희;28;마케팅팀;사원;4200;2022-06-01;2025-12-08 08:45:00;true
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class, delimiter = ';')

        // then
        assertEquals(2, result.size)
        assertEquals("김철수", result[0].name)
        assertEquals(32, result[0].age)
        assertEquals("이영희", result[1].name)
        assertEquals(28, result[1].age)
    }

    @Test
    fun `따옴표 안의 이스케이프된 따옴표를 처리한다`() {
        // given - 따옴표 안에 이스케이프된 따옴표가 있는 경우
        val csvContent = "이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부\n" +
                "김철수,30,\"개발팀 \"\"핵심\"\"\",대리,5500,,,true"

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

        // then
        assertEquals(1, result.size)
        assertEquals("김철수", result[0].name)
        assertEquals("개발팀 \"핵심\"", result[0].department)
    }

    @Test
    fun `0 값을 올바르게 처리한다`() {
        // given
        val csvContent = """
            이름,나이,부서,직급,연봉,입사일,최근 로그인,재직여부
            최제로,0,인턴팀,인턴,0,2025-12-01,2025-12-08 10:00:00,0
        """.trimIndent()

        val file = createTestCSVFile(csvContent)

        // when
        val result = CSVReader.readCSV(file, Employee::class)

        // then
        assertEquals(1, result.size)
        assertEquals("최제로", result[0].name)
        assertEquals(0, result[0].age)
        assertEquals(0.0, result[0].salary)
        assertEquals(false, result[0].isActive)  // "0" -> false
    }

    // Helper function
    private fun createTestCSVFile(csvContent: String): MockMultipartFile {
        return MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
    }
}
