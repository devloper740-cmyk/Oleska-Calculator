package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class CalculationHistory(
  val id: Long = System.currentTimeMillis(),
  val expression: String,
  val result: String,
  val timestamp: Long = System.currentTimeMillis()
)

enum class Operator(val symbol: String, val displaySymbol: String) {
  ADD("+", "+"),
  SUBTRACT("-", "−"),
  MULTIPLY("*", "×"),
  DIVIDE("/", "÷")
}

data class CalculatorState(
  val displayValue: String = "0",
  val expression: String = "",
  val memoryValue: BigDecimal = BigDecimal.ZERO,
  val isMemoryActive: Boolean = false,
  val activeOperator: Operator? = null,
  val isNewNumber: Boolean = true,
  val errorMessage: String? = null,
  val history: List<CalculationHistory> = emptyList()
)

class CalculatorEngine {
  private var accumulatedValue: BigDecimal? = null
  private var pendingOperator: Operator? = null
  private var memory: BigDecimal = BigDecimal.ZERO
  private var isNewInput: Boolean = true
  private var expressionHistory: MutableList<CalculationHistory> = mutableListOf()
  private var currentExpression: String = ""

  private val mathContext = MathContext(12, RoundingMode.HALF_UP)
  private val formatter = DecimalFormat("#,##0.##########", DecimalFormatSymbols(Locale.US)).apply {
    isGroupingUsed = true
    maximumFractionDigits = 10
  }

  fun getCurrentState(displayRaw: String): CalculatorState {
    return CalculatorState(
      displayValue = formatDisplay(displayRaw),
      expression = currentExpression,
      memoryValue = memory,
      isMemoryActive = memory.compareTo(BigDecimal.ZERO) != 0,
      activeOperator = pendingOperator,
      isNewNumber = isNewInput,
      errorMessage = null,
      history = expressionHistory.toList()
    )
  }

  fun onDigit(currentDisplay: String, digit: String): String {
    val raw = parseRaw(currentDisplay)
    return if (isNewInput || raw == "0" || raw == "Error") {
      isNewInput = false
      digit
    } else {
      if (raw.replace("-", "").replace(".", "").length >= 14) {
        raw // Limit max input digits to 14
      } else {
        raw + digit
      }
    }
  }

  fun onDecimal(currentDisplay: String): String {
    val raw = parseRaw(currentDisplay)
    return if (isNewInput || raw == "0" || raw == "Error") {
      isNewInput = false
      "0."
    } else if (!raw.contains(".")) {
      "$raw."
    } else {
      raw
    }
  }

  fun onToggleSign(currentDisplay: String): String {
    val raw = parseRaw(currentDisplay)
    if (raw == "0" || raw == "Error") return raw
    return if (raw.startsWith("-")) {
      raw.substring(1)
    } else {
      "-$raw"
    }
  }

  fun onBackspace(currentDisplay: String): String {
    val raw = parseRaw(currentDisplay)
    if (raw == "Error" || raw.length <= 1) {
      isNewInput = true
      return "0"
    }
    val shortened = raw.dropLast(1)
    if (shortened == "-" || shortened.isEmpty()) {
      isNewInput = true
      return "0"
    }
    isNewInput = false
    return shortened
  }

