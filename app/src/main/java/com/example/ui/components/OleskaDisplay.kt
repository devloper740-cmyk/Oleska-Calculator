package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalOleskaPalette
import com.example.ui.theme.OleskaSilverMist
import com.example.ui.theme.OleskaSteelGrey
import java.math.BigDecimal

@Composable
fun OleskaDisplay(
  expression: String,
  displayValue: String,
  memoryValue: BigDecimal,
  isMemoryActive: Boolean,
  onBackspace: () -> Unit,
  onCopyResult: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = LocalOleskaPalette.current
  val clipboardManager = LocalClipboardManager.current

  // Animated luxury blinking indicator
  val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
  val cursorAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(650),
      repeatMode = RepeatMode.Reverse
    ),
    label = "cursor_alpha"
  )

  // Fluid responsive font size scaling based on length
  val displayFontSize = when {
    displayValue.length <= 6 -> 64.sp
    displayValue.length <= 8 -> 50.sp
    displayValue.length <= 10 -> 40.sp
    displayValue.length <= 13 -> 32.sp
    displayValue.length <= 16 -> 26.sp
    else -> 20.sp
  }

  var dragAccumulator by remember { mutableFloatStateOf(0f) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = { dragAccumulator = 0f },
          onHorizontalDrag = { _, dragAmount ->
            dragAccumulator += dragAmount
            if (kotlin.math.abs(dragAccumulator) > 35f) {
              onBackspace()
              dragAccumulator = 0f
            }
          }
        )
      }
      .pointerInput(displayValue) {
        detectTapGestures(
          onLongPress = {
            clipboardManager.setText(AnnotatedString(displayValue))
            onCopyResult(displayValue)
          }
        )
      }
      .testTag("calculator_display"),
    horizontalAlignment = Alignment.End
  ) {
    // Top Row: Memory readout or copy action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = {
          clipboardManager.setText(AnnotatedString(displayValue))
          onCopyResult(displayValue)
        },
        modifier = Modifier
          .size(32.dp)
          .testTag("copy_button")
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Copy result",
          tint = OleskaSteelGrey,
          modifier = Modifier.size(16.dp)
        )
      }

      AnimatedVisibility(
        visible = isMemoryActive,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Text(
          text = "M+ $memoryValue",
          color = OleskaSteelGrey,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = 1.5.sp,
          textAlign = TextAlign.End,
          maxLines = 1,
          modifier = Modifier.testTag("memory_display_text")
        )
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    // Middle Line: Ongoing expression
    Text(
      text = if (expression.isNotEmpty()) expression else " ",
      color = OleskaSilverMist,
      fontSize = 20.sp,
      fontWeight = FontWeight.Light,
      letterSpacing = (-0.25).sp,
      textAlign = TextAlign.End,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .padding(vertical = 2.dp)
        .testTag("expression_text")
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Bottom Main Display Line: Large high-end typography
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = displayValue,
        color = palette.numberButtonText,
        fontSize = displayFontSize,
        fontWeight = FontWeight.Light,
        letterSpacing = (-1.0).sp,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("display_value")
      )

      // Precision Blinking Cursor
      Box(
        modifier = Modifier
          .padding(start = 4.dp, end = 2.dp)
          .width(2.5.dp)
          .height((displayFontSize.value * 0.72f).dp)
          .alpha(cursorAlpha)
          .background(palette.numberButtonText)
      )
    }
  }
}
