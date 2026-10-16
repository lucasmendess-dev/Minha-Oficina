@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.minhaoficina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.Kit
import br.com.minhaoficina.data.Situacao
import br.com.minhaoficina.data.status
import br.com.minhaoficina.ui.theme.Lima
import br.com.minhaoficina.ui.theme.Rotulo
import br.com.minhaoficina.ui.theme.TintaLima

/** Tela de um kit: mostra as ferramentas dele e permite adicionar novas. */
@Composable
fun KitScreen(
    kit: Kit,
    itens: List<Ferramenta>,
    snackbar: SnackbarHostState,
    onVoltar: () -> Unit,
    onEditarKit: () -> Unit,
    onApagarKit: () -> Unit,
    onNovaNoKit: () -> Unit,
    onAbrir: (Ferramenta) -> Unit,
) {
    var confirmar by rememberSaveable { mutableStateOf(false) }
    val ordenados = itens.sortedBy { it.nome.lowercase() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Kit", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar") } },
                actions = {
                    IconButton(onClick = onEditarKit) { Icon(Icons.Outlined.Edit, "Editar kit") }
                    IconButton(onClick = { confirmar = true }) { Icon(Icons.Outlined.Delete, "Apagar kit") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = onNovaNoKit,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
                ) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar ferramenta ao kit", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = pad.calculateTopPadding() + 4.dp,
                bottom = pad.calculateBottomPadding() + 16.dp,
            ),
        ) {
            item {
                CabecalhoKit(kit, itens)
                Spacer(Modifier.height(16.dp))
                Secao("FERRAMENTAS DO KIT")
            }
            if (ordenados.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                        Text(
                            "Este kit ainda está vazio. Toque em \"Adicionar ferramenta ao kit\" para cadastrar a primeira, " +
                                "ou edite uma ferramenta existente e escolha este kit.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                        )
                    }
                }
            } else {
                itemsIndexed(ordenados, key = { _, f -> f.id }) { i, f ->
                    val topo = if (i == 0) 16.dp else 0.dp
                    val base = if (i == ordenados.lastIndex) 16.dp else 0.dp
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = topo, topEnd = topo, bottomStart = base, bottomEnd = base),
                        shadowElevation = 1.dp,
                    ) {
                        Column {
                            if (i > 0) HorizontalDivider(Modifier.padding(start = 70.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            ItemFerramenta(f) { onAbrir(f) }
                        }
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Apagar o kit \"${kit.nome}\"?") },
            text = {
                Text(
                    if (itens.isEmpty()) "O kit está vazio."
                    else "As ${itens.size} ferramentas dele continuam cadastradas e passam a aparecer como avulsas."
                )
            },
            confirmButton = {
                Button(
                    onClick = { confirmar = false; onApagarKit() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) { Text("Apagar kit") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun CabecalhoKit(kit: Kit, itens: List<Ferramenta>) {
    val cont = Situacao.entries.associateWith { s -> itens.count { it.status == s } }
    val suave = TintaLima.copy(alpha = 0.62f)
    Surface(shape = RoundedCornerShape(22.dp), color = Lima, contentColor = TintaLima) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(TintaLima),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.HomeRepairService, null, tint = Lima, modifier = Modifier.size(26.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(kit.nome, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 26.sp)
                    if (kit.local.isNotBlank()) Text(kit.local, color = suave, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("ITENS", style = Rotulo, color = suave)
                    Text("${itens.size}", style = MaterialTheme.typography.titleMedium)
                }
                Situacao.entries.forEach { s ->
                    Column(Modifier.weight(1f)) {
                        Text(s.plural.uppercase(), style = Rotulo, color = suave, maxLines = 1)
                        Text("${cont[s] ?: 0}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            val valor = itens.sumOf { it.valor }
            if (valor > 0) Text("Valor do kit: ${reais(valor)}", color = suave, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Diálogo para criar ou editar um kit. */
@Composable
fun KitDialog(inicial: Kit?, onFechar: () -> Unit, onSalvar: (Kit) -> Unit) {
    val chave = inicial?.id ?: 0L
    var nome by rememberSaveable(chave) { mutableStateOf(inicial?.nome ?: "") }
    var local by rememberSaveable(chave) { mutableStateOf(inicial?.local ?: "") }
    var erro by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(if (inicial == null) "Novo kit" else "Editar kit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo("Nome do kit", nome, { nome = it; erro = false }, placeholder = "Ex.: Kit chave de boca",
                    erro = if (erro) "Dê um nome para o kit." else null)
                Campo("Onde fica", local, { local = it }, placeholder = "Ex.: Maleta azul, armário 1")
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nome.isBlank()) erro = true
                else onSalvar((inicial ?: Kit()).copy(nome = nome, local = local))
            }) { Text(if (inicial == null) "Criar kit" else "Salvar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } },
    )
}
