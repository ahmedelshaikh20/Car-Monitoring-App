package com.example.carmonitoringapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleLightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = Green,
    tertiary = Orange,
    background = Bg,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Label,
    onSurface = Label,
    outline = Separator,
    error = Red
)

@Composable
fun CarMonitoringAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleLightColorScheme,
        typography = Typography,
        content = content
    )
}
