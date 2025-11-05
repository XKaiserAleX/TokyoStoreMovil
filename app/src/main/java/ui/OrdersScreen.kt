package ui

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.firestore.FirebaseFirestore
import navigation.Screen
import com.google.firebase.firestore.DocumentChange

// --- DATA CLASS ---
data class Pedido(
    val id: String,
    val fecha: String,
    val estado: String,
    val total: String
)

// --- BOTTOM NAVIGATION ITEMS ---
data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Pedidos", Icons.Default.ListAlt, Screen.Orders.route),
    BottomNavItem("Historial", Icons.Default.History, Screen.History.route),
    BottomNavItem("Perfil", Icons.Default.Person, Screen.Profile.route)
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val pedidos = remember { mutableStateListOf<Pedido>() }

    //Escucha en tiempo real la colección "pedidos"
    LaunchedEffect(Unit) {
        db.collection("pedidos").addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error al escuchar cambios: ${e.message}")
                return@addSnapshotListener
            }

            // Limpiar la lista antes de volver a llenarla
            pedidos.clear()

            snapshots?.forEach { doc ->
                pedidos.add(
                    Pedido(
                        id = doc.getString("id") ?: "",
                        fecha = doc.getString("fecha") ?: "",
                        estado = doc.getString("estado") ?: "",
                        total = doc.getString("total") ?: ""
                    )
                )
            }

            // Detectar qué cambió (MODIFIED, ADDED, REMOVED)
            snapshots?.documentChanges?.forEach { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED -> {
                        val pedidoId = change.document.getString("id") ?: return@forEach
                        enviarNotificacionLocal(context, pedidoId, "Nuevo pedido agregado")
                    }
                    DocumentChange.Type.MODIFIED -> {
                        val pedidoId = change.document.getString("id") ?: return@forEach
                        val nuevoEstado = change.document.getString("estado") ?: return@forEach
                        enviarNotificacionLocal(context, pedidoId, "Actualizado a: $nuevoEstado")
                    }
                    DocumentChange.Type.REMOVED -> {
                        val pedidoId = change.document.getString("id") ?: return@forEach
                        enviarNotificacionLocal(context, pedidoId, "Pedido eliminado")
                    }
                }
            }
        }
    }


    // Para la navegación inferior
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Pedidos Activos") }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (pedidos.isEmpty()) {
            EmptyOrdersView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidos) { pedido ->
                    PedidoCardMejorado(
                        pedido = pedido,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            navController.navigate(Screen.OrderDetail.createRoute(pedido.id))
                        },
                        onEstadoChange = {
                            val nuevoEstado = when (pedido.estado) {
                                "En preparación" -> "En despacho"
                                "En despacho" -> "Entregado"
                                else -> "En preparación"
                            }

                            // 🔄 Actualiza en Firebase
                            db.collection("pedidos")
                                .whereEqualTo("id", pedido.id)
                                .get()
                                .addOnSuccessListener { result ->
                                    for (doc in result) {
                                        db.collection("pedidos")
                                            .document(doc.id)
                                            .update("estado", nuevoEstado)
                                    }
                                }

                            //Notificación local
                            enviarNotificacionLocal(context, pedido.id, nuevoEstado)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PedidoCardMejorado(
    pedido: Pedido,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEstadoChange: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusIcon = when (pedido.estado) {
                "En preparación" -> Icons.Default.SoupKitchen
                "En despacho" -> Icons.Default.LocalShipping
                else -> Icons.Default.ReceiptLong
            }

            Icon(
                imageVector = statusIcon,
                contentDescription = "Estado del pedido",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pedido.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssistChip(
                    onClick = onEstadoChange,
                    label = { Text(pedido.estado) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = pedido.total,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EmptyOrdersView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Inventory2,
                contentDescription = "No hay pedidos",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes pedidos activos",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Cuando realices tu primera compra, la verás aquí.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun enviarNotificacionLocal(context: Context, pedidoId: String, nuevoEstado: String) {
    val channelId = "pedidos_channel"

    // Crear canal de notificaciones
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Actualizaciones de pedidos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifica cuando el pedido cambia de estado"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_dialog_info)
        .setContentTitle("Pedido #$pedidoId actualizado")
        .setContentText("Nuevo estado: $nuevoEstado")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(pedidoId.hashCode(), notification)
}