  fun onOperator(currentDisplay: String, operator: Operator): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)

    if (accumulatedValue != null && pendingOperator != null && !isNewInput) {
      // Evaluate previous pending operation
      val result = evaluate(accumulatedValue!!, currentValue, pendingOperator!!)
      accumulatedValue = result
      currentExpression = "${formatBigDecimal(result)} ${operator.displaySymbol}"
      pendingOperator = operator
      isNewInput = true
      return Pair(formatBigDecimal(result), currentExpression)
    } else {
      accumulatedValue = currentValue
      pendingOperator = operator
      currentExpression = "${formatBigDecimal(currentValue)} ${operator.displaySymbol}"
      isNewInput = true
      return Pair(currentDisplay, currentExpression)
    }
  }

  fun onEquals(currentDisplay: String): Pair<String, String>? {
    if (accumulatedValue == null || pendingOperator == null) {
      return null
    }

    val raw = parseRaw(currentDisplay)
    val secondOperand = parseBigDecimal(raw)
    val operator = pendingOperator!!
    val firstOperand = accumulatedValue!!

    return try {
      val result = evaluate(firstOperand, secondOperand, operator)
      val fullExpr = "${formatBigDecimal(firstOperand)} ${operator.displaySymbol} ${formatBigDecimal(secondOperand)} ="
      val resultFormatted = formatBigDecimal(result)

      // Save to history
      expressionHistory.add(
        0,
        CalculationHistory(
          expression = "${formatBigDecimal(firstOperand)} ${operator.displaySymbol} ${formatBigDecimal(secondOperand)}",
          result = resultFormatted
        )
      )

      accumulatedValue = null
      pendingOperator = null
      currentExpression = fullExpr
      isNewInput = true

      Pair(resultFormatted, fullExpr)
    } catch (e: ArithmeticException) {
      accumulatedValue = null
      pendingOperator = null
      currentExpression = "Cannot divide by 0"
      isNewInput = true
      Pair("Error", currentExpression)
    }
  }

  fun onPercentage(currentDisplay: String): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)

    val result = if (accumulatedValue != null && pendingOperator != null) {
      // E.g. 200 + 10% -> 10% of 200 = 20
      val percentValue = accumulatedValue!!.multiply(currentValue, mathContext).divide(BigDecimal("100"), mathContext)
      percentValue
    } else {
      // Direct percentage: 50% = 0.5
      currentValue.divide(BigDecimal("100"), mathContext)
    }

    val resultFormatted = formatBigDecimal(result)
    isNewInput = true
    return Pair(resultFormatted, currentExpression)
  }

  fun onSin(currentDisplay: String, isRad: Boolean = false): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)
    val doubleVal = currentValue.toDouble()
    val angle = if (isRad) doubleVal else Math.toRadians(doubleVal)
    val sinVal = kotlin.math.sin(angle)
    val cleanSin = if (kotlin.math.abs(sinVal) < 1e-14) 0.0 else sinVal
    val result = BigDecimal(cleanSin, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "sin(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onCos(currentDisplay: String, isRad: Boolean = false): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)
    val doubleVal = currentValue.toDouble()
    val angle = if (isRad) doubleVal else Math.toRadians(doubleVal)
    val cosVal = kotlin.math.cos(angle)
    val cleanCos = if (kotlin.math.abs(cosVal) < 1e-14) 0.0 else cosVal
    val result = BigDecimal(cleanCos, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "cos(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onTan(currentDisplay: String, isRad: Boolean = false): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)
    val doubleVal = currentValue.toDouble()

    if (!isRad) {
      val normalized = (doubleVal % 180 + 180) % 180
      if (kotlin.math.abs(normalized - 90.0) < 1e-6) {
        isNewInput = true
        return Pair("Error", "Undefined")
      }
    }

    val angle = if (isRad) doubleVal else Math.toRadians(doubleVal)
    val tanVal = kotlin.math.tan(angle)
    if (tanVal.isInfinite() || tanVal.isNaN()) {
      isNewInput = true
      return Pair("Error", "Undefined")
    }

    val cleanTan = if (kotlin.math.abs(tanVal) < 1e-14) 0.0 else tanVal
    val result = BigDecimal(cleanTan, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "tan(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onLog(currentDisplay: String): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)
    val doubleVal = currentValue.toDouble()

    if (doubleVal <= 0.0) {
      isNewInput = true
      return Pair("Error", "Invalid Input")
    }

    val logVal = kotlin.math.log10(doubleVal)
    val result = BigDecimal(logVal, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "log(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onLn(currentDisplay: String): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)
    val doubleVal = currentValue.toDouble()

    if (doubleVal <= 0.0) {
      isNewInput = true
      return Pair("Error", "Invalid Input")
    }

    val lnVal = kotlin.math.ln(doubleVal)
    val result = BigDecimal(lnVal, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "ln(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onSquare(currentDisplay: String): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)
    val result = currentValue.multiply(currentValue, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "sqr(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onReciprocal(currentDisplay: String): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)

    if (currentValue.compareTo(BigDecimal.ZERO) == 0) {
      isNewInput = true
      return Pair("Error", "Cannot divide by 0")
    }

    val result = BigDecimal.ONE.divide(currentValue, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "1/(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))
    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onPi(): String {
    isNewInput = true
    return formatBigDecimal(BigDecimal(Math.PI, mathContext))
  }

  fun onEuler(): String {
    isNewInput = true
    return formatBigDecimal(BigDecimal(Math.E, mathContext))
  }

  fun onSquareRoot(currentDisplay: String): Pair<String, String> {
    val raw = parseRaw(currentDisplay)
    val currentValue = parseBigDecimal(raw)

    if (currentValue < BigDecimal.ZERO) {
      isNewInput = true
      return Pair("Error", "Invalid Input")
    }

    val doubleVal = currentValue.toDouble()
    val sqrtVal = kotlin.math.sqrt(doubleVal)
    val result = BigDecimal(sqrtVal, mathContext)
    val formatted = formatBigDecimal(result)
    val expr = "√(${formatBigDecimal(currentValue)})"

    expressionHistory.add(0, CalculationHistory(expression = expr, result = formatted))

    currentExpression = "$expr ="
    isNewInput = true
    return Pair(formatted, currentExpression)
  }

  fun onClear(): String {
    isNewInput = true
    return "0"
  }

  fun onAllClear(): String {
    accumulatedValue = null
    pendingOperator = null
    currentExpression = ""
    isNewInput = true
    return "0"
  }

  // Memory functions
  fun onMemoryAdd(currentDisplay: String): BigDecimal {
    val raw = parseRaw(currentDisplay)
    val current = parseBigDecimal(raw)
    memory = memory.add(current, mathContext)
    isNewInput = true
    return memory
  }

  fun onMemorySubtract(currentDisplay: String): BigDecimal {
    val raw = parseRaw(currentDisplay)
    val current = parseBigDecimal(raw)
    memory = memory.subtract(current, mathContext)
    isNewInput = true
    return memory
  }

  fun onMemoryRecall(): String {
    isNewInput = true
    return formatBigDecimal(memory)
  }

  fun onMemoryClear(): BigDecimal {
    memory = BigDecimal.ZERO
    return memory
  }

  fun clearHistory() {
    expressionHistory.clear()
  }

  fun restoreHistory(history: CalculationHistory): Pair<String, String> {
    accumulatedValue = null
    pendingOperator = null
    currentExpression = "${history.expression} ="
    isNewInput = true
    return Pair(history.result, currentExpression)
  }

  private fun evaluate(a: BigDecimal, b: BigDecimal, op: Operator): BigDecimal {
    return when (op) {
      Operator.ADD -> a.add(b, mathContext)
      Operator.SUBTRACT -> a.subtract(b, mathContext)
      Operator.MULTIPLY -> a.multiply(b, mathContext)
      Operator.DIVIDE -> {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
          throw ArithmeticException("Division by zero")
        }
        a.divide(b, mathContext)
      }
    }
  }

  private fun parseBigDecimal(valueStr: String): BigDecimal {
    return try {
      val clean = valueStr.replace(",", "")
      if (clean.isEmpty() || clean == "-") BigDecimal.ZERO else BigDecimal(clean)
    } catch (e: Exception) {
      BigDecimal.ZERO
    }
  }

  private fun parseRaw(formatted: String): String {
    return formatted.replace(",", "")
  }

  fun formatDisplay(raw: String): String {
    if (raw == "Error" || raw.isEmpty()) return raw
    if (raw.endsWith(".")) {
      val withoutDot = raw.dropLast(1)
      val formattedNum = try {
        val bd = BigDecimal(withoutDot)
        formatter.format(bd)
      } catch (e: Exception) {
        withoutDot
      }
      return "$formattedNum."
    }
    if (raw.contains(".")) {
      val parts = raw.split(".")
      val integerPart = try {
        val bd = BigDecimal(parts[0])
        formatter.format(bd)
      } catch (e: Exception) {
        parts[0]
      }
      return "$integerPart.${parts[1]}"
    }
    return try {
      val bd = BigDecimal(raw)
      formatter.format(bd)
    } catch (e: Exception) {
      raw
    }
  }

  private fun formatBigDecimal(bd: BigDecimal): String {
    val stripped = bd.stripTrailingZeros()
    return formatter.format(stripped)
  }
}
