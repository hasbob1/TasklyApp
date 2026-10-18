package com.example.taskly.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taskly.R
import com.example.taskly.data.model.Priority
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import com.example.taskly.ui.theme.TasklyTheme
import com.example.taskly.util.formatShort
import com.example.taskly.util.toRelativeLabel
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: Task,
    subject: Subject?,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
    onShare: (() -> Unit)? = null,
    now: LocalDateTime = LocalDateTime.now(),
) {
    val today = now.toLocalDate()
    val scheme = MaterialTheme.colorScheme
    val priorityColor = task.priority.color()
    val isOverdue = !task.isCompleted && task.dueDateTime.isBefore(now)
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.55f else 1f,
        label = "taskCardAlpha",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(priorityColor.copy(alpha = contentAlpha)),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompletionToggle(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    accent = priorityColor,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(contentAlpha)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SubjectTag(
                            name = subject?.name ?: stringResource(R.string.task_card_no_subject),
                            color = subject?.let { Color(it.colorArgb) } ?: scheme.outline,
                        )
                        DueDateLabel(task = task, today = today, isOverdue = isOverdue)
                        PriorityChip(priority = task.priority)
                    }
                }
                if (onShare != null) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.task_card_cd_share),
                            tint = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DueDateLabel(task: Task, today: LocalDate, isOverdue: Boolean) {
    val tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = TasklyIcons.Clock,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "${task.dueDate.toRelativeLabel(today)}, ${task.dueTime.formatShort()}",
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskCardPreview() {
    TasklyTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val subject = Subject(1, "Mobile Computing", 0xFF4F46E5.toInt())
            TaskCard(
                task = Task(1, "Finish Taskly Phase 1", "Compose UI and abstract", 1, LocalDate.now(), Priority.HIGH),
                subject = subject, onClick = {}, onToggleComplete = {}, onShare = {},
            )
            TaskCard(
                task = Task(2, "Submit quiz", "", 1, LocalDate.now().minusDays(1), Priority.MEDIUM),
                subject = subject, onClick = {}, onToggleComplete = {}, onShare = {},
            )
            TaskCard(
                task = Task(3, "Read article", "", 1, LocalDate.now().plusDays(2), Priority.LOW, isCompleted = true),
                subject = subject, onClick = {}, onToggleComplete = {}, onShare = {},
            )
        }
    }
}
