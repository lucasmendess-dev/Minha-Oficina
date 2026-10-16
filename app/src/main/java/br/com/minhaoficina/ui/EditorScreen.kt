@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.minhaoficina.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.minhaoficina.data.CATEGORIAS
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.Kit
import br.com.minhaoficina.data.Situacao
import br.com.minhaoficina.ui.theme.Rotulo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun EditorScreen(
    inicial: Ferramenta?,
    kits: List<Kit>,
    kitPadrao: Long?,
    onCriarKit: (Kit, (Long) -> Unit) -> Unit,
    onFechar: () -> Unit,
    onSalvar: (Ferramenta) -> Unit,
    onApagar: (Ferramenta) -> Unit,
) {
    val chave = inicial?.id ?: 0L
    var nome by rememberSaveable(chave) { mutableStateOf(inicial?.nome ?: "") }
    var marca by rememberSaveable(chave) { mutableStateOf(inicial?.marca ?: "") }
    var categoria by rememberSaveable(chave) { mutableStateOf(inicial?.categoria ?: CATEGORIAS.first()) }
    var local by rememberSaveable(chave) { mutableStateOf(inicial?.local ?: "") }
    var valor by rememberSaveable(chave) { mutableStateOf(valorParaTexto(inicial?.valor ?: 0.0)) }
    var situacao by rememberSaveable(chave) { mutableStateOf(inicial?.situacao ?: Situacao.DISPONIVEL.chave) }
    var para by rememberSaveable(chave) { mutableStateOf(inicial?.emprestadoPara ?: "") }
    var desde by rememberSaveable(chave) { mutableStateOf(inicial?.emprestadoEm ?: "") }
    var notas by rememberSaveable(chave) { mutableStateOf(inicial?.notas ?: "") }
    // -1 = sem kit (ferramenta avulsa)
    var kitId by rememberSaveable(chave) { mutableStateOf(inicial?.kitId ?: kitPadrao ?: -1L) }
    var criandoKit by rememberSaveable { mutableStateOf(false) }
    // Id do kit recém-criado: aceito mesmo antes de a lista de kits atualizar
    var kitNovoId by rememberSaveable { mutableStateOf(-1L) }
    var erroNome by rememberSaveable { mutableStateOf(false) }
    var confirmarApagar by rememberSaveable { mutableStateOf(false) }

    fun salvar() {
        if (nome.isBlank()) { erroNome = true; return }
        val emp = situacao == Situacao.EMPRESTADA.chave
        val base = inicial ?: Ferramenta()
        onSalvar(
            base.copy(
                nome = nome.trim(), marca = marca.trim(), categoria = categoria, local = local.trim(),
                valor = parseValor(valor), situacao = situacao,
                emprestadoPara = if (emp) para.trim() else "",
                emprestadoEm = if (emp) desde.ifBlank { hojeIso() } else "",
                notas = notas.trim(),
                kitId = kitId.takeIf { id -> id > 0 && (id == kitNovoId || kits.any { it.id == id }) },
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (inicial == null) "Nova ferramenta" else "Editar ferramenta", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onFechar) { Icon(Icons.Outlined.Close, "Fechar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        // Botão Salvar fixo na parte de baixo; sobe junto com o teclado
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = { salvar() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
                ) { Text(if (inicial == null) "Cadastrar ferramenta" else "Salvar alterações", style = MaterialTheme.typography.labelLarge) }
            }
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Grupo("IDENTIFICAÇÃO") {
                Campo("Nome", nome, { nome = it; erroNome = false }, placeholder = "Ex.: Furadeira de impacto",
                    erro = if (erroNome) "Dê um nome para a ferramenta." else null)
                Campo("Marca / modelo", marca, { marca = it }, placeholder = "Ex.: Bosch GSB 13 RE")
                SeletorCategoria(categoria) { categoria = it }
                SeletorKit(kits, kitId, onEscolher = { kitId = it }, onNovo = { criandoKit = true })
            }
            Grupo("ARMAZENAMENTO E VALOR") {
                Campo("Onde fica", local, { local = it }, placeholder = "Ex.: Armário, prateleira 2")
                Campo("Valor pago (R$)", valor, { valor = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    placeholder = "0,00", teclado = KeyboardType.Decimal)
            }
            Grupo("SITUAÇÃO") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Situacao.entries.forEachIndexed { i, s ->
                        SegmentedButton(
                            selected = situacao == s.chave,
                            onClick = {
                                situacao = s.chave
                                if (s == Situacao.EMPRESTADA && desde.isBlank()) desde = hojeIso()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = Situacao.entries.size),
                            label = { Text(s.rotulo, maxLines = 1) },
                        )
                    }
                }
                if (situacao == Situacao.EMPRESTADA.chave) {
                    Campo("Emprestada para", para, { para = it }, placeholder = "Nome da pessoa")
                    CampoData("Desde", desde) { desde = it }
                }
            }
            Grupo("OBSERVAÇÕES") {
                OutlinedTextField(
                    value = notas, onValueChange = { notas = it },
                    placeholder = { Text("Voltagem, acessórios, número de série…") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = coresCampo(),
                )
            }

            if (inicial != null) {
                if (confirmarApagar) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Apagar esta ferramenta de vez? Não dá para desfazer.", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { confirmarApagar = false }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                                Button(
                                    onClick = { onApagar(inicial) }, modifier = Modifier.weight(1f),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    ),
                                ) { Text("Apagar") }
                            }
                        }
                    }
                } else {
                    TextButton(onClick = { confirmarApagar = true }) {
                        Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Apagar ferramenta", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.padding(bottom = 24.dp))
        }
    }

    if (criandoKit) {
        KitDialog(
            inicial = null,
            onFechar = { criandoKit = false },
            onSalvar = { k ->
                criandoKit = false
                onCriarKit(k) { id -> kitNovoId = id; kitId = id }
            },
        )
    }
}

