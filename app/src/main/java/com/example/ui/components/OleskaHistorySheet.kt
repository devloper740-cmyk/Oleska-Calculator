package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculationHistory
import com.example.ui.theme.LocalOleskaPalette
import com.example.ui.theme.OleskaBrandFont
import com.example.ui.theme.OleskaSteelGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OleskaHistorySheet(
  historyList: List<CalculationHistory>,
  onClose: () -> Unit,
  onClearHistory: () -> Unit,
  onRestoreItem: (CalculationHistory) -> Unit,
  onCopyText: (String) -> Unit,
  onDeleteItem: ((CalculationHistory) -> Unit)? = null,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val palette = LocalOleskaPalette.current
  val clipboardManager = LocalClipboardManager.current

  ModalBottomSheet(
    onDismissRequest = onClose,
    sheetState = sheetState,
    containerColor = palette.cardBackground,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 12.dp)
          .size(width = 40.dp, height = 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(palette.subtleBorder)
      )
    },
    modifier = Modifier.testTag("history_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "CALCULATION TAPE",
            fontFamily = OleskaBrandFont,
            fontSize = 16.sp,
            letterSpacing = 2.sp,
            color = palette.numberButtonText,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "${historyList.size} SAVED RECORD${if (historyList.size == 1) "" else "S"}",
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = OleskaSteelGrey
          )
        }

        Row {
          if (historyList.isNotEmpty()) {
            IconButton(
              onClick = onClearHistory,
              modifier = Modifier.testTag("clear_history_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Clear history",
                tint = OleskaSteelGrey
              )
            }
          }
          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close history",
              tint = palette.numberButtonText
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (historyList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "TAPE IS EMPTY",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              letterSpacing = 1.5.sp,
              color = OleskaSteelGrey
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Calculations will be saved automatically to local storage",
              fontSize = 12.sp,
              color = OleskaSteelGrey.copy(alpha = 0.7f)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
        ) {
          items(historyList, key = { it.id }) { item ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .shadow(
                  elevation = 1.dp,
                  shape = RoundedCornerShape(14.dp),
                  ambientColor = palette.cardShadow,
                  spotColor = palette.cardShadow
                )
                .clip(RoundedCornerShape(14.dp))
                .background(palette.displayBackground)
                .clickable { onRestoreItem(item) }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("history_item_${item.id}")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.expression,
                    fontSize = 13.sp,
                    color = OleskaSteelGrey
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "= ${item.result}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.numberButtonText
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = {
                      val full = "${item.expression} = ${item.result}"
                      clipboardManager.setText(AnnotatedString(full))
                      onCopyText(full)
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copy equation",
                      tint = OleskaSteelGrey,
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  if (onDeleteItem != null) {
                    IconButton(
                      onClick = { onDeleteItem(item) },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete item",
                        tint = OleskaSteelGrey.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
