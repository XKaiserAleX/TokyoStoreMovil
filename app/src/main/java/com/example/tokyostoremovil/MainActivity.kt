package com.example.tokyostoremovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import navigation.NavGraph
import com.example.tokyostoremovil.ui.theme.TokyoStoreMovilTheme
import android.Manifest
import android.os.Build
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import navigation.NavGraph
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                // Si el usuario acepta, no hace falta hacer nada
            }

            if (PermissionChecker.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            TokyoStoreMovilTheme {
                // Creamos el controlador de navegación
                val navController = rememberNavController()

                // Cargamos el NavGraph (donde estarán las rutas y pantallas)
                NavGraph(navController = navController)
            }
        }

    }
}



