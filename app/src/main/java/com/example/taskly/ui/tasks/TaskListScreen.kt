package com.example.taskly.ui.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskly.R
import com.example.taskly.data.model.Priority
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import com.example.taskly.ui.components.ColorDot
import com.example.taskly.ui.components.EmptyState
import com.example.taskly.ui.components.SectionHeader
import com.example.taskly.ui.components.TaskCard
import com.example.taskly.ui.theme.TasklyTheme
import com.example.taskly.util.ShareUtils
import com.example.taskly.util.shortWeekday
import com.example.taskly.util.toRelativeLabel
import java.time.LocalDate
import java.time.LocalDateTime

private const val DAYS_IN_STRIP = 14

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onAddTask: () -> Unit,
    onEditTask: (taskId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val undoLabel = stringResource(R.string.task_list_undo)
    val deletedMessage = stringResource(R.string.task_list_deleted)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskListEvent.TaskDeleted -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = deletedMessage.format(event.title),
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete()
                }
            }
        }
    }

    TaskListContent(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        snackbarHostState = snackbarHostState,
        onAddTask = onAddTask,
        onEditTask = onEditTask,
        onModeChange = viewModel::onModeChange,
        onDateSelected = viewModel::onDateSelected,
        onSubjectSelected = viewModel::onSubjectSelected,
        onClearFilter = viewModel::onClearFilter,
        onToggleComplete = viewModel::onToggleComplete,
        onDelete = viewModel::onDelete,
        onShare = { task -> ShareUtils.shareTask(context, task, state.subjectsById[task.subjectId]?.name) },
        modifier = modifier,
    )
}

