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

    fun salvarKit(k: Kit) = viewModelScope.launch { kitDao.salvar(k.copy(nome = k.nome.trim(), local = k.local.trim())) }

    /** Apaga o kit; as ferramentas dele continuam cadastradas, como avulsas. */
    fun apagarKit(k: Kit) = viewModelScope.launch {
        dao.soltarDoKit(k.id)
        kitDao.apagar(k)
    }
}
