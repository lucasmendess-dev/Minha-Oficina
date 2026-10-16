package br.com.minhaoficina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Situações possíveis de uma ferramenta. O valor salvo no banco é [chave]. */
enum class Situacao(val chave: String, val rotulo: String, val plural: String) {
    DISPONIVEL("disp", "Disponível", "Disponíveis"),
    EMPRESTADA("emp", "Emprestada", "Emprestadas"),
    MANUTENCAO("man", "Manutenção", "Manutenção");

    companion object {
        fun de(chave: String): Situacao = entries.firstOrNull { it.chave == chave } ?: DISPONIVEL
    }
}

val CATEGORIAS = listOf("Elétrica", "Manual", "Medição", "Jardim", "Pintura", "Hidráulica", "Outros")

@Entity(tableName = "ferramentas")
data class Ferramenta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String = "",
    val nome: String = "",
    val marca: String = "",
    val categoria: String = CATEGORIAS.first(),
    val local: String = "",
    val valor: Double = 0.0,
    val situacao: String = Situacao.DISPONIVEL.chave,
    val emprestadoPara: String = "",
    /** Data no formato ISO (aaaa-mm-dd), vazia quando não está emprestada. */
    val emprestadoEm: String = "",
    val notas: String = "",
    val criadoEm: Long = System.currentTimeMillis(),
    val atualizadoEm: Long = System.currentTimeMillis(),
)

val Ferramenta.status: Situacao get() = Situacao.de(situacao)