@Composable
fun TaskListContent(
    state: TaskListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onModeChange: (ListMode) -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    onSubjectSelected: (Long?) -> Unit,
    onClearFilter: () -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onShare: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                expanded = fabExpanded,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.task_list_new_task)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item(key = "header") {
                ListHeader(
                    remaining = state.remainingCount,
                    completed = state.completedCount,
                    hasAnyTasks = state.hasAnyTasks,
                )
            }
            item(key = "search") {
                SearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                )
            }
            item(key = "mode") {
                ModeSelector(
                    selected = state.mode,
                    onSelected = onModeChange,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "filters") {
                Spacer(Modifier.height(12.dp))
                when (state.mode) {
                    ListMode.BY_DAY -> DateStrip(
                        today = state.today,
                        selectedDate = state.selectedDate,
                        daysWithTasks = state.daysWithOpenTasks,
                        onDateSelected = onDateSelected,
                    )
                    ListMode.BY_SUBJECT -> SubjectChips(
                        subjects = state.subjects,
                        selectedId = state.selectedSubjectId,
                        onSelected = onSubjectSelected,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            when {
                state.isLoading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.sections.isEmpty() -> item(key = "empty") {
                    TaskListEmptyState(
                        state = state,
                        onAddTask = onAddTask,
                        onClearFilter = onClearFilter,
                        onClearSearch = { onQueryChange("") },
                    )
                }
                else -> state.sections.forEach { section ->
                    item(key = section.header.key, contentType = "header") {
                        SectionTitle(section = section, modifier = Modifier.animateItem())
                    }
                    items(items = section.tasks, key = { it.id }, contentType = { "task" }) { task ->
                        SwipeToDeleteTaskCard(
                            task = task,
                            subject = state.subjectsById[task.subjectId],
                            onClick = { onEditTask(task.id) },
                            onToggleComplete = { onToggleComplete(task) },
                            onShare = { onShare(task) },
                            onDelete = { onDelete(task) },
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListHeader(remaining: Int, completed: Int, hasAnyTasks: Boolean) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.task_list_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = when {
                !hasAnyTasks -> stringResource(R.string.task_list_subtitle_empty)
                remaining == 0 -> stringResource(R.string.task_list_subtitle_all_done)
                else -> stringResource(R.string.task_list_subtitle_progress, remaining, completed)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager: FocusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.task_list_search_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.task_list_search_clear))
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSelector(selected: ListMode, onSelected: (ListMode) -> Unit, modifier: Modifier = Modifier) {
    val modes = ListMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
            ) {
                Text(
                    text = stringResource(
                        if (mode == ListMode.BY_DAY) R.string.task_list_mode_day else R.string.task_list_mode_subject,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DateStrip(
    today: LocalDate,
    selectedDate: LocalDate?,
    daysWithTasks: Set<LocalDate>,
    onDateSelected: (LocalDate?) -> Unit,
) {
    val days = remember(today) { List(DAYS_IN_STRIP) { today.plusDays(it.toLong()) } }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            StripCell(
                selected = selectedDate == null,
                isToday = false,
                showDot = false,
                onClick = { onDateSelected(null) },
            ) {
                Text(
                    text = stringResource(R.string.task_list_filter_all),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 22.dp),
                )
            }
        }
        items(items = days, key = { it.toEpochDay() }) { date ->
            val selected = date == selectedDate
            StripCell(
                selected = selected,
                isToday = date == today,
                showDot = date in daysWithTasks,
                onClick = { onDateSelected(date) },
            ) {
                Text(date.shortWeekday(), style = MaterialTheme.typography.labelMedium)
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StripCell(
    selected: Boolean,
    isToday: Boolean,
    showDot: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(if (selected) scheme.primary else scheme.surface, label = "cellBg")
    val contentColor = if (selected) scheme.onPrimary else scheme.onSurface
    val borderColor = when {
        selected -> Color.Transparent
        isToday -> scheme.primary
        else -> scheme.outlineVariant
    }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .width(58.dp)
            .semantics {
                this.selected = selected
                role = Role.Tab
            },
    ) {
        Column(
            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            content()
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .height(5.dp)
                    .width(5.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !showDot -> Color.Transparent
                            selected -> scheme.onPrimary
                            else -> scheme.primary
                        },
                    ),
            )
        }
    }
}

@Composable
private fun SubjectChips(subjects: List<Subject>, selectedId: Long?, onSelected: (Long?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all_subjects") {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.task_list_filter_all_subjects)) },
            )
        }
        items(items = subjects, key = { it.id }) { subject ->
            FilterChip(
                selected = selectedId == subject.id,
                onClick = { onSelected(subject.id) },
                label = { Text(subject.name) },
                leadingIcon = { ColorDot(color = Color(subject.colorArgb)) },
            )
        }
    }
}

@Composable
private fun SectionTitle(section: TaskSection, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    when (val header = section.header) {
        SectionHeaderModel.Overdue -> SectionHeader(
            title = stringResource(R.string.common_overdue),
            count = section.tasks.size,
            titleColor = MaterialTheme.colorScheme.error,
            modifier = modifier,
        )
        is SectionHeaderModel.Day -> SectionHeader(
            title = header.date.toRelativeLabel(today),
            count = section.tasks.size,
            modifier = modifier,
        )
        is SectionHeaderModel.SubjectGroup -> SectionHeader(
            title = header.subject.name,
            count = section.tasks.size,
            leading = { ColorDot(color = Color(header.subject.colorArgb)) },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTaskCard(
    task: Task,
    subject: Subject?,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val swiping = dismissState.dismissDirection != SwipeToDismissBoxValue.Settled
            val armed = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val background by animateColorAsState(
                targetValue = when {
                    !swiping -> Color.Transparent
                    armed -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                label = "swipeBackground",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .background(background)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (swiping) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.task_list_cd_delete),
                        tint = if (armed) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        },
    ) {
        TaskCard(
            task = task,
            subject = subject,
            onClick = onClick,
            onToggleComplete = onToggleComplete,
            onShare = onShare,
        )
    }
}

@Composable
private fun TaskListEmptyState(
    state: TaskListUiState,
    onAddTask: () -> Unit,
    onClearFilter: () -> Unit,
    onClearSearch: () -> Unit,
) {
    when {
        !state.hasAnyTasks -> EmptyState(
            icon = Icons.Default.CheckCircle,
            title = stringResource(R.string.task_list_empty_title),
            message = stringResource(R.string.task_list_empty_message),
            actionLabel = stringResource(R.string.task_list_empty_action),
            onAction = onAddTask,
        )
        state.query.isNotBlank() -> EmptyState(
            icon = Icons.Default.Search,
            title = stringResource(R.string.task_list_empty_search_title),
            message = stringResource(R.string.task_list_empty_search_message, state.query.trim()),
            actionLabel = stringResource(R.string.task_list_empty_search_action),
            onAction = onClearSearch,
        )
        state.mode == ListMode.BY_DAY -> EmptyState(
            icon = Icons.Default.DateRange,
            title = stringResource(R.string.task_list_empty_day_title),
            message = stringResource(R.string.task_list_empty_day_message),
            actionLabel = stringResource(R.string.task_list_show_all),
            onAction = onClearFilter,
        )
        else -> EmptyState(
            icon = Icons.Default.CheckCircle,
            title = stringResource(R.string.task_list_empty_subject_title),
            message = stringResource(R.string.task_list_empty_subject_message),
            actionLabel = stringResource(R.string.task_list_show_all),
            onAction = onClearFilter,
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun TaskListPreview() {
    val today = LocalDate.now()
    val subjects = listOf(
        Subject(1, "Mobile Computing", 0xFF4F46E5.toInt()),
        Subject(2, "Calculus", 0xFFE11D48.toInt()),
    )
    val tasks = listOf(
        Task(1, "Submit calculus quiz", "", 2, today.minusDays(1), Priority.MEDIUM),
        Task(2, "Finish Taskly Phase 1", "Compose UI and abstract", 1, today, Priority.HIGH),
        Task(3, "Revise lectures 1 to 6", "", 1, today.plusDays(2), Priority.LOW, isCompleted = true),
    )
    TasklyTheme {
        TaskListContent(
            state = buildTaskListState(tasks, subjects, TaskListFilter(), LocalDateTime.now()),
            query = "",
            onQueryChange = {},
            snackbarHostState = remember { SnackbarHostState() },
            onAddTask = {}, onEditTask = {}, onModeChange = {}, onDateSelected = {},
            onSubjectSelected = {}, onClearFilter = {}, onToggleComplete = {}, onDelete = {}, onShare = {},
        )
    }
}
