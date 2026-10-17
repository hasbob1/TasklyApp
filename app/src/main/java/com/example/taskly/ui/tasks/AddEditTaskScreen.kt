package com.example.taskly.ui.tasks

import android.Manifest
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskly.R
import com.example.taskly.data.model.Priority
import com.example.taskly.data.model.Subject
import com.example.taskly.ui.components.ColorDot
import com.example.taskly.ui.components.TasklyIcons
import com.example.taskly.ui.components.color
import com.example.taskly.ui.components.containerColor
import com.example.taskly.ui.theme.SubjectPalette
import com.example.taskly.ui.theme.TasklyTheme
import com.example.taskly.util.NotificationHelper
import com.example.taskly.util.formatFull
import com.example.taskly.util.formatShort
import com.example.taskly.util.labelRes
import com.example.taskly.util.toRelativeLabel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun AddEditTaskScreen(
    viewModel: AddEditViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AddEditEvent.Saved, AddEditEvent.Deleted, AddEditEvent.TaskNotFound -> onNavigateUp()
            }
        }
    }

    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onSave() }

    val onSaveClick: () -> Unit = {
        if (viewModel.validateForm()) {
            if (!permissionRequested && !NotificationHelper.canPostNotifications(context)) {
                permissionRequested = true
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onSave()
            }
        }
    }

    AddEditTaskContent(
        state = state,
        onBack = onNavigateUp,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSubjectSelected = viewModel::onSubjectSelected,
        onCreateSubject = viewModel::onCreateSubject,
        onUpdateSubject = viewModel::onUpdateSubject,
        onDeleteSubject = viewModel::onDeleteSubject,
        onDateSelected = viewModel::onDateSelected,
        onTimeSelected = viewModel::onTimeSelected,
        onReminderSelected = viewModel::onReminderSelected,
        onPrioritySelected = viewModel::onPrioritySelected,
        onSave = onSaveClick,
        onDelete = viewModel::onDelete,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskContent(
    state: AddEditUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubjectSelected: (Long) -> Unit,
    onCreateSubject: (String, Int) -> Unit,
    onUpdateSubject: (Subject, String, Int) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onReminderSelected: (Int) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showNewSubjectDialog by rememberSaveable { mutableStateOf(false) }
    var showManageDialog by rememberSaveable { mutableStateOf(false) }
    var editingSubjectId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingSubjectId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val titleFocus = remember { FocusRequester() }

    LaunchedEffect(state.isEditing, state.isLoading) {
        if (!state.isEditing && !state.isLoading) {
            withFrameNanos { }
            runCatching { titleFocus.requestFocus() }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.add_edit_title_edit else R.string.add_edit_title_new,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.add_edit_cd_back))
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.add_edit_cd_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 3.dp) {
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving && !state.isLoading,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(54.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(
                            if (state.isEditing) R.string.add_edit_save_changes else R.string.add_edit_save,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.add_edit_field_title)) },
                    placeholder = { Text(stringResource(R.string.add_edit_field_title_hint)) },
                    isError = state.titleError != null,
                    supportingText = {
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                text = state.titleError?.let { stringResource(it.messageRes()) }.orEmpty(),
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                stringResource(
                                    R.string.add_edit_counter,
                                    state.title.length,
                                    AddEditViewModel.MAX_TITLE_LENGTH,
                                ),
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus),
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.add_edit_field_description)) },
                    placeholder = { Text(stringResource(R.string.add_edit_field_description_hint)) },
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SubjectDropdown(
                subjects = state.subjects,
                selectedId = state.subjectId,
                isError = state.subjectError,
                onSelected = onSubjectSelected,
                onNewSubject = { showNewSubjectDialog = true },
                onManageSubjects = { showManageDialog = true },
            )

            FormSection(label = stringResource(R.string.add_edit_section_due)) {
                DueDateSection(
                    dueDate = state.dueDate,
                    dueTime = state.dueTime,
                    isInPast = state.deadlineInPast,
                    onPickDate = { showDatePicker = true },
                    onPickTime = { showTimePicker = true },
                    onQuickPick = onDateSelected,
                )
            }

            FormSection(label = stringResource(R.string.add_edit_section_reminder)) {
                ReminderSelector(selectedMinutes = state.remindBeforeMinutes, onSelected = onReminderSelected)
            }

            FormSection(label = stringResource(R.string.add_edit_section_priority)) {
                PrioritySelector(selected = state.priority, onSelected = onPrioritySelected)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        TaskDatePickerDialog(
            initial = state.dueDate,
            onConfirm = {
                onDateSelected(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        TaskTimePickerDialog(
            initial = state.dueTime,
            onConfirm = {
                onTimeSelected(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
    if (showNewSubjectDialog) {
        SubjectDialog(
            title = stringResource(R.string.add_edit_new_subject_title),
            confirmLabel = stringResource(R.string.add_edit_new_subject_add),
            initialName = "",
            initialColorArgb = SubjectPalette.first().toArgb(),
            onConfirm = { name, color ->
                onCreateSubject(name, color)
                showNewSubjectDialog = false
            },
            onDismiss = { showNewSubjectDialog = false },
        )
    }
    if (showManageDialog && state.subjects.isNotEmpty()) {
        ManageSubjectsDialog(
            subjects = state.subjects,
            taskCounts = state.subjectTaskCounts,
            onEdit = { editingSubjectId = it.id },
            onDelete = { deletingSubjectId = it.id },
            onDismiss = { showManageDialog = false },
        )
    }
    state.subjects.firstOrNull { it.id == editingSubjectId }?.let { subject ->
        SubjectDialog(
            title = stringResource(R.string.add_edit_edit_subject_title),
            confirmLabel = stringResource(R.string.add_edit_save_subject),
            initialName = subject.name,
            initialColorArgb = subject.colorArgb,
            onConfirm = { name, color ->
                onUpdateSubject(subject, name, color)
                editingSubjectId = null
            },
            onDismiss = { editingSubjectId = null },
        )
    }
    state.subjects.firstOrNull { it.id == deletingSubjectId }?.let { subject ->
        val taskCount = state.subjectTaskCounts[subject.id] ?: 0
        AlertDialog(
            onDismissRequest = { deletingSubjectId = null },
            title = { Text(stringResource(R.string.add_edit_delete_subject_title)) },
            text = {
                Text(
                    stringResource(
                        if (taskCount > 0) R.string.add_edit_delete_subject_message_tasks
                        else R.string.add_edit_delete_subject_message_empty,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSubject(subject)
                        deletingSubjectId = null
                    },
                ) { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingSubjectId = null }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.add_edit_delete_dialog_title)) },
            text = { Text(stringResource(R.string.add_edit_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@StringRes
private fun TitleError.messageRes(): Int = when (this) {
    TitleError.EMPTY -> R.string.add_edit_error_title_empty
    TitleError.TOO_LONG -> R.string.add_edit_error_title_too_long
}

@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(
    subjects: List<Subject>,
    selectedId: Long?,
    isError: Boolean,
    onSelected: (Long) -> Unit,
    onNewSubject: () -> Unit,
    onManageSubjects: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = subjects.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.add_edit_subject_label)) },
            placeholder = { Text(stringResource(R.string.add_edit_subject_placeholder)) },
            leadingIcon = if (selected != null) {
                { ColorDot(color = Color(selected.colorArgb)) }
            } else {
                null
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            supportingText = if (isError) {
                { Text(stringResource(R.string.add_edit_error_subject)) }
            } else {
                null
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject.name) },
                    leadingIcon = { ColorDot(color = Color(subject.colorArgb)) },
                    onClick = {
                        onSelected(subject.id)
                        expanded = false
                    },
                )
            }
            if (subjects.isNotEmpty()) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_edit_subject_manage)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onManageSubjects()
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_edit_subject_new)) },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = {
                    expanded = false
                    onNewSubject()
                },
            )
        }
    }
}

