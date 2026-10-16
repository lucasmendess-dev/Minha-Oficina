package br.com.minhaoficina

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhaoficina.data.AppDatabase
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.Kit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OficinaViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val dao = db.ferramentas()
    private val kitDao = db.kits()

    val ferramentas: StateFlow<List<Ferramenta>> =
        dao.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Próximo código sequencial, ex.: FER-007. */
    fun proximoCodigo(): String {
        val maior = ferramentas.value.maxOfOrNull { f ->
            f.codigo.filter { it.isDigit() }.toIntOrNull() ?: 0
        } ?: 0
        return "FER-" + (maior + 1).toString().padStart(3, '0')
    }

    fun salvar(f: Ferramenta) = viewModelScope.launch {
        val codigo = f.codigo.ifBlank { proximoCodigo() }
        dao.salvar(f.copy(codigo = codigo, atualizadoEm = System.currentTimeMillis()))
    }

    fun apagar(f: Ferramenta) = viewModelScope.launch { dao.apagar(f) }

    val kits: StateFlow<List<Kit>> =
        kitDao.todos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Salva o kit e devolve o id dele em [aoSalvar] (útil para já vincular a ferramenta ao kit novo). */
    fun salvarKit(k: Kit, aoSalvar: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = kitDao.salvar(k.copy(nome = k.nome.trim(), local = k.local.trim()))
        aoSalvar(if (k.id != 0L) k.id else id)
    }

    /** Atualiza várias ferramentas de uma vez (ex.: emprestar ou devolver um kit inteiro). */
    fun atualizarVarias(lista: List<Ferramenta>) = viewModelScope.launch {
        val agora = System.currentTimeMillis()
        lista.forEach { dao.salvar(it.copy(atualizadoEm = agora)) }
    }

    /** Cadastra várias ferramentas de uma vez, com códigos em sequência. */
    fun salvarVarias(novas: List<Ferramenta>) = viewModelScope.launch {
        var n = ferramentas.value.maxOfOrNull { f -> f.codigo.filter { it.isDigit() }.toIntOrNull() ?: 0 } ?: 0
        val agora = System.currentTimeMillis()
        novas.forEach { f ->
            n += 1
            dao.salvar(f.copy(codigo = "FER-" + n.toString().padStart(3, '0'), criadoEm = agora, atualizadoEm = agora))
        }
    }

    /** Apaga o kit; as ferramentas dele continuam cadastradas, como avulsas. */
    fun apagarKit(k: Kit) = viewModelScope.launch {
        dao.soltarDoKit(k.id)
        kitDao.apagar(k)
    }
}
