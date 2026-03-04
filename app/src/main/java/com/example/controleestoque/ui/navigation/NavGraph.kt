package com.example.controleestoque.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.controleestoque.ui.screens.configuracoes.ConfiguracoesScreen
import com.example.controleestoque.ui.screens.home.HomeScreen
import com.example.controleestoque.ui.screens.movimentacao.MovimentacaoScreen
import com.example.controleestoque.ui.screens.produto.ProdutoFormScreen
import com.example.controleestoque.ui.screens.relatorios.RelatoriosScreen

/**
 * Grafo de navegação do aplicativo.
 * Define todas as rotas e como navegar entre as telas.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Tela inicial: lista de produtos
        composable(Screen.Home.route) {
            HomeScreen(
                onNovoProduto = { navController.navigate(Screen.ProdutoForm.createRoute()) },
                onEditarProduto = { id -> navController.navigate(Screen.ProdutoForm.createRoute(id)) },
                onMovimentacoes = { navController.navigate(Screen.Movimentacoes.route) },
                onRelatorios = { navController.navigate(Screen.Relatorios.route) },
                onConfiguracoes = { navController.navigate(Screen.Configuracoes.route) }
            )
        }

        // Tela de cadastro/edição de produto
        composable(
            route = Screen.ProdutoForm.route,
            arguments = listOf(navArgument("produtoId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val produtoId = backStackEntry.arguments?.getLong("produtoId") ?: 0L
            ProdutoFormScreen(
                produtoId = produtoId,
                onVoltar = { navController.popBackStack() }
            )
        }

        // Tela de movimentações
        composable(Screen.Movimentacoes.route) {
            MovimentacaoScreen(onVoltar = { navController.popBackStack() })
        }

        // Tela de relatórios
        composable(Screen.Relatorios.route) {
            RelatoriosScreen(onVoltar = { navController.popBackStack() })
        }

        // Tela de configurações
        composable(Screen.Configuracoes.route) {
            ConfiguracoesScreen(onVoltar = { navController.popBackStack() })
        }
    }
}
