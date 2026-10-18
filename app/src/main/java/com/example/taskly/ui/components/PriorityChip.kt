package com.example.taskly.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.taskly.data.model.Priority
import com.example.taskly.ui.theme.taskly
import com.example.taskly.util.labelRes

@Composable
fun Priority.color(): Color = when (this) {
    Priority.HIGH -> MaterialTheme.taskly.priorityHigh
    Priority.MEDIUM -> MaterialTheme.taskly.priorityMedium
    Priority.LOW -> MaterialTheme.taskly.priorityLow
}

@Composable
fun Priority.containerColor(): Color = when (this) {
    Priority.HIGH -> MaterialTheme.taskly.priorityHighContainer
    Priority.MEDIUM -> MaterialTheme.taskly.priorityMediumContainer
    Priority.LOW -> MaterialTheme.taskly.priorityLowContainer
}

@Composable
fun PriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = priority.containerColor(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ColorDot(color = priority.color(), size = 8.dp)
            Text(
                text = stringResource(priority.labelRes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
