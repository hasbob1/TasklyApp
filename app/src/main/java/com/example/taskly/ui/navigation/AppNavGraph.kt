package com.example.taskly.ui.navigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.taskly.ui.dashboard.DashboardScreen
import com.example.taskly.ui.tasks.AddEditTaskScreen
import com.example.taskly.ui.tasks.TaskListScreen
@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tasks.route,
        modifier = modifier
    ) {
        composable(Screen.Tasks.route) {
            TaskListScreen(
                onAddClick = { navController.navigate(Screen.AddEdit.createRoute()) }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { entry ->
            AddEditTaskScreen(
                taskId = entry.arguments?.getInt("taskId") ?: -1,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
