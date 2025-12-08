# Excel Reader

엑셀 파일을 업로드하여 내용을 화면에 표시하는 간단한 웹 애플리케이션입니다.

## 기술 스택

### 백엔드
- Spring Boot 3.2.0
- Kotlin 1.9.20
- Apache POI 5.2.5

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

## 기능

- 엑셀 파일(.xlsx, .xls) 업로드
- 첫 번째 시트의 데이터를 자동으로 읽음
- 헤더 행(첫 번째 행)을 컬럼명으로 사용
- 데이터를 테이블 형태로 화면에 표시
- 다양한 날짜/시간 형식 지원
- 수식(Formula) 셀 자동 계산

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

## ExcelReader 유틸리티

제공된 ExcelReader 유틸리티는 다음 기능을 지원합니다:

- 시트 인덱스 또는 이름으로 데이터 읽기
- Kotlin data class로 자동 매핑
- 다양한 날짜/시간 형식 파싱
- 수식 셀 평가
- 빈 행 건너뛰기

사용 예시:

```kotlin
data class Employee(
    val name: String,
    val age: Int,
    val hireDate: LocalDate
)

val employees = ExcelReader.readExcel(
    file = uploadedFile,
    kClass = Employee::class,
    sheetIndex = 0,
    skipRows = 1
)
```
