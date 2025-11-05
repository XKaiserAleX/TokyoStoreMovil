package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class ProductoDetalle(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(navController: NavController, orderId: String) {
    // --- Datos de ejemplo más estructurados ---
    val productos = listOf(
        ProductoDetalle("Audífonos inalámbricos", 1, 20000.0),
        ProductoDetalle("Mouse gamer RGB", 1, 15000.0),
        ProductoDetalle("Cargador rápido USB-C", 1, 10000.0)
    )
    val subtotal = productos.sumOf { it.precioUnitario * it.cantidad }
    val envio = 3500.0 // Costo de envío de ejemplo
    val total = subtotal + envio

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle Pedido #$orderId") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- MEJORA: LazyColumn para un scroll suave si hay muchos productos ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECCIÓN 1: RESUMEN DEL PEDIDO ---
            item {
                Text("Resumen del Pedido", style = MaterialTheme.typography.titleMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Usamos un Chip para destacar el estado
                        AssistChip(
                            onClick = { /* Para tracking futuro */ },
                            label = { Text("En despacho") },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailItem(icon = Icons.Default.CalendarMonth, label = "Fecha de compra", value = "24/10/2025")
                        DetailItem(icon = Icons.Default.LocationOn, label = "Dirección de envío", value = "Av. Siempre Viva 742")
                    }
                }
            }

            // --- SECCIÓN 2: ARTÍCULOS DEL PEDIDO ---
            item {
                Text("Artículos", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        productos.forEachIndexed { index, producto ->
                            ProductRow(producto = producto)
                            if (index < productos.size - 1) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN 3: RESUMEN DE PAGO ---
            item {
                Text("Total a Pagar", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TotalRow(label = "Subtotal", amount = "$${subtotal.toInt()}")
                        TotalRow(label = "Envío", amount = "$${envio.toInt()}")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        TotalRow(label = "Total", amount = "$${total.toInt()}", isTotal = true)
                    }
                }
            }

            // --- SECCIÓN 4: ACCIONES ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                        Text("Contactar Soporte")
                    }
                    Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                        Text("Ver Factura")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProductRow(producto: ProductoDetalle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${producto.cantidad}x", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(producto.nombre, modifier = Modifier.weight(1f))
        Text("$${(producto.precioUnitario * producto.cantidad).toInt()}", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TotalRow(label: String, amount: String, isTotal: Boolean = false) {
    val style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    val weight = if (isTotal) FontWeight.Bold else FontWeight.Normal
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = style, fontWeight = weight)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = amount, style = style, fontWeight = weight)
    }
}