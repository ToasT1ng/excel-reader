# Excel Reader

엑셀 파일을 업로드하여 내용을 화면에 표시하는 웹 애플리케이션입니다.

유연한 타입 변환과 다양한 edge case 처리를 지원하여 실무에서 발생할 수 있는 다양한 엑셀 형식을 안정적으로 읽을 수 있습니다.

## 기술 스택

### 백엔드
- Spring Boot 3.2.0
- Kotlin 1.9.20
- Apache POI 5.2.5
- JUnit 5

### 프론트엔드
- HTML/CSS/JavaScript (Vanilla)

## 실행 방법

### 1. 백엔드 실행

```bash
./gradlew bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다.

### 2. 프론트엔드 실행

브라우저에서 `frontend/index.html` 파일을 직접 열거나, 간단한 HTTP 서버를 사용합니다:

```bash
cd frontend
python3 -m http.server 3000
```

브라우저에서 `http://localhost:3000`으로 접속합니다.

## 주요 기능

### 기본 기능
- 엑셀 파일(.xlsx, .xls) 업로드
- 시트 인덱스 또는 이름으로 데이터 읽기
- 헤더 행(첫 번째 행)을 컬럼명으로 사용
- 데이터를 테이블 형태로 화면에 표시
- Kotlin data class로 자동 매핑

### 타입 변환 (Type Conversion)
ExcelReader는 다양한 타입 변환을 지원합니다:

**숫자 변환 (String → Int/Long/Double)**
- `"28세"` → `28`
- `"7,000원"` → `7000.0`
- `"  35  "` (공백 포함) → `35`
- 음수 처리: `"-5"` → `-5`

**Boolean 변환**
- String: `"Yes"`, `"True"`, `"Y"`, `"1"` → `true`
- String: `"No"`, `"False"`, `"N"`, `"0"` → `false`
- 숫자: `1.0` → `true`, `0.0` → `false`

**날짜/시간 변환**
- 다양한 날짜 형식 자동 파싱
  - `2020-03-15`, `2020/03/15`, `20/05/2020`
  - `2025-12-08 09:30:00`, `2025/12/08 09:30:00`
- LocalDate, LocalDateTime 지원

### Edge Case 처리
- **null 값 처리**: 빈 셀을 null로 안전하게 처리
- **빈 행 건너뛰기**: 모든 셀이 비어있는 행 자동 제외
- **수식(Formula) 자동 계산**: 수식 셀의 결과값 추출
- **잘못된 형식 처리**: 변환 불가능한 값은 null 반환
- **혼합 타입 처리**: 숫자와 문자열이 섞인 컬럼 안전하게 처리

## API 엔드포인트

### POST /api/excel/upload

엑셀 파일을 업로드하고 데이터를 반환합니다.

**Request**
- Content-Type: multipart/form-data
- Parameter: file (엑셀 파일)

**Response**
```json
{
  "success": true,
  "message": "엑셀 파일을 성공적으로 읽었습니다.",
  "data": [
    {
      "Column1": "value1",
      "Column2": "value2"
    }
  ]
}
```

## ExcelReader 사용 방법

### 기본 사용법

```kotlin
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

// 시트 인덱스로 읽기
val employees = ExcelReader.readExcel(
    file = uploadedFile,
    kClass = Employee::class,
    sheetIndex = 0,
    skipRows = 1  // 헤더 건너뛰기
)

// 시트 이름으로 읽기
val employees = ExcelReader.readExcelBySheetName(
    file = uploadedFile,
    kClass = Employee::class,
    sheetName = "직원정보"
)

// 시트 이름 목록 가져오기
val sheetNames = ExcelReader.getSheetNames(uploadedFile)
```

### Nullable 타입 권장
실무 엑셀 파일은 빈 셀이나 잘못된 형식이 포함될 수 있으므로, data class의 모든 필드를 nullable(`?`)로 선언하는 것을 권장합니다.

```kotlin
// ✅ 권장: Nullable 타입
data class Employee(
    val name: String?,
    val age: Int?
)

// ❌ 비권장: Non-nullable 타입
data class Employee(
    val name: String,  // null이 들어오면 에러
    val age: Int       // 변환 실패하면 에러
)
```

## 테스트

프로젝트는 12개의 유닛 테스트를 포함하고 있으며, 모든 edge case를 커버합니다.

```bash
# 전체 테스트 실행
./gradlew test

# ExcelReader 테스트만 실행
./gradlew test --tests "com.example.excelreader.util.ExcelReaderTest"
```

**테스트 커버리지:**
- 정상 데이터 읽기
- null 값 처리
- 문자열 타입 변환
- Boolean 타입 변환 (10가지 형식)
- 음수 및 큰 숫자 처리
- 빈 행 건너뛰기
- 다양한 날짜 형식 파싱
- 잘못된 날짜 처리
- 시트 이름/인덱스 읽기
- 예외 처리

## 테스트 데이터

프로젝트는 테스트용 엑셀 파일과 생성 스크립트를 포함합니다:

### 샘플 파일
- `sample/직원정보.xlsx` - 정상 데이터
- `sample/직원정보_예외케이스.xlsx` - 12가지 예외 케이스
- `sample/직원정보_null_edge_cases.xlsx` - 30가지 null 및 edge case

### 생성 스크립트
- `CreateEdgeCaseExcel.kt` - Kotlin으로 테스트 파일 생성
- `sample/create_edge_cases.py` - Python으로 테스트 파일 생성

```bash
# Kotlin 스크립트로 생성
./gradlew createEdgeCaseExcel

# Python 스크립트로 생성
python3 sample/create_edge_cases.py
```
