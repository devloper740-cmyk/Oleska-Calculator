package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Oleska Luxury Monochrome Schemes
val OleskaDarkColorScheme = darkColorScheme(
  primary = OleskaWhite,
  onPrimary = OleskaBlack,
  primaryContainer = OleskaGraphite,
  onPrimaryContainer = OleskaWhite,
  secondary = OleskaSilverMist,
  onSecondary = OleskaBlack,
  secondaryContainer = OleskaCharcoal,
  onSecondaryContainer = OleskaWhite,
  tertiary = OleskaSteelGrey,
  onTertiary = OleskaWhite,
  background = OleskaBlack,
  onBackground = OleskaWhite,
  surface = OleskaCharcoal,
  onSurface = OleskaWhite,
  surfaceVariant = OleskaGraphite,
  onSurfaceVariant = OleskaSilverMist,
  outline = OleskaGraphite,
  outlineVariant = OleskaCharcoal,
  inverseSurface = OleskaWhite,
  inverseOnSurface = OleskaBlack,
  inversePrimary = OleskaBlack
)

val OleskaLightColorScheme = lightColorScheme(
  primary = OleskaBlack,
  onPrimary = OleskaWhite,
  primaryContainer = OleskaFog,
  onPrimaryContainer = OleskaBlack,
  secondary = OleskaGraphite,
  onSecondary = OleskaWhite,
  secondaryContainer = OleskaOffWhite,
  onSecondaryContainer = OleskaBlack,
  tertiary = OleskaSteelGrey,
  onTertiary = OleskaWhite,
  background = OleskaOffWhite,
  onBackground = OleskaBlack,
  surface = OleskaWhite,
  onSurface = OleskaBlack,
  surfaceVariant = OleskaFog,
  onSurfaceVariant = OleskaSteelGrey,
  outline = OleskaSilverMist,
  outlineVariant = OleskaFog,
  inverseSurface = OleskaBlack,
  inverseOnSurface = OleskaWhite,
  inversePrimary = OleskaWhite
)

data class OleskaPalette(
  val isDark: Boolean,
  val cardBackground: Color,
  val displayBackground: Color,
  val numberButtonBackground: Color,
  val numberButtonText: Color,
  val operatorButtonBackground: Color,
  val operatorButtonText: Color,
  val operatorActiveBackground: Color,
  val operatorActiveText: Color,
  val actionButtonBackground: Color,
  val actionButtonText: Color,
  val memoryButtonBackground: Color,
  val memoryButtonText: Color,
  val equalsButtonBackground: Color,
  val equalsButtonText: Color,
  val subtleBorder: Color,
  val cardShadow: Color
)

val LocalOleskaPalette = compositionLocalOf {
  OleskaPalette(
    isDark = true,
    cardBackground = OleskaCharcoal,
    displayBackground = OleskaCharcoal,
    numberButtonBackground = OleskaCharcoal,
    numberButtonText = OleskaWhite,
    operatorButtonBackground = OleskaGraphite,
    operatorButtonText = OleskaWhite,
    operatorActiveBackground = OleskaWhite,
    operatorActiveText = OleskaBlack,
    actionButtonBackground = OleskaGraphite,
    actionButtonText = OleskaWhite,
    memoryButtonBackground = OleskaCharcoal,
    memoryButtonText = OleskaSilverMist,
    equalsButtonBackground = OleskaWhite,
    equalsButtonText = OleskaBlack,
    subtleBorder = OleskaGraphite,
    cardShadow = OleskaShadowDark
  )
}

@Composable
fun OleskaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val baseScheme = if (darkTheme) OleskaDarkColorScheme else OleskaLightColorScheme
  
  // Smooth animated color transitions for theme switching
  val animBackground = animateColorAsState(baseScheme.background, tween(300), label = "bg")
  val animSurface = animateColorAsState(baseScheme.surface, tween(300), label = "surface")
  val animOnSurface = animateColorAsState(baseScheme.onSurface, tween(300), label = "onSurface")
  val animPrimary = animateColorAsState(baseScheme.primary, tween(300), label = "primary")
  val animOnPrimary = animateColorAsState(baseScheme.onPrimary, tween(300), label = "onPrimary")

  val animatedColorScheme = baseScheme.copy(
    background = animBackground.value,
    surface = animSurface.value,
    onSurface = animOnSurface.value,
    primary = animPrimary.value,
    onPrimary = animOnPrimary.value
  )

  val palette = if (darkTheme) {
    OleskaPalette(
      isDark = true,
      cardBackground = OleskaCharcoal,
      displayBackground = OleskaCharcoal,
      numberButtonBackground = OleskaCharcoal,
      numberButtonText = OleskaWhite,
      operatorButtonBackground = OleskaGraphite,
      operatorButtonText = OleskaWhite,
      operatorActiveBackground = OleskaWhite,
      operatorActiveText = OleskaBlack,
      actionButtonBackground = OleskaGraphite,
      actionButtonText = OleskaSilverMist,
      memoryButtonBackground = OleskaCharcoal,
      memoryButtonText = OleskaSilverMist,
      equalsButtonBackground = OleskaWhite,
      equalsButtonText = OleskaBlack,
      subtleBorder = OleskaGraphite,
      cardShadow = OleskaShadowDark
    )
  } else {
    OleskaPalette(
      isDark = false,
      cardBackground = OleskaWhite,
      displayBackground = OleskaOffWhite,
      numberButtonBackground = OleskaWhite,
      numberButtonText = OleskaBlack,
      operatorButtonBackground = OleskaFog,
      operatorButtonText = OleskaBlack,
      operatorActiveBackground = OleskaBlack,
      operatorActiveText = OleskaWhite,
      actionButtonBackground = OleskaFog,
      actionButtonText = OleskaCharcoal,
      memoryButtonBackground = OleskaOffWhite,
      memoryButtonText = OleskaSteelGrey,
      equalsButtonBackground = OleskaBlack,
      equalsButtonText = OleskaWhite,
      subtleBorder = OleskaFog,
      cardShadow = OleskaShadowLight
    )
  }

  CompositionLocalProvider(LocalOleskaPalette provides palette) {
    MaterialTheme(
      colorScheme = animatedColorScheme,
      typography = Typography,
      content = content
    )
  }
}
