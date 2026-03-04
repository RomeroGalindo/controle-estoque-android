package com.example.controleestoque.ui.navigation

/**
 * Define as rotas de navegação do aplicativo.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ProdutoForm : Screen("produto_form/{produtoId}") {
        fun createRoute(produtoId: Long = 0L) = "produto_form/$produtoId"
    }
    object Movimentacoes : Screen("movimentacoes")
    object Relatorios : Screen("relatorios")
    object Configuracoes : Screen("configuracoes")
}
