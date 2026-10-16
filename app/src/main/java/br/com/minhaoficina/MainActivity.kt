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
import br.com.minhaoficina.ui.KitDialog
import br.com.minhaoficina.ui.KitScreen
import br.com.minhaoficina.ui.ListaScreen
import br.com.minhaoficina.ui.LoteScreen
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

/** -1 = nenhuma; 0 = novo; >0 = id do item */
private const val NENHUM = -1L

@Composable
fun OficinaApp(vm: OficinaViewModel = viewModel()) {
    val lista by vm.ferramentas.collectAsStateWithLifecycle()
    val kits by vm.kits.collectAsStateWithLifecycle()
    var editandoId by rememberSaveable { mutableStateOf(NENHUM) }
    var detalheId by rememberSaveable { mutableStateOf(NENHUM) }
    var kitAbertoId by rememberSaveable { mutableStateOf(NENHUM) }
    var kitEditandoId by rememberSaveable { mutableStateOf(NENHUM) }
    var loteKitId by rememberSaveable { mutableStateOf(NENHUM) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val kitAberto = kits.firstOrNull { it.id == kitAbertoId }

    fun avisar(msg: String) { scope.launch { snack.showSnackbar(msg) } }

    val kitLote = kits.firstOrNull { it.id == loteKitId }
    if (kitLote != null) {
        BackHandler { loteKitId = NENHUM }
        LoteScreen(
            kit = kitLote,
            onFechar = { loteKitId = NENHUM },
            onSalvar = { novas ->
                vm.salvarVarias(novas)
                loteKitId = NENHUM
                avisar(if (novas.size == 1) "1 ferramenta adicionada ao kit" else "${novas.size} ferramentas adicionadas ao kit")
            },
        )
        return
    }

    if (editandoId != NENHUM) {
        val atual: Ferramenta? = lista.firstOrNull { it.id == editandoId }
        BackHandler { editandoId = NENHUM }
        EditorScreen(
            inicial = atual,
            kits = kits,
            kitPadrao = kitAberto?.id,
            onCriarKit = { k, aoSalvar -> vm.salvarKit(k, aoSalvar) },
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

    if (kitAberto != null) {
        BackHandler { kitAbertoId = NENHUM }
        KitScreen(
            kit = kitAberto,
            itens = lista.filter { it.kitId == kitAberto.id },
            snackbar = snack,
            onVoltar = { kitAbertoId = NENHUM },
            onEditarKit = { kitEditandoId = kitAberto.id },
            onApagarKit = {
                vm.apagarKit(kitAberto)
                kitAbertoId = NENHUM
                avisar("Kit apagado")
            },
            onNovaNoKit = { loteKitId = kitAberto.id },
            onAbrir = { detalheId = it.id },
        )
    } else {
        ListaScreen(
            lista = lista,
            kits = kits,
            snackbar = snack,
            onNova = { editandoId = 0L },
            onNovoKit = { kitEditandoId = 0L },
            onAbrir = { detalheId = it.id },
            onAbrirKit = { kitAbertoId = it.id },
            onExportar = {
                val envio = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Inventário Minhas Ferramentas")
                    putExtra(Intent.EXTRA_TEXT, gerarCsv(lista, kits.associate { it.id to it.nome }))
                }
                context.startActivity(Intent.createChooser(envio, "Exportar inventário"))
            },
        )
    }

    val detalhe = lista.firstOrNull { it.id == detalheId }
    if (detalhe != null) {
        DetalheSheet(
            f = detalhe,
            kitNome = kits.firstOrNull { it.id == detalhe.kitId }?.nome,
            onFechar = { detalheId = NENHUM },
            onEditar = { detalheId = NENHUM; editandoId = detalhe.id },
            onSalvar = { f, msg -> vm.salvar(f); avisar(msg) },
        )
    }

    if (kitEditandoId != NENHUM) {
        val kitAtual = kits.firstOrNull { it.id == kitEditandoId }
        KitDialog(
            inicial = kitAtual,
            onFechar = { kitEditandoId = NENHUM },
            onSalvar = { k ->
                vm.salvarKit(k)
                kitEditandoId = NENHUM
                avisar(if (kitAtual == null) "Kit criado" else "Kit atualizado")
            },
        )
    }
}
