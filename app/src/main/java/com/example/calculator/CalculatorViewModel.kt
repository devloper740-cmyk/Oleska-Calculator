package com.example.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CalculatorViewModel(
  private val repository: CalculatorRepository? = null
) : ViewModel() {
  private val engine = CalculatorEngine()

  private val _uiState = MutableStateFlow(
    CalculatorState(
      displayValue = "0",
      expression = "",
      memoryValue = BigDecimal.ZERO,
      isMemoryActive = false,
      activeOperator = null,
      isNewNumber = true,
      history = emptyList()
    )
  )
  val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

  private val _isDarkMode = MutableStateFlow(true)
  val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

  private val _isScientificMode = MutableStateFlow(false)
  val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

  private val _isRadMode = MutableStateFlow(false)
  val isRadMode: StateFlow<Boolean> = _isRadMode.asStateFlow()

  private val _showHistorySheet = MutableStateFlow(false)
  val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

  private val _copiedToastMessage = MutableStateFlow<String?>(null)
  val copiedToastMessage: StateFlow<String?> = _copiedToastMessage.asStateFlow()

  init {
    if (repository != null) {
      viewModelScope.launch {
        repository.historyFlow.collect { dbHistory ->
          _uiState.update { currentState ->
            currentState.copy(history = dbHistory)
          }
        }
      }
    }
  }

  fun toggleTheme() {
    _isDarkMode.update { !it }
  }

  fun setTheme(isDark: Boolean) {
    _isDarkMode.value = isDark
  }

  fun toggleScientificMode() {
    _isScientificMode.update { !it }
  }

  fun setScientificMode(isScientific: Boolean) {
    _isScientificMode.value = isScientific
  }

  fun toggleAngleMode() {
    _isRadMode.update { !it }
  }

  fun toggleHistorySheet() {
    _showHistorySheet.update { !it }
  }

  fun setHistorySheet(show: Boolean) {
    _showHistorySheet.value = show
  }

  fun clearToastMessage() {
    _copiedToastMessage.value = null
  }

  fun showCopiedFeedback(message: String = "Copied to clipboard") {
    _copiedToastMessage.value = message
  }

  fun onDigitClick(digit: String) {
    val currentRaw = _uiState.value.displayValue
    val newDisplayRaw = engine.onDigit(currentRaw, digit)
    val formatted = engine.formatDisplay(newDisplayRaw)

    _uiState.update {
      it.copy(
        displayValue = formatted,
        isNewNumber = false,
        activeOperator = null
      )
    }
  }

  fun onDecimalClick() {
    val currentRaw = _uiState.value.displayValue
    val newDisplayRaw = engine.onDecimal(currentRaw)
    val formatted = engine.formatDisplay(newDisplayRaw)

    _uiState.update {
      it.copy(
        displayValue = formatted,
        isNewNumber = false
      )
    }
  }

  fun onOperatorClick(operator: Operator) {
    val currentDisplay = _uiState.value.displayValue
    val (resultDisplay, newExpression) = engine.onOperator(currentDisplay, operator)

    _uiState.update {
      it.copy(
        displayValue = resultDisplay,
        expression = newExpression,
        activeOperator = operator,
        isNewNumber = true
      )
    }
  }

  fun onEqualsClick() {
    val currentDisplay = _uiState.value.displayValue
    val result = engine.onEquals(currentDisplay)
    if (result != null) {
      val (res, expr) = result
      val cleanExpr = expr.removeSuffix(" =").trim()
      _uiState.update {
        it.copy(
          displayValue = res,
          expression = expr,
          activeOperator = null,
          isNewNumber = true,
          history = if (repository == null) engine.getCurrentState(res).history else it.history
        )
      }
      if (repository != null && res != "Error") {
        viewModelScope.launch {
          repository.saveCalculation(cleanExpr, res)
        }
      }
    }
  }

  fun onPercentageClick() {
    val currentDisplay = _uiState.value.displayValue
    val (res, expr) = engine.onPercentage(currentDisplay)
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true
      )
    }
  }

  fun onSquareRootClick() {
    val currentDisplay = _uiState.value.displayValue
    val (res, expr) = engine.onSquareRoot(currentDisplay)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onSinClick() {
    val currentDisplay = _uiState.value.displayValue
    val isRad = _isRadMode.value
    val (res, expr) = engine.onSin(currentDisplay, isRad)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onCosClick() {
    val currentDisplay = _uiState.value.displayValue
    val isRad = _isRadMode.value
    val (res, expr) = engine.onCos(currentDisplay, isRad)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onTanClick() {
    val currentDisplay = _uiState.value.displayValue
    val isRad = _isRadMode.value
    val (res, expr) = engine.onTan(currentDisplay, isRad)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onLogClick() {
    val currentDisplay = _uiState.value.displayValue
    val (res, expr) = engine.onLog(currentDisplay)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onLnClick() {
    val currentDisplay = _uiState.value.displayValue
    val (res, expr) = engine.onLn(currentDisplay)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onSquareClick() {
    val currentDisplay = _uiState.value.displayValue
    val (res, expr) = engine.onSquare(currentDisplay)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onReciprocalClick() {
    val currentDisplay = _uiState.value.displayValue
    val (res, expr) = engine.onReciprocal(currentDisplay)
    val cleanExpr = expr.removeSuffix(" =").trim()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = expr,
        isNewNumber = true,
        history = if (repository == null) engine.getCurrentState(res).history else it.history
      )
    }
    if (repository != null && res != "Error") {
      viewModelScope.launch {
        repository.saveCalculation(cleanExpr, res)
      }
    }
  }

  fun onPiClick() {
    val piVal = engine.onPi()
    val formatted = engine.formatDisplay(piVal)
    _uiState.update {
      it.copy(
        displayValue = formatted,
        isNewNumber = true
      )
    }
  }

  fun onEulerClick() {
    val eVal = engine.onEuler()
    val formatted = engine.formatDisplay(eVal)
    _uiState.update {
      it.copy(
        displayValue = formatted,
        isNewNumber = true
      )
    }
  }

  fun onToggleSignClick() {
    val currentDisplay = _uiState.value.displayValue
    val newDisplayRaw = engine.onToggleSign(currentDisplay)
    val formatted = engine.formatDisplay(newDisplayRaw)
    _uiState.update {
      it.copy(displayValue = formatted)
    }
  }

  fun onBackspaceClick() {
    val currentDisplay = _uiState.value.displayValue
    val shortenedRaw = engine.onBackspace(currentDisplay)
    val formatted = engine.formatDisplay(shortenedRaw)
    _uiState.update {
      it.copy(displayValue = formatted)
    }
  }

  fun onClearClick() {
    if (_uiState.value.displayValue != "0") {
      val res = engine.onClear()
      _uiState.update {
        it.copy(
          displayValue = res,
          isNewNumber = true,
          activeOperator = null
        )
      }
    } else {
      val res = engine.onAllClear()
      _uiState.update {
        it.copy(
          displayValue = res,
          expression = "",
          activeOperator = null,
          isNewNumber = true
        )
      }
    }
  }

  fun onAllClearClick() {
    val res = engine.onAllClear()
    _uiState.update {
      it.copy(
        displayValue = res,
        expression = "",
        activeOperator = null,
        isNewNumber = true
      )
    }
  }

  // Memory Functions
  fun onMemoryAdd() {
    val currentDisplay = _uiState.value.displayValue
    val newMem = engine.onMemoryAdd(currentDisplay)
    _uiState.update {
      it.copy(
        memoryValue = newMem,
        isMemoryActive = newMem.compareTo(BigDecimal.ZERO) != 0,
        isNewNumber = true
      )
    }
    showCopiedFeedback("Added to Memory (M+)")
  }

  fun onMemorySubtract() {
    val currentDisplay = _uiState.value.displayValue
    val newMem = engine.onMemorySubtract(currentDisplay)
    _uiState.update {
      it.copy(
        memoryValue = newMem,
        isMemoryActive = newMem.compareTo(BigDecimal.ZERO) != 0,
        isNewNumber = true
      )
    }
    showCopiedFeedback("Subtracted from Memory (M-)")
  }

  fun onMemoryRecall() {
    val recalled = engine.onMemoryRecall()
    _uiState.update {
      it.copy(
        displayValue = recalled,
        isNewNumber = true
      )
    }
    showCopiedFeedback("Memory Recalled (MR)")
  }

  fun onMemoryClear() {
    val newMem = engine.onMemoryClear()
    _uiState.update {
      it.copy(
        memoryValue = newMem,
        isMemoryActive = false
      )
    }
    showCopiedFeedback("Memory Cleared (MC)")
  }

  fun onClearHistory() {
    engine.clearHistory()
    _uiState.update {
      it.copy(history = emptyList())
    }
    if (repository != null) {
      viewModelScope.launch {
        repository.clearHistory()
      }
    }
  }

  fun onDeleteHistoryItem(item: CalculationHistory) {
    if (repository != null) {
      viewModelScope.launch {
        repository.deleteCalculation(item.id)
      }
    } else {
      _uiState.update { state ->
        state.copy(history = state.history.filter { it.id != item.id })
      }
    }
  }

  fun onRestoreHistoryItem(item: CalculationHistory) {
    val (result, expr) = engine.restoreHistory(item)
    _uiState.update {
      it.copy(
        displayValue = result,
        expression = expr,
        activeOperator = null,
        isNewNumber = true
      )
    }
    _showHistorySheet.value = false
    showCopiedFeedback("Restored: ${item.result}")
  }
}
