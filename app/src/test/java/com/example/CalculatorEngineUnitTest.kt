package com.example

import com.example.calculator.CalculatorEngine
import com.example.calculator.Operator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineUnitTest {

  private lateinit var engine: CalculatorEngine

  @Before
  fun setUp() {
    engine = CalculatorEngine()
  }

  @Test
  fun testBasicAddition() {
    val d1 = engine.onDigit("0", "1")
    val d2 = engine.onDigit(d1, "5")
    val (opDisplay, _) = engine.onOperator(d2, Operator.ADD)
    val d3 = engine.onDigit(opDisplay, "2")
    val d4 = engine.onDigit(d3, "7")
    val result = engine.onEquals(d4)
    assertNotNull(result)
    assertEquals("42", result?.first)
  }

  @Test
  fun testBasicSubtraction() {
    val d1 = engine.onDigit("0", "1")
    val d2 = engine.onDigit(d1, "0")
    val d3 = engine.onDigit(d2, "0")
    val (opDisplay, _) = engine.onOperator(d3, Operator.SUBTRACT)
    val d4 = engine.onDigit(opDisplay, "3")
    val d5 = engine.onDigit(d4, "5")
    val result = engine.onEquals(d5)
    assertNotNull(result)
    assertEquals("65", result?.first)
  }

  @Test
  fun testBasicMultiplication() {
    val d1 = engine.onDigit("0", "8")
    val (opDisplay, _) = engine.onOperator(d1, Operator.MULTIPLY)
    val d2 = engine.onDigit(opDisplay, "9")
    val result = engine.onEquals(d2)
    assertNotNull(result)
    assertEquals("72", result?.first)
  }

  @Test
  fun testBasicDivision() {
    val d1 = engine.onDigit("0", "1")
    val d2 = engine.onDigit(d1, "4")
    val d3 = engine.onDigit(d2, "4")
    val (opDisplay, _) = engine.onOperator(d3, Operator.DIVIDE)
    val d4 = engine.onDigit(opDisplay, "1")
    val d5 = engine.onDigit(d4, "2")
    val result = engine.onEquals(d5)
    assertNotNull(result)
    assertEquals("12", result?.first)
  }

  @Test
  fun testDivisionByZero() {
    val d1 = engine.onDigit("0", "9")
    val (opDisplay, _) = engine.onOperator(d1, Operator.DIVIDE)
    val d2 = engine.onDigit(opDisplay, "0")
    val result = engine.onEquals(d2)
    assertNotNull(result)
    assertEquals("Error", result?.first)
    assertEquals("Cannot divide by 0", result?.second)
  }

  @Test
  fun testDecimals() {
    val d1 = engine.onDecimal("0")
    assertEquals("0.", d1)
    val d2 = engine.onDigit(d1, "5")
    assertEquals("0.5", d2)
    val d3 = engine.onDecimal(d2) // duplicate decimal should be ignored
    assertEquals("0.5", d3)
  }

  @Test
  fun testToggleSign() {
    assertEquals("-5", engine.onToggleSign("5"))
    assertEquals("5", engine.onToggleSign("-5"))
    assertEquals("0", engine.onToggleSign("0"))
    assertEquals("Error", engine.onToggleSign("Error"))
  }

  @Test
  fun testBackspace() {
    assertEquals("12", engine.onBackspace("123"))
    assertEquals("1", engine.onBackspace("12"))
    assertEquals("0", engine.onBackspace("1"))
    assertEquals("0", engine.onBackspace("0"))
  }

  @Test
  fun testScientificFunctions() {
    // Trigonometry Degrees
    assertEquals("1", engine.onSin("90", isRad = false).first)
    assertEquals("1", engine.onCos("0", isRad = false).first)
    assertEquals("1", engine.onTan("45", isRad = false).first)

    // Trigonometry Radians
    assertEquals("0", engine.onSin("0", isRad = true).first)

    // Logarithms
    assertEquals("3", engine.onLog("1000").first)
    assertEquals("Error", engine.onLog("-5").first)
    assertEquals("Error", engine.onLog("0").first)

    // Square and Square Root
    assertEquals("49", engine.onSquare("7").first)
    assertEquals("9", engine.onSquareRoot("81").first)
    assertEquals("Error", engine.onSquareRoot("-4").first)

    // Reciprocal
    assertEquals("0.2", engine.onReciprocal("5").first)
    assertEquals("Error", engine.onReciprocal("0").first)

    // Constants
    assertTrue(engine.onPi().startsWith("3.14"))
    assertTrue(engine.onEuler().startsWith("2.71"))
  }

  @Test
  fun testMemoryOperations() {
    assertEquals(BigDecimal("10"), engine.onMemoryAdd("10"))
    assertEquals("10", engine.onMemoryRecall())
    assertEquals(BigDecimal("15"), engine.onMemoryAdd("5"))
    assertEquals("15", engine.onMemoryRecall())
    assertEquals(BigDecimal("7"), engine.onMemorySubtract("8"))
    assertEquals("7", engine.onMemoryRecall())
    assertEquals(BigDecimal.ZERO, engine.onMemoryClear())
    assertEquals("0", engine.onMemoryRecall())
  }
}
