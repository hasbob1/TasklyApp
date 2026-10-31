@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskly.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskly.R
import com.example.taskly.TasklyApplication
import com.example.taskly.data.SettingsDataStore
import com.example.taskly.util.formatShort
import com.example.taskly.util.labelRes
import com.example.taskly.util.toRelativeLabel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = rememberDashboardViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { StatsRow(state.stats) }
            item { ProgressCard(state.stats) }


            item { SectionTitle(stringResource(R.string.dashboard_upcoming)) }
            if (state.upcoming.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.dashboard_nothing_due),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.upcoming, key = { it.task.id }) { UpcomingRow(it) }
            }

            if (state.subjectStats.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.dashboard_by_subject)) }
                items(state.subjectStats, key = { it.subject.id }) { SubjectRow(it) }
            }

            item {
                DarkModeCard(
                    checked = state.darkMode ?: isSystemInDarkTheme(),
                    onCheckedChange = viewModel::onDarkModeChange,
                )
            }
        }
    }
}

@Composable
private fun rememberDashboardViewModel(): DashboardViewModel {
    val app = LocalContext.current.applicationContext as TasklyApplication
    val settings = remember { SettingsDataStore(app) }
    return viewModel(factory = DashboardViewModel.factory(app.repository, settings))

}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun StatsRow(stats: TaskStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            label = stringResource(R.string.dashboard_remaining),
            value = stats.remaining,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.dashboard_completed),
            value = stats.completed,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.dashboard_overdue),
            value = stats.overdue,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable

private fun StatCard(label: String, value: Int, containerColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ProgressCard(stats: TaskStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(
                    R.string.dashboard_progress,
                    (stats.progress * 100).toInt(),
                    stats.completed,
                    stats.total,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            LinearProgressIndicator(progress = { stats.progress }, modifier = Modifier.fillMaxWidth())

        }
    }
}

@Composable
private fun UpcomingRow(item: UpcomingTask) {
    val task = item.task
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item.subject?.let { ColorDot(Color(it.colorArgb)) }
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${task.dueDate.toRelativeLabel()}, ${task.dueTime.formatShort()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(task.priority.labelRes()),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}


@Composable
private fun SubjectRow(stat: SubjectStat) {
    val color = Color(stat.subject.colorArgb)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorDot(color)
                Text(stat.subject.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.dashboard_subject_count, stat.completed, stat.total))
            }
            LinearProgressIndicator(progress = { stat.progress }, color = color, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
}

@Composable
private fun DarkModeCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.dashboard_dark_mode),
                style = MaterialTheme.typography.titleMedium,

                modifier = Modifier.weight(1f),
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}