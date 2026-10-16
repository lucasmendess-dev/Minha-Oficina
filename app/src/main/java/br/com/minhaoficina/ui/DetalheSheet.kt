@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.minhaoficina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.minhaoficina.data.Ferramenta
import br.com.minhaoficina.data.status
import br.com.minhaoficina.data.Situacao
import br.com.minhaoficina.ui.theme.coresDe

@Composable
fun DetalheSheet(
    f: Ferramenta,
    kitNome: String?,
    onFechar: () -> Unit,
    onEditar: () -> Unit,
    onSalvar: (Ferramenta, String) -> Unit,
) {
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var emprestando by rememberSaveable(f.id) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = estado,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconeCategoria(f.categoria, tamanho = 56)
                Spacer(Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(f.nome, style = MaterialTheme.typography.titleLarge)
                    ChipSituacao(f.status)
                }
            }

            if (f.status == Situacao.EMPRESTADA) {
                val atrasada = (diasDesde(f.emprestadoEm) ?: 0) > DIAS_ALERTA
                val c = coresDe(if (atrasada) Situacao.MANUTENCAO else Situacao.EMPRESTADA)
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.fundo).padding(14.dp),
                ) {
                    Text("Com ${f.emprestadoPara.ifBlank { "alguém" }}", color = c.texto, fontWeight = FontWeight.Bold)
                    val quando = dataBr(f.emprestadoEm).ifEmpty { "data não informada" }
                    val dias = textoDias(f.emprestadoEm)
                    Text(
                        "Emprestada em $quando" + if (dias.isNotEmpty()) " · $dias" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val linhas = listOf(
                "Código" to f.codigo,
                "Kit" to (kitNome ?: ""),
                "Categoria" to f.categoria,
                "Marca / modelo" to f.marca,
                "Onde fica" to f.local,
                "Valor pago" to if (f.valor > 0) reais(f.valor) else "",
                "Cadastrada em" to dataBr(f.criadoEm),
            ).filter { it.second.isNotBlank() }
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
            ) {
                linhas.forEachIndexed { i, (rotulo, valor) ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp)) {
                        Text(rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(valor, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f))
                    }
                }
            }

            if (f.notas.isNotBlank()) {
                Text(
                    f.notas,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp),
                )
            }

            if (emprestando) {
                FormEmprestimo(
                    onCancelar = { emprestando = false },
                    onConfirmar = { para, data ->
                        onSalvar(f.copy(situacao = Situacao.EMPRESTADA.chave, emprestadoPara = para, emprestadoEm = data), "Empréstimo registrado")
                        emprestando = false
                    },
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (f.status) {
                        Situacao.DISPONIVEL -> Button(
                            onClick = { emprestando = true },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text("Emprestar")
                        }
                        else -> Button(
                            onClick = {
                                val msg = if (f.status == Situacao.EMPRESTADA) "Devolução registrada" else "Ferramenta disponível"
                                onSalvar(f.copy(situacao = Situacao.DISPONIVEL.chave, emprestadoPara = "", emprestadoEm = ""), msg)
                            },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.AssignmentReturn, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (f.status == Situacao.EMPRESTADA) "Registrar devolução" else "Voltou da manutenção")
                        }
                    }
                    OutlinedButton(onClick = onEditar, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Editar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormEmprestimo(onCancelar: () -> Unit, onConfirmar: (String, String) -> Unit) {
    var para by rememberSaveable { mutableStateOf("") }
    var data by rememberSaveable { mutableStateOf(hojeIso()) }
    var erro by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Campo("Emprestar para", para, { para = it; erro = false }, placeholder = "Nome da pessoa")
        CampoData("Data", data) { data = it }
        if (erro) Text("Informe para quem você emprestou.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Cancelar") }
            Button(
                onClick = { if (para.isBlank()) erro = true else onConfirmar(para.trim(), data) },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
            ) { Text("Confirmar") }
        }
    }
}
