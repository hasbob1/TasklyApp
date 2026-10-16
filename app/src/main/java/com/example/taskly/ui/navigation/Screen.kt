package com.example.taskly.ui.navigation

sealed class Screen(val route: String) {
    object Tasks : Screen("tasks")
    object Dashboard : Screen("dashboard")
    object AddEdit : Screen("add_edit?taskId={taskId}") {
        fun createRoute(taskId: Int? = null): String =
            if (taskId != null) "add_edit?taskId=$taskId" else "add_edit"
    }
}
