package br.com.minhaoficina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.minhaoficina.data.CATEGORIAS
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.status
import br.com.minhaoficina.data.Situacao
import br.com.minhaoficina.ui.theme.Lima
import br.com.minhaoficina.ui.theme.Rotulo
import br.com.minhaoficina.ui.theme.TintaLima
import br.com.minhaoficina.ui.theme.coresDe

@Composable
fun ListaScreen(
    lista: List<Ferramenta>,
    snackbar: SnackbarHostState,
    onNova: () -> Unit,
    onAbrir: (Ferramenta) -> Unit,
    onExportar: () -> Unit,
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("") } // "" = todas
    var categoria by rememberSaveable { mutableStateOf("") }

    val q = busca.trim().lowercase()
    val visiveis = lista
        .filter { filtro.isEmpty() || it.situacao == filtro }
        .filter { categoria.isEmpty() || it.categoria == categoria }
        .filter { f ->
            q.isEmpty() || listOf(f.nome, f.marca, f.local, f.emprestadoPara, f.codigo)
                .joinToString(" ").lowercase().contains(q)
        }
        .sortedWith(compareBy<Ferramenta> { if (it.status == Situacao.EMPRESTADA) 0 else 1 }.thenBy { it.nome.lowercase() })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        // Cabeçalho e busca ficam fixos no topo durante a rolagem
        topBar = {
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            ) {
                Cabecalho(total = lista.size, onExportar = onExportar)
                CampoBusca(busca, { busca = it })
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNova,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Nova ferramenta", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = pad.calculateTopPadding() + 4.dp,
                bottom = pad.calculateBottomPadding() + 96.dp,
            ),
        ) {
            item {
                Resumo(lista)
                Spacer(Modifier.height(16.dp))
            }
            item {
                Filtros(
                    lista = lista, filtro = filtro, onFiltro = { filtro = it },
                    categoria = categoria, onCategoria = { categoria = it },
                )
                Spacer(Modifier.height(10.dp))
            }

            when {
                lista.isEmpty() -> item {
                    Vazio(Icons.Outlined.Build, "Seu inventário está vazio",
                        "Toque em Nova ferramenta para cadastrar a primeira e registrar onde ela fica guardada.")
                }
                visiveis.isEmpty() -> item {
                    Vazio(Icons.Outlined.Search, "Nenhum resultado", "Tente outra busca ou troque o filtro.")
                }
                else -> itemsIndexed(visiveis, key = { _, f -> f.id }) { i, f ->
                    val topo = if (i == 0) 16.dp else 0.dp
                    val base = if (i == visiveis.lastIndex) 16.dp else 0.dp
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = topo, topEnd = topo, bottomStart = base, bottomEnd = base),
                        tonalElevation = 0.dp,
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
}

@Composable
private fun Cabecalho(total: Int, onExportar: () -> Unit) {
    var menu by rememberSaveable { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Lima),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Build, null, tint = TintaLima, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Minhas Ferramentas", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(coresDe(Situacao.DISPONIVEL).texto))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Salvo neste aparelho · $total ${if (total == 1) "item" else "itens"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, "Mais opções") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Exportar inventário (CSV)") },
                    leadingIcon = { Icon(Icons.Outlined.FileUpload, null) },
                    onClick = { menu = false; onExportar() },
                )
            }
        }
    }
}

