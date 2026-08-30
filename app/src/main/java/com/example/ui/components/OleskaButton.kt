package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalOleskaPalette
import com.example.ui.theme.OleskaBlack
import com.example.ui.theme.OleskaSilverMist
import com.example.ui.theme.OleskaSteelGrey
import com.example.ui.theme.OleskaWhite

enum class ButtonType {
  NUMBER,
  OPERATOR,
  ACTION,
  MEMORY,
  EQUALS
}

@Composable
fun OleskaButton(
  text: String,
  type: ButtonType,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  isActiveOperator: Boolean = false,
  testTag: String = "btn_$text",
  fontSize: TextUnit = 20.sp,
  shape: Shape = RoundedCornerShape(12.dp)
) {
  val palette = LocalOleskaPalette.current
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Tactile Spring scale-down on press animation
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "button_scale"
  )

  val targetBg: Color = when (type) {
    ButtonType.NUMBER -> if (isPressed) palette.actionButtonBackground else if (palette.isDark) Color(0xFF242424) else Color(0xFFFFFFFF)
    ButtonType.OPERATOR -> if (isActiveOperator) palette.operatorActiveBackground else if (isPressed) palette.actionButtonText else if (palette.isDark) Color(0xFF323232) else Color(0xFFE2E2E2)
    ButtonType.ACTION -> if (isPressed) palette.actionButtonBackground else if (palette.isDark) Color(0xFF2C2C2C) else Color(0xFFDADADA)
    ButtonType.MEMORY -> Color.Transparent
    ButtonType.EQUALS -> if (isPressed) palette.actionButtonBackground else if (palette.isDark) Color(0xFFFFFFFF) else Color(0xFF121212)
  }

  val targetTextColor: Color = when (type) {
    ButtonType.NUMBER -> if (palette.isDark) OleskaWhite else OleskaBlack
    ButtonType.OPERATOR -> if (isActiveOperator) palette.operatorActiveText else if (palette.isDark) OleskaWhite else OleskaBlack
    ButtonType.ACTION -> if (palette.isDark) OleskaWhite else OleskaBlack
    ButtonType.MEMORY -> OleskaSteelGrey
    ButtonType.EQUALS -> if (palette.isDark) OleskaBlack else OleskaWhite
  }

  val animBg by animateColorAsState(targetBg, tween(100), label = "button_bg")
  val animTextColor by animateColorAsState(targetTextColor, tween(100), label = "button_text_color")

  val borderStroke = if (palette.isDark) {
    BorderStroke(1.dp, Color(0xFF333333))
  } else {
    BorderStroke(1.dp, Color(0xFFE0E0E0))
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .scale(scale)
      .clip(shape)
      .background(animBg)
      .border(borderStroke, shape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {
          try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          } catch (_: Throwable) {}
          onClick()
        }
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = text,
        tint = animTextColor,
        modifier = Modifier.size(22.dp)
      )
    } else {
      Text(
        text = text,
        color = animTextColor,
        fontSize = fontSize,
        fontWeight = when (type) {
          ButtonType.NUMBER -> FontWeight.Normal
          ButtonType.OPERATOR -> FontWeight.Normal
          ButtonType.ACTION -> FontWeight.SemiBold
          ButtonType.MEMORY -> FontWeight.Bold
          ButtonType.EQUALS -> FontWeight.Medium
        },
        textAlign = TextAlign.Center
      )
    }
  }
}
