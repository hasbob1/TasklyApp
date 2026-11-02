package com.example.taskly.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskly.data.TaskRepository
import com.example.taskly.ui.dashboard.DashboardScreen
import com.example.taskly.ui.tasks.AddEditTaskScreen
import com.example.taskly.ui.tasks.AddEditViewModel
import com.example.taskly.ui.tasks.TaskListScreen
import com.example.taskly.ui.tasks.TaskListViewModel
import com.example.taskly.util.ReminderScheduler

private object Routes {
    const val TASKS = "tasks"
    const val ADD = "add"
    const val EDIT = "edit/{taskId}"

    fun edit(taskId: Long) = "edit/$taskId"
}

@Composable
fun AppNavGraph(
    repository: TaskRepository,
    reminderScheduler: ReminderScheduler,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route in
        setOf(Screen.Tasks.route, Screen.Dashboard.route)

    Scaffold(
        // Each screen has its own Scaffold and handles system bars itself,
        // so the outer Scaffold must not add status-bar padding on top.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (showBottomBar) TasklyBottomBar(navController) },
    ) { innerPadding ->
    NavHost(
        navController = navController,
        startDestination = Routes.TASKS,
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding),
    ) {
        composable(Routes.TASKS) {
            val viewModel: TaskListViewModel =
                viewModel(factory = TaskListViewModel.factory(repository, reminderScheduler))
            TaskListScreen(
                viewModel = viewModel,
                onAddTask = { navController.navigate(Routes.ADD) },
                onEditTask = { id -> navController.navigate(Routes.edit(id)) },
            )
        }
        composable(Routes.ADD) {
            val viewModel: AddEditViewModel =
                viewModel(factory = AddEditViewModel.factory(repository, reminderScheduler, null))
            AddEditTaskScreen(viewModel = viewModel, onNavigateUp = { navController.popBackStack() })
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
        ) { entry ->
            val taskId = entry.arguments?.getLong("taskId")
            val viewModel: AddEditViewModel = viewModel(
                key = "edit_$taskId",
                factory = AddEditViewModel.factory(repository, reminderScheduler, taskId),
            )
            AddEditTaskScreen(viewModel = viewModel, onNavigateUp = { navController.popBackStack() })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
    }
    }
}
