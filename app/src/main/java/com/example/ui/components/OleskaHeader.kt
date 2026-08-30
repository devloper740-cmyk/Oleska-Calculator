package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalOleskaPalette
import com.example.ui.theme.OleskaBrandFont
import com.example.ui.theme.OleskaSteelGrey

@Composable
fun OleskaHeader(
  isDarkMode: Boolean,
  isMemoryActive: Boolean,
  historyCount: Int,
  isScientificMode: Boolean = false,
  onToggleTheme: () -> Unit,
  onToggleScientificMode: () -> Unit = {},
  onOpenHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = LocalOleskaPalette.current

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Brand Monogram / History
    BadgedBox(
      badge = {
        if (historyCount > 0) {
          Badge(
            containerColor = palette.numberButtonText,
            contentColor = palette.cardBackground,
            modifier = Modifier.testTag("history_badge")
          ) {
            Text(
              text = if (historyCount > 9) "9+" else "$historyCount",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    ) {
      IconButton(
        onClick = onOpenHistory,
        modifier = Modifier
          .size(40.dp)
          .shadow(
            elevation = 2.dp,
            shape = CircleShape,
            ambientColor = palette.cardShadow,
            spotColor = palette.cardShadow
          )
          .clip(CircleShape)
          .background(palette.displayBackground)
          .testTag("history_button")
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = "Calculation history",
          tint = palette.numberButtonText,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    // Sophisticated Dark Brand Wordmark
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "OLESKA",
        fontFamily = OleskaBrandFont,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Light,
        fontSize = 21.sp,
        letterSpacing = 4.5.sp,
        color = palette.numberButtonText,
        modifier = Modifier.testTag("brand_logo")
      )

      Text(
        text = if (isScientificMode) "SCIENTIFIC CALCULATOR" else "CALCULATOR",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 3.sp,
        color = OleskaSteelGrey,
        modifier = Modifier
          .padding(top = 1.dp)
          .testTag("brand_subtitle")
      )

      AnimatedVisibility(
        visible = isMemoryActive,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(palette.subtleBorder)
              .padding(horizontal = 5.dp, vertical = 1.dp)
              .testTag("memory_active_badge")
          ) {
            Text(
              text = "MEMORY ACTIVE",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp,
              color = palette.numberButtonText
            )
          }
        }
      }
    }

    // Right actions: Mode toggle + Dark/Light Theme toggle
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onToggleScientificMode,
        modifier = Modifier
          .size(40.dp)
          .shadow(
            elevation = 2.dp,
            shape = CircleShape,
            ambientColor = palette.cardShadow,
            spotColor = palette.cardShadow
          )
          .clip(CircleShape)
          .background(if (isScientificMode) palette.subtleBorder else palette.displayBackground)
          .testTag("mode_toggle_button")
      ) {
        Icon(
          imageVector = if (isScientificMode) Icons.Default.Functions else Icons.Default.Calculate,
          contentDescription = if (isScientificMode) "Switch to Standard Mode" else "Switch to Scientific Mode",
          tint = palette.numberButtonText,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onToggleTheme,
        modifier = Modifier
          .size(40.dp)
          .shadow(
            elevation = 2.dp,
            shape = CircleShape,
            ambientColor = palette.cardShadow,
            spotColor = palette.cardShadow
          )
          .clip(CircleShape)
          .background(palette.displayBackground)
          .testTag("theme_toggle_button")
      ) {
        Icon(
          imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
          contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
          tint = palette.numberButtonText,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
