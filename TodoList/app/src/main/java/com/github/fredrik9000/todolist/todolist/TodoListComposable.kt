package com.github.fredrik9000.todolist.todolist

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.AddEditTodoViewModel
import com.github.fredrik9000.todolist.model.Todo
import com.google.android.material.composethemeadapter.MdcTheme
import java.util.*

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun TodoListComposable(
    todoListViewModel: TodoListViewModel,
    onTodoItemClick: (todoItem: Todo) -> Unit,
    onTodoItemLongClick: (todoItem: Todo) -> Unit
) {
    val todoListState by todoListViewModel.getTodoList().observeAsState()
    todoListState?.let { todoList ->
        Surface {
            if (todoList.isEmpty() && !todoListViewModel.isSearching) {
                OnboardingComposable()
            } else {
                TodoListComposable(todoList, onTodoItemClick, onTodoItemLongClick, todoListViewModel::updatedCompleted)
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun TodoListComposable(
    todoItemList: List<Todo>,
    onTodoItemClick: (todoItem: Todo) -> Unit,
    onTodoItemLongClick: (todoItem: Todo) -> Unit,
    updateCompletionState: (isChecked: Boolean, todoItem: Todo, context: Context) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = dimensionResource(id = R.dimen.todo_list_padding_bottom_to_make_room_for_fab))) {
        items(todoItemList, key = { it.id }) {
            Box(Modifier.animateItemPlacement()) {
                TodoItemDetailsComposable(it, onTodoItemClick, onTodoItemLongClick, updateCompletionState)
            }
        }
    }
}

@Composable
private fun OnboardingComposable() {
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(id = R.string.onboarding_explanatory_text),
            fontSize = dimensionResource(id = R.dimen.onboarding_view_text_size).value.sp,
            lineHeight = dimensionResource(id = R.dimen.onboarding_view_text_line_height).value.sp,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.onboarding_view_margin)))
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun TodoItemDetailsComposable(
    todoItem: Todo, onTodoItemClick: (todoItem: Todo) -> Unit,
    onTodoItemLongClick: (todoItem: Todo) -> Unit,
    updateCompletionState: (isChecked: Boolean, todoItem: Todo, context: Context) -> Unit
) {
    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.list_item_card_corner_radius)),
        backgroundColor = getCardViewBackgroundColor(todoItem.isCompleted),
        elevation = dimensionResource(id = R.dimen.list_item_card_elevation),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    // Disable editing for completed tasks
                    if (!todoItem.isCompleted) {
                        onTodoItemClick(todoItem)
                    }
                },
                onLongClick = {
                    onTodoItemLongClick(todoItem)
                }
            )
            .padding(dimensionResource(id = R.dimen.list_item_card_spacing))
    ) {
        Column(
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.list_item_vertical_padding))
                .fillMaxWidth()
        ) {
            TextAndCompletionRowComposable(todoItem, updateCompletionState)
            if ((todoItem.notificationEnabled && !isNotificationExpired(todoItem)) || todoItem.geofenceNotificationEnabled) {
                NotificationRowComposable(todoItem)
            }
            if (!todoItem.description.isNullOrEmpty()) {
                DescriptionRowComposable(todoItem)
            }
        }
    }
}

private fun isNotificationExpired(todo: Todo): Boolean {
    return Calendar.getInstance().also {
        it[todo.notifyYear, todo.notifyMonth, todo.notifyDay, todo.notifyHour, todo.notifyMinute] = 0
    }.timeInMillis < Calendar.getInstance().timeInMillis
}

@Composable
private fun getCardViewBackgroundColor(isCompleted: Boolean) =
    if (!isCompleted) {
        colorResource(id = R.color.card_view_background_color)
    } else {
        colorResource(id = R.color.card_view_completed_background_color)
    }

@Composable
private fun TextAndCompletionRowComposable(todoItem: Todo, updateCompletionState: (isChecked: Boolean, todoItem: Todo, context: Context) -> Unit) {
    val currentContext = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.list_item_checkbox_padding))
                .scale(1.4F),
            checked = todoItem.isCompleted,
            colors = CheckboxDefaults.colors(checkedColor = getPriorityColor(todoItem.priority), uncheckedColor = getPriorityColor(todoItem.priority)),
            onCheckedChange = {
                updateCompletionState(it, todoItem, currentContext)
            })
        Text(text = todoItem.title ?: "",
            maxLines = integerResource(id = R.integer.max_lines_list_item_title),
            overflow = TextOverflow.Ellipsis,
            fontSize = dimensionResource(id = R.dimen.list_item_text_size).value.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimensionResource(id = R.dimen.list_item_horizontal_padding)))
    }
}

