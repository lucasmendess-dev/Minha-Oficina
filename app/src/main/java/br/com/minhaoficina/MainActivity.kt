package br.com.minhaoficina

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.ui.DetalheSheet
import br.com.minhaoficina.ui.EditorScreen
import br.com.minhaoficina.ui.ListaScreen
import br.com.minhaoficina.ui.gerarCsv
import br.com.minhaoficina.ui.theme.OficinaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OficinaTheme { OficinaApp() }
        }
    }
}

/** -1 = nenhuma; 0 = nova ferramenta; >0 = id da ferramenta em edição */
private const val NENHUM = -1L

@Composable
fun OficinaApp(vm: OficinaViewModel = viewModel()) {
    val lista by vm.ferramentas.collectAsStateWithLifecycle()
    var editandoId by rememberSaveable { mutableStateOf(NENHUM) }
    var detalheId by rememberSaveable { mutableStateOf(NENHUM) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun avisar(msg: String) { scope.launch { snack.showSnackbar(msg) } }

    if (editandoId != NENHUM) {
        val atual: Ferramenta? = lista.firstOrNull { it.id == editandoId }
        BackHandler { editandoId = NENHUM }
        EditorScreen(
            inicial = atual,
            onFechar = { editandoId = NENHUM },
            onSalvar = { f ->
                vm.salvar(f)
                editandoId = NENHUM
                avisar(if (atual == null) "Ferramenta cadastrada" else "Alterações salvas")
            },
            onApagar = { f ->
                vm.apagar(f)
                editandoId = NENHUM
                avisar("Ferramenta apagada")
            },
        )
        return
    }

    ListaScreen(
        lista = lista,
        snackbar = snack,
        onNova = { editandoId = 0L },
        onAbrir = { detalheId = it.id },
        onExportar = {
            val envio = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Inventário Minha Oficina")
                putExtra(Intent.EXTRA_TEXT, gerarCsv(lista))
            }
            context.startActivity(Intent.createChooser(envio, "Exportar inventário"))
        },
    )

    val detalhe = lista.firstOrNull { it.id == detalheId }
    if (detalhe != null) {
        DetalheSheet(
            f = detalhe,
            onFechar = { detalheId = NENHUM },
            onEditar = { detalheId = NENHUM; editandoId = detalhe.id },
            onSalvar = { f, msg -> vm.salvar(f); avisar(msg) },
        )
    }
}
