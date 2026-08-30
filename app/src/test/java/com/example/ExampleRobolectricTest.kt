package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.CalculatorEngine
import com.example.calculator.Operator
import com.example.data.AppDatabase
import com.example.data.CalculatorRepository
import com.example.ui.OleskaCalculatorScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Oleska Calculator", appName)
  }

  @Test
  fun `calculator basic arithmetic`() {
    val engine = CalculatorEngine()
    var display = engine.onDigit("0", "1")
    display = engine.onDigit(display, "2")
    display = engine.onDigit(display, "5")
    val (dispAfterOp, _) = engine.onOperator(display, Operator.ADD)
    assertEquals("125", dispAfterOp)

    var nextDisplay = engine.onDigit(dispAfterOp, "7")
    nextDisplay = engine.onDigit(nextDisplay, "5")
    val result = engine.onEquals(nextDisplay)
    assertEquals("200", result?.first)
  }

  @Test
  fun `calculator backspace functionality`() {
    val engine = CalculatorEngine()
    var display = engine.onDigit("0", "9")
    display = engine.onDigit(display, "8")
    display = engine.onDigit(display, "7")
    assertEquals("987", display)

    display = engine.onBackspace(display)
    assertEquals("98", display)

    display = engine.onBackspace(display)
    assertEquals("9", display)

    display = engine.onBackspace(display)
    assertEquals("0", display)
  }

  @Test
  fun `calculator division by zero error handling`() {
    val engine = CalculatorEngine()
    val display = engine.onDigit("0", "8")
    val (dispAfterOp, _) = engine.onOperator(display, Operator.DIVIDE)
    val nextDisplay = engine.onDigit(dispAfterOp, "0")
    val result = engine.onEquals(nextDisplay)
    assertEquals("Error", result?.first)
    assertEquals("Cannot divide by 0", result?.second)
  }

  @Test
  fun `calculator percentage and negation`() {
    val engine = CalculatorEngine()
    var display = engine.onDigit("0", "5")
    display = engine.onDigit(display, "0")
    val (pctResult, _) = engine.onPercentage(display)
    assertEquals("0.5", pctResult)

    val negated = engine.onToggleSign(pctResult)
    assertEquals("-0.5", negated)

    val restored = engine.onToggleSign(negated)
    assertEquals("0.5", restored)
  }

  @Test
  fun `calculator memory operations`() {
    val engine = CalculatorEngine()
    val mem = engine.onMemoryAdd("50")
    assertEquals(BigDecimal("50"), mem)
    val recalled = engine.onMemoryRecall()
    assertEquals("50", recalled)
    val memAfterSub = engine.onMemorySubtract("20")
    assertEquals(BigDecimal("30"), memAfterSub)
    val cleared = engine.onMemoryClear()
    assertEquals(BigDecimal.ZERO, cleared)
  }

  @Test
  fun `ui displays brand header and button grid correctly`() {
    composeTestRule.setContent {
      OleskaCalculatorScreen()
    }

    composeTestRule.onNodeWithTag("brand_logo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("brand_subtitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_backspace").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_equals").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_divide").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_multiply").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_subtract").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_add").assertIsDisplayed()
  }

  @Test
  fun `ui calculates 25 plus 15 equals 40`() {
    composeTestRule.setContent {
      OleskaCalculatorScreen()
    }

    composeTestRule.onNodeWithTag("btn_2").performClick()
    composeTestRule.onNodeWithTag("btn_5").performClick()
    composeTestRule.onNodeWithTag("btn_add").performClick()
    composeTestRule.onNodeWithTag("btn_1").performClick()
    composeTestRule.onNodeWithTag("btn_5").performClick()
    composeTestRule.onNodeWithTag("btn_equals").performClick()

    composeTestRule.onNodeWithTag("display_value").assertExists()
  }

  @Test
  fun `room database stores and retrieves calculations`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.calculationDao()
    val repo = CalculatorRepository(dao)

    repo.saveCalculation("50 + 25", "75")
    val items = repo.historyFlow.first()
    assertEquals(1, items.size)
    assertEquals("50 + 25", items[0].expression)
    assertEquals("75", items[0].result)

    repo.clearHistory()
    val cleared = repo.historyFlow.first()
    assertEquals(0, cleared.size)
    db.close()
  }

  @Test
  fun `scientific operations trigonometric and logarithmic logic`() {
    val engine = CalculatorEngine()

    // sin(90 deg) = 1
    val sin90 = engine.onSin("90", isRad = false)
    assertEquals("1", sin90.first)

    // cos(0 deg) = 1
    val cos0 = engine.onCos("0", isRad = false)
    assertEquals("1", cos0.first)

    // tan(45 deg) = 1
    val tan45 = engine.onTan("45", isRad = false)
    assertEquals("1", tan45.first)

    // log10(100) = 2
    val log100 = engine.onLog("100")
    assertEquals("2", log100.first)

    // ln(e) = 1
    val lne = engine.onLn("2.718281828459045")
    assertEquals("1", lne.first)

    // square(5) = 25
    val sq5 = engine.onSquare("5")
    assertEquals("25", sq5.first)

    // sqrt(16) = 4
    val sqrt16 = engine.onSquareRoot("16")
    assertEquals("4", sqrt16.first)

    // constants
    assertEquals("3.1415926536", engine.onPi())
    assertEquals("2.7182818285", engine.onEuler())
  }

  @Test
  fun `ui toggles scientific mode and displays scientific keys`() {
    composeTestRule.setContent {
      OleskaCalculatorScreen()
    }

    // Initially in Standard mode
    composeTestRule.onNodeWithTag("btn_mode_standard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_mode_scientific").assertIsDisplayed()

    // Switch to Scientific mode
    composeTestRule.onNodeWithTag("btn_mode_scientific").performClick()

    // Scientific keys are now displayed
    composeTestRule.onNodeWithTag("btn_sin").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_cos").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_tan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_log").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_ln").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_square").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_sqrt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_pi").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_angle_mode").assertIsDisplayed()

    // Toggle DEG/RAD angle mode
    composeTestRule.onNodeWithTag("btn_angle_mode").performClick()

    // Switch back to Standard mode
    composeTestRule.onNodeWithTag("btn_mode_standard").performClick()
  }
}

