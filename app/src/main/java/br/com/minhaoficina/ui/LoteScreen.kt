@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.minhaoficina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.minhaoficina.data.CATEGORIAS
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.Kit
import br.com.minhaoficina.ui.theme.Rotulo

/**
 * Cadastro de várias ferramentas de uma vez dentro de um kit.
 * Ex.: nome base "Chave" e quantidade 3 → Chave 01, Chave 02, Chave 03.
 */
@Composable
fun LoteScreen(
    kit: Kit,
    onFechar: () -> Unit,
    onSalvar: (List<Ferramenta>) -> Unit,
) {
    val nomes: SnapshotStateList<String> = rememberSaveable(
        saver = listSaver<SnapshotStateList<String>, String>(save = { it.toList() }, restore = { it.toMutableStateList() }),
    ) { mutableStateListOf("", "", "") }
    var base by rememberSaveable { mutableStateOf("") }
    var qtd by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Manual") }
    var marca by rememberSaveable { mutableStateOf("") }
    var local by rememberSaveable { mutableStateOf(kit.local) }

    val validos = nomes.map { it.trim() }.filter { it.isNotEmpty() }

    fun gerar() {
        val n = qtd.toIntOrNull()?.coerceIn(1, 100) ?: return
        val b = base.trim().ifEmpty { return }
        val digitos = if (n >= 100) 3 else 2
        // Substitui as linhas vazias e acrescenta as geradas
        nomes.removeAll { it.isBlank() }
        repeat(n) { i -> nomes.add("$b ${(i + 1).toString().padStart(digitos, '0')}") }
        base = ""; qtd = ""
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Adicionar ferramentas", fontWeight = FontWeight.ExtraBold)
                        Text(kit.nome, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onFechar) { Icon(Icons.Outlined.Close, "Fechar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = {
                        onSalvar(validos.map { nome ->
                            Ferramenta(nome = nome, marca = marca.trim(), categoria = categoria, local = local.trim(), kitId = kit.id)
                        })
                    },
                    enabled = validos.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
                ) {
                    Text(
                        when (validos.size) {
                            0 -> "Digite ao menos um nome"
                            1 -> "Cadastrar 1 ferramenta"
                            else -> "Cadastrar ${validos.size} ferramentas"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BlocoLote("GERAR EM SEQUÊNCIA") {
                Text(
                    "Para itens numerados, como Chave 01, Chave 02, Chave 03.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = base, onValueChange = { base = it },
                        label = { Text("Nome base") }, placeholder = { Text("Chave") },
                        singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresLote(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1.6f),
                    )
                    OutlinedTextField(
                        value = qtd, onValueChange = { v -> qtd = v.filter { it.isDigit() }.take(3) },
                        label = { Text("Qtd.") }, placeholder = { Text("3") },
                        singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresLote(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedButton(
                    onClick = { gerar() },
                    enabled = base.isNotBlank() && (qtd.toIntOrNull() ?: 0) > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gerar lista")
                }
            }

            BlocoLote("FERRAMENTAS (${validos.size})") {
                nomes.forEachIndexed { i, nome ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = nome, onValueChange = { nomes[i] = it },
                            placeholder = { Text("Nome da ferramenta ${i + 1}") },
                            singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresLote(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { if (nomes.size > 1) nomes.removeAt(i) else nomes[0] = "" }) {
                            Icon(Icons.Outlined.Close, "Remover linha", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                TextButton(onClick = { nomes.add("") }) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Adicionar linha")
                }
            }

            BlocoLote("IGUAL PARA TODAS") {
                SeletorCategoriaLote(categoria) { categoria = it }
                Campo("Marca", marca, { marca = it }, placeholder = "Ex.: Tramontina")
                Campo("Onde fica", local, { local = it }, placeholder = "Ex.: Maleta azul")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BlocoLote(titulo: String, conteudo: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = Rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
            conteudo()
        }
    }
}

@Composable
private fun coresLote() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
)

@Composable
private fun SeletorCategoriaLote(atual: String, onEscolher: (String) -> Unit) {
    var aberto by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = aberto, onExpandedChange = { aberto = it }) {
        OutlinedTextField(
            value = atual, onValueChange = {}, readOnly = true,
            label = { Text("Categoria") },
            leadingIcon = { Icon(iconeCategoria(atual), null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            shape = RoundedCornerShape(12.dp), colors = coresLote(),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            CATEGORIAS.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    leadingIcon = { Icon(iconeCategoria(c), null) },
                    onClick = { onEscolher(c); aberto = false },
                )
            }
        }
    }
}