@Composable
private fun DueDateSection(
    dueDate: LocalDate,
    dueTime: LocalTime,
    isInPast: Boolean,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onQuickPick: (LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val quickOptions = listOf(
        R.string.common_today to today,
        R.string.common_tomorrow to today.plusDays(1),
        R.string.add_edit_date_next_week to today.plusWeeks(1),
    )

    PickerCard(
        icon = Icons.Default.DateRange,
        title = dueDate.formatFull(),
        subtitle = dueDate.toRelativeLabel(today),
        isError = isInPast,
        onClick = onPickDate,
    )
    PickerCard(
        icon = TasklyIcons.Clock,
        title = dueTime.formatShort(),
        subtitle = stringResource(R.string.add_edit_time_label),
        isError = isInPast,
        onClick = onPickTime,
    )
    if (isInPast) {
        Text(
            text = stringResource(R.string.add_edit_error_deadline_past),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        quickOptions.forEach { (labelRes, date) ->
            FilterChip(
                selected = dueDate == date,
                onClick = { onQuickPick(date) },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
}

@Composable
private fun PickerCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isError: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.add_edit_date_change),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val ReminderOptions = listOf(
    0 to R.string.add_edit_reminder_none,
    15 to R.string.add_edit_reminder_15m,
    60 to R.string.add_edit_reminder_1h,
    1440 to R.string.add_edit_reminder_1d,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderSelector(selectedMinutes: Int, onSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ReminderOptions.forEach { (minutes, labelRes) ->
                FilterChip(
                    selected = selectedMinutes == minutes,
                    onClick = { onSelected(minutes) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }
        Text(
            text = stringResource(R.string.add_edit_reminder_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrioritySelector(selected: Priority, onSelected: (Priority) -> Unit) {
    Row(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Priority.entries.forEach { priority ->
            PriorityOption(
                priority = priority,
                selected = priority == selected,
                onClick = { onSelected(priority) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PriorityOption(
    priority: Priority,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = priority.color()
    val borderColor by animateColorAsState(
        targetValue = if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
        label = "priorityBorder",
    )
    val container by animateColorAsState(
        targetValue = if (selected) priority.containerColor() else MaterialTheme.colorScheme.surface,
        label = "priorityContainer",
    )
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = container,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = modifier.semantics {
            this.selected = selected
            role = Role.RadioButton
        },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ColorDot(color = accent, size = 14.dp)
            Text(
                text = stringResource(priority.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val earliestMillis = minOf(LocalDate.now(), initial).toUtcMillis()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toUtcMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= earliestMillis
        },
    )
    val selectedMillis = pickerState.selectedDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { selectedMillis?.let { onConfirm(it.toLocalDateUtc()) } },
                enabled = selectedMillis != null,
            ) { Text(stringResource(R.string.add_edit_date_picker_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_edit_time_picker_title)) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = pickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.add_edit_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    initialColorArgb: Int,
    onConfirm: (name: String, colorArgb: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var colorArgb by rememberSaveable { mutableIntStateOf(initialColorArgb) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text(stringResource(R.string.add_edit_new_subject_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.add_edit_new_subject_color),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SubjectPalette.forEach { color ->
                        ColorSwatch(
                            color = color,
                            selected = color.toArgb() == colorArgb,
                            onClick = { colorArgb = color.toArgb() },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, colorArgb) }, enabled = name.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@Composable
private fun ManageSubjectsDialog(
    subjects: List<Subject>,
    taskCounts: Map<Long, Int>,
    onEdit: (Subject) -> Unit,
    onDelete: (Subject) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_edit_manage_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                subjects.forEach { subject ->
                    val count = taskCounts[subject.id] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ColorDot(color = Color(subject.colorArgb), size = 14.dp)
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = pluralStringResource(R.plurals.add_edit_subject_task_count, count, count),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onEdit(subject) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.add_edit_cd_edit_subject),
                            )
                        }
                        IconButton(onClick = { onDelete(subject) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.add_edit_cd_delete_subject),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_edit_manage_done)) }
        },
    )
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun AddEditPreview() {
    TasklyTheme {
        AddEditTaskContent(
            state = AddEditUiState(
                title = "Finish Taskly Phase 1",
                subjectId = 1,
                subjects = listOf(Subject(1, "Mobile Computing", 0xFF4F46E5.toInt())),
                priority = Priority.HIGH,
            ),
            onBack = {}, onTitleChange = {}, onDescriptionChange = {}, onSubjectSelected = {},
            onCreateSubject = { _, _ -> }, onUpdateSubject = { _, _, _ -> }, onDeleteSubject = {},
            onDateSelected = {}, onTimeSelected = {}, onReminderSelected = {},
            onPrioritySelected = {}, onSave = {}, onDelete = {},
        )
    }
}