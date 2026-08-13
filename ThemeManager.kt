package com.ptahstudio.myapp

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color
)

enum class PtahTheme {
    CLASICO, CLARO, OSCURO, ANOCHECER, BOSQUE, SOLARIZED_CLARO, SOLARIZED_OSCURO
}

object ThemeManager {
    val themes = mapOf(
        PtahTheme.CLASICO to ThemeColors(
            primary = Color(0xFF6200EE),
            onPrimary = Color.White,
            background = Color(0xFFF5F5F5),
            onBackground = Color(0xFF121212),
            surface = Color.White,
            onSurface = Color(0xFF121212)
        ),
        PtahTheme.CLARO to ThemeColors(
            primary = Color(0xFF007AFF),
            onPrimary = Color.White,
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF1C1C1E),
            surface = Color.White,
            onSurface = Color(0xFF1C1C1E)
        ),
        PtahTheme.OSCURO to ThemeColors(
            primary = Color(0xFFBB86FC),
            onPrimary = Color.Black,
            background = Color(0xFF121212),
            onBackground = Color(0xFFE1E1E1),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFE1E1E1)
        ),
        PtahTheme.ANOCHECER to ThemeColors(
            primary = Color(0xFFFF5722),
            onPrimary = Color.White,
            background = Color(0xFF1A1A2E),
            onBackground = Color(0xFFE2E2E2),
            surface = Color(0xFF16213E),
            onSurface = Color(0xFFE2E2E2)
        ),
        PtahTheme.BOSQUE to ThemeColors(
            primary = Color(0xFF2E7D32),
            onPrimary = Color.White,
            background = Color(0xFFECEFF1),
            onBackground = Color(0xFF263238),
            surface = Color.White,
            onSurface = Color(0xFF263238)
        ),
        PtahTheme.SOLARIZED_CLARO to ThemeColors(
            primary = Color(0xFFB58900),
            onPrimary = Color(0xFFFDF6E3),
            background = Color(0xFFFDF6E3),
            onBackground = Color(0xFF657B83),
            surface = Color(0xFFEEE8D5),
            onSurface = Color(0xFF586E75)
        ),
        PtahTheme.SOLARIZED_OSCURO to ThemeColors(
            primary = Color(0xFF268BD2),
            onPrimary = Color(0xFF002B36),
            background = Color(0xFF002B36),
            onBackground = Color(0xFF93A1A1),
            surface = Color(0xFF073642),
            onSurface = Color(0xFF839496)
        )
    )
}
