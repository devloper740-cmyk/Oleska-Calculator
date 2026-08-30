package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.CalculatorViewModel
import com.example.calculator.Operator
import com.example.data.AppDatabase
import com.example.data.CalculatorRepository
import com.example.ui.components.ButtonType
import com.example.ui.components.OleskaButton
import com.example.ui.components.OleskaDisplay
import com.example.ui.components.OleskaHeader
import com.example.ui.components.OleskaHistorySheet
import com.example.ui.theme.LocalOleskaPalette
import com.example.ui.theme.OleskaSilverMist
import com.example.ui.theme.OleskaSteelGrey
import com.example.ui.theme.OleskaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OleskaCalculatorScreen(
  context: Context = LocalContext.current,
  viewModel: CalculatorViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
          val db = AppDatabase.getDatabase(context)
          val repo = CalculatorRepository(db.calculationDao())
          CalculatorViewModel(repo) as T
        } catch (_: Throwable) {
          CalculatorViewModel() as T
        }
      }
    }
  )
) {
  val uiState by viewModel.uiState.collectAsState()
  val isDarkMode by viewModel.isDarkMode.collectAsState()
  val isScientificMode by viewModel.isScientificMode.collectAsState()
  val isRadMode by viewModel.isRadMode.collectAsState()
  val showHistorySheet by viewModel.showHistorySheet.collectAsState()
  val toastMessage by viewModel.copiedToastMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val haptic = LocalHapticFeedback.current

  LaunchedEffect(toastMessage) {
    if (toastMessage != null) {
      coroutineScope.launch {
        snackbarHostState.showSnackbar(toastMessage!!)
        viewModel.clearToastMessage()
      }
    }
  }

  OleskaTheme(darkTheme = isDarkMode) {
    val palette = LocalOleskaPalette.current

    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("oleska_scaffold"),
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      snackbarHost = {
        SnackbarHost(
          hostState = snackbarHostState,
          modifier = Modifier.navigationBarsPadding()
        )
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .statusBarsPadding()
          .navigationBarsPadding()
          .testTag("calculator_container"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // 1. Brand Header
        OleskaHeader(
          isDarkMode = isDarkMode,
          isMemoryActive = uiState.isMemoryActive,
          historyCount = uiState.history.size,
          isScientificMode = isScientificMode,
          onToggleTheme = { viewModel.toggleTheme() },
          onToggleScientificMode = { viewModel.toggleScientificMode() },
          onOpenHistory = { viewModel.setHistorySheet(true) }
        )

        // 2. Responsive Display Area (Elevated spacious layout)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(if (isScientificMode) 0.8f else 1.0f)
            .padding(horizontal = 14.dp),
          contentAlignment = Alignment.BottomEnd
        ) {
          OleskaDisplay(
            expression = uiState.expression,
            displayValue = uiState.displayValue,
            memoryValue = uiState.memoryValue,
            isMemoryActive = uiState.isMemoryActive,
            onBackspace = { viewModel.onBackspaceClick() },
            onCopyResult = { viewModel.showCopiedFeedback("Copied: $it") }
          )
        }

        // 3. Mode Toggle & Memory Bar Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Mode Switcher [ STD | SCI ]
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(palette.displayBackground)
              .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (!isScientificMode) palette.subtleBorder else androidx.compose.ui.graphics.Color.Transparent)
                .clickable { viewModel.setScientificMode(false) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("btn_mode_standard"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "STD",
                fontSize = 10.sp,
                fontWeight = if (!isScientificMode) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 1.sp,
                color = if (!isScientificMode) palette.numberButtonText else OleskaSteelGrey
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isScientificMode) palette.subtleBorder else androidx.compose.ui.graphics.Color.Transparent)
                .clickable { viewModel.setScientificMode(true) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("btn_mode_scientific"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "SCI",
                fontSize = 10.sp,
                fontWeight = if (isScientificMode) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 1.sp,
                color = if (isScientificMode) palette.numberButtonText else OleskaSteelGrey
              )
            }
          }

          // Memory Functions (MC, MR, M-, M+)
          Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val memItems = listOf(
              Triple("MC", { viewModel.onMemoryClear() }, "btn_mc"),
              Triple("MR", { viewModel.onMemoryRecall() }, "btn_mr"),
              Triple("M-", { viewModel.onMemorySubtract() }, "btn_m_minus"),
              Triple("M+", { viewModel.onMemoryAdd() }, "btn_m_plus")
            )

            memItems.forEach { (label, action, tag) ->
              val isActive = uiState.isMemoryActive && (label == "M+" || label == "MR")
              val textColor = if (isActive) {
                if (isDarkMode) OleskaSilverMist else palette.numberButtonText
              } else {
                OleskaSteelGrey
              }

              Box(
                modifier = Modifier
                  .height(28.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                      try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      } catch (_: Throwable) {}
                      action()
                    }
                  )
                  .padding(horizontal = 6.dp)
                  .testTag(tag),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = textColor,
                  textDecoration = if (isActive) TextDecoration.Underline else TextDecoration.None,
                  textAlign = TextAlign.Center
                )
              }
            }
          }

          // Angle Mode Toggle: DEG / RAD (visible in SCI mode)
          if (isScientificMode) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(palette.displayBackground)
                .clickable { viewModel.toggleAngleMode() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("btn_angle_mode"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isRadMode) "RAD" else "DEG",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.numberButtonText
              )
            }
          }
        }

        // 4. Refined Compact Keypad Container
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .weight(if (isScientificMode) 2.4f else 2.1f)
            .shadow(
              elevation = if (isDarkMode) 24.dp else 8.dp,
              shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
              ambientColor = palette.cardShadow,
              spotColor = palette.cardShadow
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("keypad_grid_container"),
          color = palette.cardBackground
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 14.dp, vertical = if (isScientificMode) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (isScientificMode) 6.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // SCIENTIFIC ROWS (sin, cos, tan, log, ln, x², √, π/e)
            if (isScientificMode) {
              // Scientific Row 1: sin, cos, tan, log
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                OleskaButton(
                  text = "sin",
                  type = ButtonType.ACTION,
                  fontSize = 16.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onSinClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_sin"
                )
                OleskaButton(
                  text = "cos",
                  type = ButtonType.ACTION,
                  fontSize = 16.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onCosClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_cos"
                )
                OleskaButton(
                  text = "tan",
                  type = ButtonType.ACTION,
                  fontSize = 16.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onTanClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_tan"
                )
                OleskaButton(
                  text = "log",
                  type = ButtonType.ACTION,
                  fontSize = 16.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onLogClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_log"
                )
              }

              // Scientific Row 2: ln, x², √, π
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                OleskaButton(
                  text = "ln",
                  type = ButtonType.ACTION,
                  fontSize = 16.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onLnClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_ln"
                )
                OleskaButton(
                  text = "x²",
                  type = ButtonType.ACTION,
                  fontSize = 16.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onSquareClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_square"
                )
                OleskaButton(
                  text = "√",
                  type = ButtonType.ACTION,
                  fontSize = 18.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onSquareRootClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_sqrt"
                )
                OleskaButton(
                  text = "π",
                  type = ButtonType.ACTION,
                  fontSize = 17.sp,
                  shape = RoundedCornerShape(10.dp),
                  onClick = { viewModel.onPiClick() },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  testTag = "btn_pi"
                )
              }
            }

            // Row 1: AC/C, ±, %, ⌫ (Backspace in top-right slot)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              horizontalArrangement = Arrangement.spacedBy(if (isScientificMode) 6.dp else 8.dp)
            ) {
              val isClear = uiState.displayValue != "0"
              OleskaButton(
                text = if (isClear) "C" else "AC",
                type = ButtonType.ACTION,
                fontSize = 18.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onClearClick() },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_clear"
              )
              OleskaButton(
                text = "±",
                type = ButtonType.ACTION,
                fontSize = 19.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onToggleSignClick() },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_negate"
              )
              OleskaButton(
                text = if (isScientificMode) "e" else "%",
                type = ButtonType.ACTION,
                fontSize = 18.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = {
                  if (isScientificMode) viewModel.onEulerClick() else viewModel.onPercentageClick()
                },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = if (isScientificMode) "btn_euler" else "btn_percent"
              )
              OleskaButton(
                text = "⌫",
                icon = Icons.AutoMirrored.Filled.Backspace,
                type = ButtonType.ACTION,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onBackspaceClick() },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_backspace"
              )
            }

            // Row 2: 7, 8, 9, ÷ (Division in right operator column)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              horizontalArrangement = Arrangement.spacedBy(if (isScientificMode) 6.dp else 8.dp)
            ) {
              OleskaButton(
                text = "7",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("7") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_7"
              )
              OleskaButton(
                text = "8",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("8") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_8"
              )
              OleskaButton(
                text = "9",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("9") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_9"
              )
              OleskaButton(
                text = "÷",
                type = ButtonType.OPERATOR,
                isActiveOperator = uiState.activeOperator == Operator.DIVIDE,
                fontSize = 22.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onOperatorClick(Operator.DIVIDE) },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_divide"
              )
            }

            // Row 3: 4, 5, 6, × (Multiplication in right operator column)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              horizontalArrangement = Arrangement.spacedBy(if (isScientificMode) 6.dp else 8.dp)
            ) {
              OleskaButton(
                text = "4",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("4") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_4"
              )
              OleskaButton(
                text = "5",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("5") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_5"
              )
              OleskaButton(
                text = "6",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("6") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_6"
              )
              OleskaButton(
                text = "×",
                type = ButtonType.OPERATOR,
                isActiveOperator = uiState.activeOperator == Operator.MULTIPLY,
                fontSize = 22.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onOperatorClick(Operator.MULTIPLY) },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_multiply"
              )
            }

            // Row 4: 1, 2, 3, − (Subtraction in right operator column)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              horizontalArrangement = Arrangement.spacedBy(if (isScientificMode) 6.dp else 8.dp)
            ) {
              OleskaButton(
                text = "1",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("1") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_1"
              )
              OleskaButton(
                text = "2",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("2") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_2"
              )
              OleskaButton(
                text = "3",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("3") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_3"
              )
              OleskaButton(
                text = "−",
                type = ButtonType.OPERATOR,
                isActiveOperator = uiState.activeOperator == Operator.SUBTRACT,
                fontSize = 22.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onOperatorClick(Operator.SUBTRACT) },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_subtract"
              )
            }

            // Row 5: =, 0, ., + (Equals at bottom-left, Addition in right operator column)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              horizontalArrangement = Arrangement.spacedBy(if (isScientificMode) 6.dp else 8.dp)
            ) {
              OleskaButton(
                text = "=",
                type = ButtonType.EQUALS,
                fontSize = 22.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onEqualsClick() },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_equals"
              )
              OleskaButton(
                text = "0",
                type = ButtonType.NUMBER,
                fontSize = 20.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDigitClick("0") },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_0"
              )
              OleskaButton(
                text = ".",
                type = ButtonType.NUMBER,
                fontSize = 22.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onDecimalClick() },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_decimal"
              )
              OleskaButton(
                text = "+",
                type = ButtonType.OPERATOR,
                isActiveOperator = uiState.activeOperator == Operator.ADD,
                fontSize = 22.sp,
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.onOperatorClick(Operator.ADD) },
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(),
                testTag = "btn_add"
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Home Bar Indicator
            Box(
              modifier = Modifier
                .padding(bottom = 2.dp)
                .size(width = 120.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.subtleBorder)
                .testTag("home_bar_indicator")
            )
          }
        }
      }

      // History Tape Bottom Sheet
      if (showHistorySheet) {
        OleskaHistorySheet(
          historyList = uiState.history,
          onClose = { viewModel.setHistorySheet(false) },
          onClearHistory = { viewModel.onClearHistory() },
          onRestoreItem = { viewModel.onRestoreHistoryItem(it) },
          onDeleteItem = { viewModel.onDeleteHistoryItem(it) },
          onCopyText = { viewModel.showCopiedFeedback("Copied calculation") }
        )
      }
    }
  }
}