@Composable
private fun getPriorityColor(priority: Int): Color {
    return colorResource(id = when (priority) {
        AddEditTodoViewModel.PRIORITY_LOW -> R.color.low_priority
        AddEditTodoViewModel.PRIORITY_HIGH -> R.color.high_priority
        else -> R.color.medium_priority
    })
}

@Composable
private fun NotificationRowComposable(todoItem: Todo) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(
            start = dimensionResource(id = R.dimen.list_item_horizontal_padding),
            end = dimensionResource(id = R.dimen.list_item_horizontal_padding),
            bottom = dimensionResource(id = R.dimen.list_item_vertical_padding)
        )
    ) {
        if (todoItem.notificationEnabled && !isNotificationExpired(todoItem)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0F)) {
                Image(painter = painterResource(id = R.drawable.ic_notifications_active_black_20dp),
                    contentDescription = "Reminder icon",
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.list_item_icon)),
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.list_item_notification_text_to_image_spacing)))
                Text(text = Todo.getPrettifiedDateAndTime(todoItem) ?: "",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    fontSize = dimensionResource(id = R.dimen.list_item_notification_text_size).value.sp,
                    modifier = Modifier.weight(1.0F))
            }
        }
        if (todoItem.geofenceNotificationEnabled) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0F)) {
                Image(painter = painterResource(id = R.drawable.ic_geofence_location_black_20dp),
                    contentDescription = "Geofence icon",
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.list_item_icon)),
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.list_item_notification_text_to_image_spacing)))
                Text(text = Todo.getAddressFromLatLong(LocalContext.current,
                    todoItem.geofenceLatitude,
                    todoItem.geofenceLongitude,
                    todoItem.geofenceNotificationEnabled),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    fontSize = dimensionResource(id = R.dimen.list_item_notification_text_size).value.sp,
                    modifier = Modifier.weight(1.0F))
            }
        }
    }
}

@Composable
private fun DescriptionRowComposable(todoItem: Todo) {
    var expandedDescription by remember { mutableStateOf (false) }
    var descriptionIsCollapsedAndEllipsizised by remember { mutableStateOf (false) }
    var descriptionLineCount by remember { mutableStateOf (0) }
    val collapsedMaxDescriptionLines = integerResource(id = R.integer.max_lines_collapsed_list_item_description)

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = todoItem.description ?: "",
            maxLines = if (expandedDescription) {
                integerResource(id = R.integer.max_lines_expanded_list_item_description)
            } else {
                integerResource(id = R.integer.max_lines_collapsed_list_item_description)
            },
            overflow = TextOverflow.Ellipsis,
            fontSize = dimensionResource(id = R.dimen.list_item_description_text_size).value.sp,
            color = colorResource(id = R.color.list_item_description_text_color),
            onTextLayout = { res ->
                descriptionLineCount = res.lineCount
                descriptionIsCollapsedAndEllipsizised = descriptionLineCount == collapsedMaxDescriptionLines && res.isLineEllipsized(collapsedMaxDescriptionLines - 1)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0F)
                .padding(
                    start = dimensionResource(id = R.dimen.list_item_horizontal_padding),
                    bottom = dimensionResource(id = R.dimen.list_item_vertical_padding)
                )
                .animateContentSize()
        )
        if (descriptionLineCount > collapsedMaxDescriptionLines || descriptionIsCollapsedAndEllipsizised) {
            Image(painter = painterResource(id = if (descriptionLineCount > collapsedMaxDescriptionLines) R.drawable.ic_description_arrow_up_24 else R.drawable.ic_description_arrow_down_24),
                contentDescription = "Expand description",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.list_item_icon)),
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.list_item_horizontal_padding),
                        end = dimensionResource(id = R.dimen.list_item_horizontal_padding),
                        bottom = dimensionResource(id = R.dimen.list_item_vertical_padding_plus_expand_description_image_adjustment)
                    )
                    .clickable {
                        expandedDescription = !expandedDescription
                    }
            )
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview
@Composable
fun TodoListComposablePreview() {
    MdcTheme {
        TodoListComposable(listOf(
            Todo(
                id = 1,
                title = "This is a title",
                description = "This is a description",
                priority = 1,
                notificationEnabled = true,
                notifyYear = 2022,
                notifyMonth = 2,
                notifyDay = 20,
                notifyHour = 5,
                notifyMinute = 30
            ),
            Todo(
                id = 2,
                title = "Completed todo item",
                description = "This is another description",
                priority = 1,
                isCompleted = true
            )
        ), {
            // Navigation not handled in preview
        }, {
            // Long press not handled in preview
        }, { isChecked: Boolean, todo: Todo, context: Context ->
            // Completion toggle not handled in preview
        })
        // OnboardingComposable()
    }
}