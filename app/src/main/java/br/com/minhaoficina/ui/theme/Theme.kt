package br.com.minhaoficina.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.minhaoficina.data.Situacao

private val Claro = lightColorScheme(
    primary = Color(0xFF0E5A73),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EFF3),
    onPrimaryContainer = Color(0xFF0E5A73),
    background = Color(0xFFF3F5F7),
    onBackground = Color(0xFF14202B),
    surface = Color.White,
    onSurface = Color(0xFF14202B),
    surfaceVariant = Color(0xFFEEF1F4),
    onSurfaceVariant = Color(0xFF5E6B78),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF3F5F7),
    surfaceContainerHigh = Color.White,
    outline = Color(0xFF8A96A3),
    outlineVariant = Color(0xFFE1E6EB),
    error = Color(0xFFB3332C),
)

private val Escuro = darkColorScheme(
    primary = Color(0xFF5FB8D4),
    onPrimary = Color(0xFF06212B),
    primaryContainer = Color(0xFF173441),
    onPrimaryContainer = Color(0xFF9CD6E8),
    background = Color(0xFF0E1419),
    onBackground = Color(0xFFE8EDF2),
    surface = Color(0xFF161E25),
    onSurface = Color(0xFFE8EDF2),
    surfaceVariant = Color(0xFF1D2730),
    onSurfaceVariant = Color(0xFF9AA7B4),
    surfaceContainerLow = Color(0xFF161E25),
    surfaceContainer = Color(0xFF0E1419),
    surfaceContainerHigh = Color(0xFF1D2730),
    outline = Color(0xFF6F7C89),
    outlineVariant = Color(0xFF26323C),
    error = Color(0xFFF28C85),
)

private val Tipos = Typography().let { t ->
    t.copy(
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.Bold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.Bold),
    )
}

val Rotulo = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)

data class CoresSituacao(val texto: Color, val fundo: Color)

@Composable
fun coresDe(s: Situacao): CoresSituacao {
    val escuro = isSystemInDarkTheme()
    return when (s) {
        Situacao.DISPONIVEL -> if (escuro) CoresSituacao(Color(0xFF5FD09A), Color(0xFF15342A)) else CoresSituacao(Color(0xFF17784A), Color(0xFFE1F3E9))
        Situacao.EMPRESTADA -> if (escuro) CoresSituacao(Color(0xFFF2B45E), Color(0xFF3A2A13)) else CoresSituacao(Color(0xFFA15C00), Color(0xFFFCEFD9))
        Situacao.MANUTENCAO -> if (escuro) CoresSituacao(Color(0xFFF28C85), Color(0xFF3E1D1B)) else CoresSituacao(Color(0xFFB3332C), Color(0xFFFBE4E2))
    }
}

@Composable
fun OficinaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Escuro else Claro,
        typography = Tipos,
        content = content,
    )
}
