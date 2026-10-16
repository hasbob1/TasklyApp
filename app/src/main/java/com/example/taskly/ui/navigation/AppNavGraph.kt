package com.example.taskly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskly.data.TaskRepository
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

    NavHost(navController = navController, startDestination = Routes.TASKS) {
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
    }
}
