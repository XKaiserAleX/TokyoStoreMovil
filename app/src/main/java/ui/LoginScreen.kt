package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    // --- MEJORA DE ESTADO ---
    // Mantenemos el estado como lo tenías, pero usamos 'rememberSaveable'
    // para que la contraseña no se borre si el usuario rota la pantalla.
    var email by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }


    // como un menú de navegación.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), // Más espacio en los lados
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- MEJORA VISUAL: Título y Bienvenida ---
        // Le damos más personalidad a la pantalla.
        Text(
            text = "TokyoStore Móvil",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Bienvenido de vuelta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp)) // Más espacio para separar el título

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = "" }, // Limpia el error al escribir
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = "Icono de email")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = "" }, // Limpia el error al escribir
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = "Icono de contraseña")
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { /* TODO: Navegar a pantalla de recuperación */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Olvidaste tu contraseña?")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Mostramos el mensaje de error si existe
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Completa todos los campos"
                } else {
                    error = ""
                    // La navegación es correcta
                    navController.navigate(Screen.Orders.route) {
                        // Limpia la pila de navegación para que el usuario no pueda "volver" al login
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp) // Hacemos el botón un poco más alto
        ) {
            Text("INGRESAR", fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿No tienes una cuenta?")
            TextButton(onClick = { /* TODO: Navegar a la pantalla de registro */ }) {
                Text("Regístrate aquí")
            }
        }
    }
}