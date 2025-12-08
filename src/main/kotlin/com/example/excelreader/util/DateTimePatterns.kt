package com.example.excelreader.util

object DateTimePatterns {
    // 엄격한 패턴 (2자리 고정) - 표준 형식
    const val DATETIME_PATTERN_1 = "yyyy-MM-dd HH:mm:ss"
    const val DATETIME_PATTERN_2 = "yyyy-MM-dd HH:mm"
    const val DATETIME_PATTERN_3 = "yyyy/MM/dd HH:mm:ss"
    const val DATETIME_PATTERN_4 = "yyyy/MM/dd HH:mm"

    // 유연한 패턴 (1~2자리) - 사용자 입력 대응
    const val DATETIME_PATTERN_5 = "yyyy-M-d H:m:s"
    const val DATETIME_PATTERN_6 = "yyyy-M-d H:m"
    const val DATETIME_PATTERN_7 = "yyyy/M/d H:m:s"
    const val DATETIME_PATTERN_8 = "yyyy/M/d H:m"

    const val DATE_PATTERN_1 = "yyyy-MM-dd"
    const val DATE_PATTERN_2 = "yyyy/MM/dd"
    const val DATE_PATTERN_3 = "dd-MM-yyyy"
    const val DATE_PATTERN_4 = "dd/MM/yyyy"

    const val DATE_PATTERN_5 = "yyyy-M-d"
    const val DATE_PATTERN_6 = "yyyy/M/d"
    const val DATE_PATTERN_7 = "d-M-yyyy"
    const val DATE_PATTERN_8 = "d/M/yyyy"
}
