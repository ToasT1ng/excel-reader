package com.example.excelreader.controller

import com.example.excelreader.dto.EmployeeDto
import com.example.excelreader.util.ExcelReader
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/excel")
class ExcelController {

    @PostMapping("/upload")
    fun uploadExcel(@RequestParam("file") file: MultipartFile): ResponseEntity<ExcelResponse> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(
                ExcelResponse(success = false, message = "파일이 비어있습니다.", data = emptyList())
            )
        }

        try {
            val employees = ExcelReader.readExcel(
                file = file,
                kClass = EmployeeDto::class,
                sheetIndex = 0,
                skipRows = 1,
                skipEmptyRows = true
            )

            return ResponseEntity.ok(
                ExcelResponse(
                    success = true,
                    message = "엑셀 파일을 성공적으로 읽었습니다. (총 ${employees.size}건)",
                    data = employees
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.badRequest().body(
                ExcelResponse(
                    success = false,
                    message = "엑셀 파일 처리 중 오류가 발생했습니다: ${e.message}",
                    data = emptyList()
                )
            )
        }
    }

}

data class ExcelResponse(
    val success: Boolean,
    val message: String,
    val data: List<EmployeeDto>
)
