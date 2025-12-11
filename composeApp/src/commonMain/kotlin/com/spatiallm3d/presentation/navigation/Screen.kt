package com.spatiallm3d.presentation.navigation

/**
 * Sealed class representing the different screens in the application.
 */
sealed class Screen {
    data object Home : Screen()
    data object Analysis : Screen()
    data object Results : Screen()
}
