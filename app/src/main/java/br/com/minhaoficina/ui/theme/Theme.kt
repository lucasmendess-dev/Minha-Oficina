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

/** Verde-limão de destaque (cards de resumo, logo, indicadores). */
val Lima = Color(0xFFD4F53C)
val TintaLima = Color(0xFF151515)

private val Claro = lightColorScheme(
    primary = Color(0xFF1A1A1C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF9C6),
    onPrimaryContainer = Color(0xFF1A1A1C),
    secondary = Lima,
    onSecondary = TintaLima,
    background = Color(0xFFF2F3F5),
    onBackground = Color(0xFF1A1A1C),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1C),
    surfaceVariant = Color(0xFFF2F3F5),
    onSurfaceVariant = Color(0xFF6E7076),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF2F3F5),
    surfaceContainerHigh = Color.White,
    outline = Color(0xFF9A9CA3),
    outlineVariant = Color(0xFFE6E7EA),
    error = Color(0xFFC0392B),
)

private val Escuro = darkColorScheme(
    primary = Lima,
    onPrimary = TintaLima,
    primaryContainer = Color(0xFF2F3612),
    onPrimaryContainer = Lima,
    secondary = Lima,
    onSecondary = TintaLima,
    background = Color(0xFF101011),
    onBackground = Color(0xFFF1F2F4),
    surface = Color(0xFF1C1C1F),
    onSurface = Color(0xFFF1F2F4),
    surfaceVariant = Color(0xFF26262A),
    onSurfaceVariant = Color(0xFFA2A4AA),
    surfaceContainerLow = Color(0xFF1C1C1F),
    surfaceContainer = Color(0xFF101011),
    surfaceContainerHigh = Color(0xFF26262A),
    outline = Color(0xFF6E7076),
    outlineVariant = Color(0xFF2C2C30),
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
        Situacao.DISPONIVEL -> if (escuro) CoresSituacao(Lima, Color(0xFF2F3612)) else CoresSituacao(Color(0xFF4A6B00), Color(0xFFEFF9C6))
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
