package com.example.data

import com.example.calculator.CalculationHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CalculatorRepository(private val dao: CalculationDao) {
  val historyFlow: Flow<List<CalculationHistory>> = dao.getAllHistory().map { list ->
    list.map { CalculationHistory(id = it.id, expression = it.expression, result = it.result, timestamp = it.timestamp) }
  }

  suspend fun saveCalculation(expression: String, result: String): Long {
    val entity = CalculationEntity(expression = expression, result = result, timestamp = System.currentTimeMillis())
    return dao.insert(entity)
  }

  suspend fun deleteCalculation(id: Long) {
    dao.deleteById(id)
  }

  suspend fun clearHistory() {
    dao.clearAll()
  }
}