@Composable
private fun Grupo(titulo: String, conteudo: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = Rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
            conteudo()
        }
    }
}

@Composable
private fun coresCampo() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
)

@Composable
fun Campo(
    rotulo: String,
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String = "",
    erro: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        label = { Text(rotulo) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = erro != null,
        supportingText = if (erro != null) { { Text(erro) } } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado,
            capitalization = if (teclado == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
        ),
        shape = RoundedCornerShape(12.dp),
        colors = coresCampo(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun CampoData(rotulo: String, iso: String, onData: (String) -> Unit) {
    var aberto by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = dataBr(iso),
        onValueChange = {},
        readOnly = true,
        label = { Text(rotulo) },
        trailingIcon = { IconButton(onClick = { aberto = true }) { Icon(Icons.Outlined.CalendarMonth, "Escolher data") } },
        shape = RoundedCornerShape(12.dp),
        colors = coresCampo(),
        modifier = Modifier.fillMaxWidth(),
    )
    if (aberto) {
        val inicialMs = runCatching {
            LocalDate.parse(iso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
        val estado = rememberDatePickerState(initialSelectedDateMillis = inicialMs)
        DatePickerDialog(
            onDismissRequest = { aberto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { ms ->
                        onData(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    aberto = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { aberto = false }) { Text("Cancelar") } },
        ) { DatePicker(state = estado) }
    }
}

@Composable
private fun SeletorKit(kits: List<Kit>, atual: Long, onEscolher: (Long) -> Unit, onNovo: () -> Unit) {
    var aberto by rememberSaveable { mutableStateOf(false) }
    val nome = kits.firstOrNull { it.id == atual }?.nome ?: if (atual > 0) "Kit novo" else "Nenhum (ferramenta avulsa)"
    ExposedDropdownMenuBox(expanded = aberto, onExpandedChange = { aberto = it }) {
        OutlinedTextField(
            value = nome,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kit") },
            leadingIcon = { Icon(Icons.Outlined.HomeRepairService, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            shape = RoundedCornerShape(12.dp),
            colors = coresCampo(),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            DropdownMenuItem(
                text = { Text("Criar novo kit…", fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Outlined.Add, null) },
                onClick = { aberto = false; onNovo() },
            )
            DropdownMenuItem(text = { Text("Nenhum (ferramenta avulsa)") }, onClick = { onEscolher(-1L); aberto = false })
            kits.forEach { k ->
                DropdownMenuItem(
                    text = { Text(k.nome) },
                    leadingIcon = { Icon(Icons.Outlined.HomeRepairService, null) },
                    onClick = { onEscolher(k.id); aberto = false },
                )
            }
        }
    }
}

@Composable
private fun SeletorCategoria(atual: String, onEscolher: (String) -> Unit) {
    var aberto by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = aberto, onExpandedChange = { aberto = it }) {
        OutlinedTextField(
            value = atual,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            leadingIcon = { Icon(iconeCategoria(atual), null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            shape = RoundedCornerShape(12.dp),
            colors = coresCampo(),
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
