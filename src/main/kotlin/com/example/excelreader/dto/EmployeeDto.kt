package com.example.excelreader.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class EmployeeDto(
    val name: String?,
    val age: Int?,
    val department: String?,
    val position: String?,
    val salary: Double?,
    val hireDate: LocalDate?,
    val lastLogin: LocalDateTime?,
    val isActive: Boolean?
)
