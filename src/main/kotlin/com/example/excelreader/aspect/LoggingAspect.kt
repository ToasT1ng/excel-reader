package com.example.excelreader.aspect

import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Aspect
@Component
class LoggingAspect(
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    fun logRestControllerMethods(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val methodName = signature.method.name
        val className = joinPoint.target.javaClass.simpleName

        val args = joinPoint.args
        val parameterNames = signature.parameterNames

        logger.info("=" .repeat(80))
        logger.info(">>> [REQUEST] $className.$methodName()")
        logger.info("=" .repeat(80))

        // 요청 파라미터 로깅
        args.forEachIndexed { index, arg ->
            val paramName = parameterNames.getOrNull(index) ?: "arg$index"

            when (arg) {
                is MultipartFile -> {
                    logger.info("  - $paramName: [MultipartFile]")
                    logger.info("    └─ fileName: ${arg.originalFilename}")
                    logger.info("    └─ size: ${arg.size} bytes")
                    logger.info("    └─ contentType: ${arg.contentType}")
                }
                else -> {
                    try {
                        val argJson = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(arg)
                        logger.info("  - $paramName: $argJson")
                    } catch (e: Exception) {
                        logger.info("  - $paramName: ${arg?.toString() ?: "null"}")
                    }
                }
            }
        }

        val startTime = System.currentTimeMillis()

        return try {
            val result = joinPoint.proceed()
            val elapsedTime = System.currentTimeMillis() - startTime

            logger.info("-" .repeat(80))
            logger.info("<<< [RESPONSE] $className.$methodName() - ${elapsedTime}ms")
            logger.info("-" .repeat(80))

            // 응답 로깅
            try {
                val responseJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result)
                logger.info("Response Body:\n$responseJson")
            } catch (e: Exception) {
                logger.info("Response: ${result?.toString() ?: "null"}")
            }

            logger.info("=" .repeat(80))

            result
        } catch (e: Exception) {
            val elapsedTime = System.currentTimeMillis() - startTime

            logger.error("-" .repeat(80))
            logger.error("<<< [ERROR] $className.$methodName() - ${elapsedTime}ms")
            logger.error("-" .repeat(80))
            logger.error("Exception: ${e.javaClass.simpleName}")
            logger.error("Message: ${e.message}")
            logger.error("StackTrace:", e)
            logger.error("=" .repeat(80))

            throw e
        }
    }
}
