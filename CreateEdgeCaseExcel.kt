import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun main() {
    createEdgeCaseExcel()
}

fun createEdgeCaseExcel() {
    val workbook: Workbook = XSSFWorkbook()
    val sheet: Sheet = workbook.createSheet("직원 정보")

    // 헤더 스타일
    val headerStyle = workbook.createCellStyle().apply {
        fillForegroundColor = IndexedColors.LIGHT_BLUE.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        val font = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
        }
        setFont(font)
        alignment = HorizontalAlignment.CENTER
        verticalAlignment = VerticalAlignment.CENTER
    }

    // 날짜 스타일
    val dateStyle = workbook.createCellStyle().apply {
        dataFormat = workbook.createDataFormat().getFormat("yyyy-mm-dd")
    }

    val dateTimeStyle = workbook.createCellStyle().apply {
        dataFormat = workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
    }

    // 헤더 행 생성
    val headerRow = sheet.createRow(0)
    val headers = listOf("이름", "나이", "부서", "직급", "연봉", "입사일", "최근 로그인", "재직여부")

    headers.forEachIndexed { index, header ->
        val cell = headerRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = headerStyle
    }

    // 데이터 행 생성 (null 포함 edge case)
    val data = listOf(
        // 1. 정상 데이터 (기준)
        mapOf(
            "이름" to "김철수", "나이" to 32.0, "부서" to "개발팀", "직급" to "대리",
            "연봉" to 5500.0, "입사일" to "2020-03-15", "최근 로그인" to "2025-12-08 09:30:00", "재직여부" to true
        ),

        // 2. 나이만 null
        mapOf(
            "이름" to "이영희", "나이" to null, "부서" to "마케팅팀", "직급" to "사원",
            "연봉" to 4200.0, "입사일" to "2022-06-01", "최근 로그인" to "2025-12-08 08:45:00", "재직여부" to true
        ),

        // 3. 부서와 직급이 null
        mapOf(
            "이름" to "박민수", "나이" to 35.0, "부서" to null, "직급" to null,
            "연봉" to 7000.0, "입사일" to "2018-01-10", "최근 로그인" to "2025-12-07 18:20:00", "재직여부" to true
        ),

        // 4. 연봉만 null
        mapOf(
            "이름" to "정수진", "나이" to 30.0, "부서" to "인사팀", "직급" to "대리",
            "연봉" to null, "입사일" to "2021-09-20", "최근 로그인" to "2025-12-08 10:15:00", "재직여부" to true
        ),

        // 5. 입사일만 null
        mapOf(
            "이름" to "최동욱", "나이" to 42.0, "부서" to "영업팀", "직급" to "부장",
            "연봉" to 9000.0, "입사일" to null, "최근 로그인" to "2025-12-08 07:30:00", "재직여부" to true
        ),

        // 6. 최근 로그인만 null
        mapOf(
            "이름" to "강민지", "나이" to 26.0, "부서" to "디자인팀", "직급" to "사원",
            "연봉" to 4000.0, "입사일" to "2023-03-01", "최근 로그인" to null, "재직여부" to true
        ),

        // 7. 재직여부만 null
        mapOf(
            "이름" to "윤서현", "나이" to 33.0, "부서" to "개발팀", "직급" to "과장",
            "연봉" to 6800.0, "입사일" to "2019-07-15", "최근 로그인" to "2025-12-08 08:00:00", "재직여부" to null
        ),

        // 8. 여러 필드가 null (나이, 연봉, 최근 로그인)
        mapOf(
            "이름" to "한지훈", "나이" to null, "부서" to "마케팅팀", "직급" to "대리",
            "연봉" to null, "입사일" to "2021-11-10", "최근 로그인" to null, "재직여부" to true
        ),

        // 9. 대부분 필드가 null (이름과 부서만 존재)
        mapOf(
            "이름" to "오현아", "나이" to null, "부서" to "영업팀", "직급" to null,
            "연봉" to null, "입사일" to null, "최근 로그인" to null, "재직여부" to null
        ),

        // 10. 모든 필드가 null인 행
        mapOf(
            "이름" to null, "나이" to null, "부서" to null, "직급" to null,
            "연봉" to null, "입사일" to null, "최근 로그인" to null, "재직여부" to null
        ),

        // 11. 첫 번째 필드(이름)만 null
        mapOf(
            "이름" to null, "나이" to 38.0, "부서" to "개발팀", "직급" to "차장",
            "연봉" to 8200.0, "입사일" to "2016-08-01", "최근 로그인" to "2025-12-08 08:30:00", "재직여부" to true
        ),

        // 12. 마지막 필드(재직여부)만 null
        mapOf(
            "이름" to "조민재", "나이" to 27.0, "부서" to "기획팀", "직급" to "사원",
            "연봉" to 4500.0, "입사일" to "2024-01-15", "최근 로그인" to "2024-01-15 09:00:00", "재직여부" to null
        ),

        // 13. 연속된 null 필드 (부서, 직급, 연봉)
        mapOf(
            "이름" to "서지우", "나이" to 35.0, "부서" to null, "직급" to null,
            "연봉" to null, "입사일" to "2019-03-10", "최근 로그인" to "2025-12-08 09:15:00", "재직여부" to true
        ),

        // 14. 빈 문자열 (null과 구분)
        mapOf(
            "이름" to "", "나이" to 29.0, "부서" to "", "직급" to "대리",
            "연봉" to 5300.0, "입사일" to "2022-02-20", "최근 로그인" to "2025-12-08 10:00:00", "재직여부" to true
        ),

        // 15. 나이가 문자열로 되어있는 경우
        mapOf(
            "이름" to "김미래", "나이" to "28세", "부서" to "개발팀", "직급" to "사원",
            "연봉" to 4500.0, "입사일" to "2023-05-10", "최근 로그인" to "2025-12-08 09:00:00", "재직여부" to true
        ),

        // 16. 연봉이 문자열로 되어있는 경우 (쉼표 포함)
        mapOf(
            "이름" to "이과거", "나이" to 40.0, "부서" to "영업팀", "직급" to "과장",
            "연봉" to "8,500,000원", "입사일" to "2017-08-20", "최근 로그인" to "2025-12-08 08:30:00", "재직여부" to true
        ),

        // 17. 날짜가 문자열로 되어있는 경우
        mapOf(
            "이름" to "박현재", "나이" to 32.0, "부서" to "인사팀", "직급" to "대리",
            "연봉" to 5600.0, "입사일" to "2020-06-15", "최근 로그인" to "2025-12-08 09:30:00", "재직여부" to true
        ),

        // 18. Boolean이 문자열로 되어있는 경우
        mapOf(
            "이름" to "정미래", "나이" to 35.0, "부서" to "기획팀", "직급" to "과장",
            "연봉" to 7200.0, "입사일" to "2018-11-05", "최근 로그인" to "2025-12-08 08:15:00", "재직여부" to "TRUE"
        ),

        // 19. 나이가 실수로 되어있는 경우
        mapOf(
            "이름" to "최현실", "나이" to 33.5, "부서" to "디자인팀", "직급" to "대리",
            "연봉" to 5400.0, "입사일" to "2021-04-10", "최근 로그인" to "2025-12-08 09:45:00", "재직여부" to true
        ),

        // 20. 모든 필드가 문자열인 경우
        mapOf(
            "이름" to "한문자", "나이" to "30", "부서" to "개발팀", "직급" to "대리",
            "연봉" to "5500", "입사일" to "2021/03/01", "최근 로그인" to "2025/12/08 09:00:00", "재직여부" to "Yes"
        ),

        // 21. 날짜 형식이 다른 경우 (dd/MM/yyyy)
        mapOf(
            "이름" to "윤형식", "나이" to 31.0, "부서" to "영업팀", "직급" to "대리",
            "연봉" to 5600.0, "입사일" to "20/05/2020", "최근 로그인" to "2025-12-07 17:30:00", "재직여부" to true
        ),

        // 22. 특수문자가 포함된 경우
        mapOf(
            "이름" to "임특수", "나이" to "38세 (만)", "부서" to "개발팀", "직급" to "차장",
            "연봉" to 8200.5, "입사일" to "2016-08-01", "최근 로그인" to "2025-12-08 08:30:00", "재직여부" to true
        ),

        // 23. 날짜가 잘못된 형식인 경우
        mapOf(
            "이름" to "조오류", "나이" to 27.0, "부서" to "기획팀", "직급" to "사원",
            "연봉" to 4500.0, "입사일" to "invalid-date", "최근 로그인" to "2024-01-15 09:00:00", "재직여부" to false
        ),

        // 24. 숫자에 공백이 포함된 경우
        mapOf(
            "이름" to "서공백", "나이" to "  35  ", "부서" to "개발팀", "직급" to "과장",
            "연봉" to "  6500  ", "입사일" to "2019-03-10", "최근 로그인" to "2025-12-08 09:15:00", "재직여부" to true
        ),

        // 25. Boolean이 0/1로 되어있는 경우
        mapOf(
            "이름" to "김숫자", "나이" to 29.0, "부서" to "마케팅팀", "직급" to "사원",
            "연봉" to 4300.0, "입사일" to "2023-07-01", "최근 로그인" to "2025-12-08 08:00:00", "재직여부" to 1
        ),

        // 26. Boolean이 False인 경우 (퇴사자)
        mapOf(
            "이름" to "이퇴사", "나이" to 45.0, "부서" to "개발팀", "직급" to "부장",
            "연봉" to 9500.0, "입사일" to "2010-03-01", "최근 로그인" to "2024-06-30 18:00:00", "재직여부" to false
        ),

        // 27. 음수 나이 (오류 데이터)
        mapOf(
            "이름" to "박음수", "나이" to -5.0, "부서" to "영업팀", "직급" to "사원",
            "연봉" to 4000.0, "입사일" to "2024-01-01", "최근 로그인" to "2025-12-08 09:00:00", "재직여부" to true
        ),

        // 28. 매우 큰 숫자 (연봉)
        mapOf(
            "이름" to "정억만", "나이" to 50.0, "부서" to "임원", "직급" to "사장",
            "연봉" to 999999999.0, "입사일" to "2005-01-01", "최근 로그인" to "2025-12-08 07:00:00", "재직여부" to true
        ),

        // 29. 0 값들
        mapOf(
            "이름" to "최제로", "나이" to 0.0, "부서" to "인턴팀", "직급" to "인턴",
            "연봉" to 0.0, "입사일" to "2025-12-01", "최근 로그인" to "2025-12-08 10:00:00", "재직여부" to true
        ),

        // 30. null과 빈 문자열이 섞인 경우
        mapOf(
            "이름" to "", "나이" to null, "부서" to "", "직급" to null,
            "연봉" to "", "입사일" to null, "최근 로그인" to null, "재직여부" to ""
        )
    )

    data.forEachIndexed { rowIndex, rowData ->
        val row = sheet.createRow(rowIndex + 1)

        headers.forEachIndexed { cellIndex, header ->
            val value = rowData[header]

            if (value != null) {
                val cell = row.createCell(cellIndex)

                when (value) {
                    is String -> {
                        if (value.isEmpty()) {
                            // 빈 문자열은 그대로 저장
                            cell.setCellValue(value)
                        } else if (header == "입사일" && value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            // 날짜 형식이 yyyy-MM-dd인 경우
                            val parts = value.split("-")
                            val date = LocalDateTime.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), 0, 0)
                            cell.setCellValue(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()))
                            cell.cellStyle = dateStyle
                        } else if (header == "최근 로그인" && value.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"))) {
                            // 날짜시간 형식이 yyyy-MM-dd HH:mm:ss인 경우
                            val datePart = value.substring(0, 10).split("-")
                            val timePart = value.substring(11).split(":")
                            val dateTime = LocalDateTime.of(
                                datePart[0].toInt(), datePart[1].toInt(), datePart[2].toInt(),
                                timePart[0].toInt(), timePart[1].toInt(), timePart[2].toInt()
                            )
                            cell.setCellValue(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))
                            cell.cellStyle = dateTimeStyle
                        } else {
                            // 나머지는 문자열로 저장
                            cell.setCellValue(value)
                        }
                    }
                    is Double -> cell.setCellValue(value)
                    is Int -> cell.setCellValue(value.toDouble())
                    is Boolean -> cell.setCellValue(value)
                    else -> cell.setCellValue(value.toString())
                }
            }
            // null인 경우 셀을 생성하지 않음 (null로 유지)
        }
    }

    // 열 너비 자동 조정
    headers.indices.forEach { i ->
        sheet.autoSizeColumn(i)
        sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000)
    }

    // 파일 저장
    val filename = "sample/직원정보_null_edge_cases.xlsx"
    FileOutputStream(filename).use { outputStream ->
        workbook.write(outputStream)
    }

    workbook.close()

    println("✅ null 및 예외 케이스 엑셀 파일이 생성되었습니다: $filename")
    println("\n📋 포함된 Edge Cases:")
    println("\n[NULL 관련 케이스]")
    println("  1. 정상 데이터 (기준)")
    println("  2. 나이만 null")
    println("  3. 부서와 직급이 null")
    println("  4. 연봉만 null")
    println("  5. 입사일만 null")
    println("  6. 최근 로그인만 null")
    println("  7. 재직여부만 null")
    println("  8. 여러 필드가 null (나이, 연봉, 최근 로그인)")
    println("  9. 대부분 필드가 null (이름과 부서만 존재)")
    println(" 10. 모든 필드가 null인 행")
    println(" 11. 첫 번째 필드(이름)만 null")
    println(" 12. 마지막 필드(재직여부)만 null")
    println(" 13. 연속된 null 필드 (부서, 직급, 연봉)")
    println(" 14. 빈 문자열 (null과 구분)")
    println("\n[타입 관련 케이스]")
    println(" 15. 나이가 문자열 ('28세')")
    println(" 16. 연봉이 문자열 ('8,500,000원')")
    println(" 17. 날짜가 문자열 ('2020-06-15')")
    println(" 18. Boolean이 문자열 ('TRUE')")
    println(" 19. 나이가 실수 (33.5)")
    println(" 20. 모든 필드가 문자열")
    println("\n[형식 관련 케이스]")
    println(" 21. 날짜 형식이 다름 ('20/05/2020')")
    println(" 22. 특수문자 포함 ('38세 (만)')")
    println(" 23. 잘못된 날짜 형식 ('invalid-date')")
    println(" 24. 공백 포함 숫자 ('  35  ', '  6500  ')")
    println(" 25. Boolean이 0/1로 되어있는 경우")
    println(" 26. Boolean이 False인 경우 (퇴사자)")
    println("\n[경계값 케이스]")
    println(" 27. 음수 나이 (-5)")
    println(" 28. 매우 큰 숫자 (999999999)")
    println(" 29. 0 값들")
    println(" 30. null과 빈 문자열이 섞인 경우")
    println("\n총 30개의 테스트 케이스")
}
