package com.example.controleestoque.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controleestoque.data.local.entity.Produto
import com.example.controleestoque.ui.theme.*
import com.example.controleestoque.utils.ConfiguracoesManager
import com.example.controleestoque.utils.DateUtils

/**
 * Card que representa um produto na lista.
 * Exibe indicadores visuais de validade e estoque baixo.
 */
@Composable
fun ProdutoCard(
    produto: Produto,
    diasAntesVencimento: Int,
    quantidadeMinimaGlobal: Int,
    onEditar: () -> Unit,
    onDeletar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vencido = DateUtils.estaVencido(produto.dataValidade)
    val proximoVencer = DateUtils.estaProximoDoVencimento(produto.dataValidade, diasAntesVencimento)
    val qtdMin = if (produto.quantidadeMinima > 0) produto.quantidadeMinima else quantidadeMinimaGlobal
    val estoqueBaixo = produto.quantidadeAtual <= qtdMin

    val (bgColor, borderColor) = when {
        vencido -> ColorVencidoBg to ColorVencido
        proximoVencer -> ColorProximoVencerBg to ColorProximoVencer
        estoqueBaixo -> ColorEstoqueBaixoBg to ColorEstoqueBaixo
        else -> Color.Transparent to Color.Transparent
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bgColor != Color.Transparent) bgColor
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qtd: ${produto.quantidadeAtual} ${produto.unidade}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (estoqueBaixo) ColorEstoqueBaixo else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Validade: ${DateUtils.formatarData(produto.dataValidade)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        vencido -> ColorVencido
                        proximoVencer -> ColorProximoVencer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (produto.categoria.isNotBlank()) {
                    Text(
                        text = produto.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Badge de status
                when {
                    vencido -> StatusBadge("VENCIDO", ColorVencido)
                    proximoVencer -> StatusBadge(
                        "Vence em ${DateUtils.diasParaVencer(produto.dataValidade)}d",
                        ColorProximoVencer
                    )
                    estoqueBaixo -> StatusBadge("ESTOQUE BAIXO", ColorEstoqueBaixo)
                    else -> {}
                }
            }

            Column {
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeletar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Badge de status colorido */
@Composable
private fun StatusBadge(texto: String, cor: Color) {
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .background(cor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = texto,
            color = cor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
