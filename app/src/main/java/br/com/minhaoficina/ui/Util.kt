package br.com.minhaoficina.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.status
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

val PT_BR: Locale = Locale.forLanguageTag("pt-BR")
private val moeda = NumberFormat.getCurrencyInstance(PT_BR)
private val fmtBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun reais(v: Double): String = moeda.format(v)

fun hojeIso(): String = LocalDate.now().toString()

fun dataBr(iso: String): String = runCatching { LocalDate.parse(iso).format(fmtBr) }.getOrDefault("")

fun dataBr(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(fmtBr)

/** Dias desde a data ISO; null se a data estiver vazia ou inválida. */
fun diasDesde(iso: String): Long? = runCatching {
    ChronoUnit.DAYS.between(LocalDate.parse(iso), LocalDate.now()).coerceAtLeast(0)
}.getOrNull()

fun textoDias(iso: String): String = when (val d = diasDesde(iso)) {
    null -> ""
    0L -> "desde hoje"
    1L -> "há 1 dia"
    else -> "há $d dias"
}

const val DIAS_ALERTA = 14

fun iconeCategoria(cat: String): ImageVector = when (cat) {
    "Elétrica" -> Icons.Outlined.ElectricBolt
    "Manual" -> Icons.Outlined.Build
    "Medição" -> Icons.Outlined.Straighten
    "Jardim" -> Icons.Outlined.Grass
    "Pintura" -> Icons.Outlined.FormatPaint
    "Hidráulica" -> Icons.Outlined.WaterDrop
    else -> Icons.Outlined.Inventory2
}

fun parseValor(texto: String): Double =
    texto.trim().replace("R$", "").replace(" ", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0

fun valorParaTexto(v: Double): String =
    if (v <= 0.0) "" else String.format(PT_BR, "%.2f", v)

fun gerarCsv(lista: List<Ferramenta>): String {
    fun c(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
    val cab = "Código;Nome;Marca;Categoria;Local;Valor;Situação;Emprestada para;Desde;Observações"
    return (listOf(cab) + lista.map { f ->
        listOf(
            f.codigo, f.nome, f.marca, f.categoria, f.local, valorParaTexto(f.valor),
            f.status.rotulo, f.emprestadoPara, dataBr(f.emprestadoEm), f.notas,
        ).joinToString(";") { c(it) }
    }).joinToString("\n")
}