@Composable
private fun CampoBusca(valor: String, onValor: (String) -> Unit) {
    TextField(
        value = valor,
        onValueChange = onValor,
        placeholder = { Text("Buscar ferramenta, marca ou local") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        trailingIcon = {
            if (valor.isNotEmpty()) IconButton(onClick = { onValor("") }) { Icon(Icons.Outlined.Close, "Limpar busca") }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Resumo(lista: List<Ferramenta>) {
    val total = lista.size
    val cont = Situacao.entries.associateWith { s -> lista.count { it.status == s } }
    val investido = lista.sumOf { it.valor }
    val suave = TintaLima.copy(alpha = 0.62f)
    // Cores sobre o fundo verde-limão
    fun cor(s: Situacao) = when (s) {
        Situacao.DISPONIVEL -> TintaLima
        Situacao.EMPRESTADA -> TintaLima.copy(alpha = 0.42f)
        Situacao.MANUTENCAO -> Color.White
    }

    Surface(shape = RoundedCornerShape(22.dp), color = Lima, contentColor = TintaLima) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("INVENTÁRIO", style = Rotulo, color = suave)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$total", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 44.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("ferramentas", color = suave, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 7.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("VALOR INVESTIDO", style = Rotulo, color = suave)
                    Text(reais(investido), style = MaterialTheme.typography.titleMedium)
                }
            }
            // Barra proporcional por situação
            Row(
                Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(TintaLima.copy(alpha = 0.12f)),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (total > 0) Situacao.entries.forEach { s ->
                    val n = cont[s] ?: 0
                    if (n > 0) Box(Modifier.weight(n.toFloat()).fillMaxSize().background(cor(s)))
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Situacao.entries.forEach { s ->
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(cor(s)))
                            Spacer(Modifier.width(6.dp))
                            Text(s.plural, style = MaterialTheme.typography.bodySmall, color = suave)
                        }
                        Text("${cont[s] ?: 0}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun Filtros(
    lista: List<Ferramenta>,
    filtro: String, onFiltro: (String) -> Unit,
    categoria: String, onCategoria: (String) -> Unit,
) {
    var menuCat by rememberSaveable { mutableStateOf(false) }
    val opcoes = listOf("" to "Todas") + Situacao.entries.map { it.chave to it.plural }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opcoes, key = { it.first }) { (chave, rotulo) ->
            val n = if (chave.isEmpty()) lista.size else lista.count { it.situacao == chave }
            FilterChip(
                selected = filtro == chave,
                onClick = { onFiltro(chave) },
                label = { Text("$rotulo  $n") },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
        item {
            Box {
                FilterChip(
                    selected = categoria.isNotEmpty(),
                    onClick = { menuCat = true },
                    label = { Text(categoria.ifEmpty { "Categoria" }) },
                    leadingIcon = { Icon(Icons.Outlined.FilterList, null, Modifier.size(18.dp)) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
                DropdownMenu(expanded = menuCat, onDismissRequest = { menuCat = false }) {
                    DropdownMenuItem(text = { Text("Todas as categorias") }, onClick = { onCategoria(""); menuCat = false })
                    CATEGORIAS.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            leadingIcon = { Icon(iconeCategoria(c), null) },
                            onClick = { onCategoria(c); menuCat = false },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconeCategoria(cat: String, tamanho: Int = 44) {
    Box(
        Modifier.size(tamanho.dp).clip(RoundedCornerShape((tamanho * 0.28f).dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(iconeCategoria(cat), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size((tamanho * 0.5f).dp))
    }
}

@Composable
fun ChipSituacao(s: Situacao) {
    val c = coresDe(s)
    Text(
        s.rotulo,
        color = c.texto,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(CircleShape).background(c.fundo).padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
private fun ItemFerramenta(f: Ferramenta, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconeCategoria(f.categoria)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(f.nome, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (f.status == Situacao.EMPRESTADA) {
                val atrasada = (diasDesde(f.emprestadoEm) ?: 0) > DIAS_ALERTA
                val cor = coresDe(if (atrasada) Situacao.MANUTENCAO else Situacao.EMPRESTADA).texto
                Text(
                    "Com ${f.emprestadoPara.ifBlank { "alguém" }} · ${textoDias(f.emprestadoEm)}".trimEnd(' ', '·'),
                    color = cor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    listOf(f.marca, f.local).filter { it.isNotBlank() }.joinToString(" · ").ifEmpty { f.categoria },
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ChipSituacao(f.status)
            Text(f.codigo, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun Vazio(icone: ImageVector, titulo: String, texto: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Lima),
                contentAlignment = Alignment.Center,
            ) { Icon(icone, null, tint = TintaLima) }
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(texto, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
