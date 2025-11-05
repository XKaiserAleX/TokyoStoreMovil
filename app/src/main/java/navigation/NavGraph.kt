package navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ui.LoginScreen
import ui.OrdersScreen
import ui.OrderDetailScreen
import ui.HistoryScreen
import ui.ProfileScreen
/**
 * Definimos las rutas de la app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object History : Screen("history")
    object Profile : Screen("profile") // <-- AÑADE ESTA LÍNEA
}

/**
 * Manejador de navegación principal
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Orders.route) { OrdersScreen(navController) }
        // --- AÑADE ESTE NUEVO BLOQUE ---
        composable(Screen.Profile.route) {ProfileScreen(navController)}
        composable(Screen.History.route) { HistoryScreen(navController) }
        composable(Screen.OrderDetail.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(navController, orderId)

        }

    }
}
