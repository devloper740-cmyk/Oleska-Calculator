package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val OleskaBrandFont = FontFamily.Serif
val OleskaSansFont = FontFamily.SansSerif

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Light,
    fontSize = 64.sp,
    lineHeight = 70.sp,
    letterSpacing = (-1.5).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Light,
    fontSize = 48.sp,
    lineHeight = 54.sp,
    letterSpacing = (-1.0).sp,
  ),
  displaySmall = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.5).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = OleskaBrandFont,
    fontWeight = FontWeight.Light,
    fontStyle = FontStyle.Italic,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 4.5.sp, // Sophisticated luxury tracking
  ),
  titleMedium = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Light,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.25).sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Light,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 2.sp, // High-end uppercase tracking
  ),
  labelSmall = TextStyle(
    fontFamily = OleskaSansFont,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.5.sp,
  )
)
