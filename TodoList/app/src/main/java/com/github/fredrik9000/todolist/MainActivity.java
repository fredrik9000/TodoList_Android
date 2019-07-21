package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.fredrik9000.todolist.databinding.ActivityMainBinding;
import com.github.fredrik9000.todolist.model.Todo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeleteTodosDialog.OnDeleteTodosDialogInteractionListener, TodoAdapter.OnItemClickListener {

    private static final int ADD_TODO_REQUEST_CODE = 1;
    private static final int EDIT_TODO_REQUEST_CODE = 2;
    TodoListViewModel todoListViewModel;
    private CoordinatorLayout coordinatorLayout;
    private ActionMode actionMode;
    private int lastItemLongClickedPosition;
    private TodoAdapter adapter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setTitle(R.string.title_activity_main);
        todoListViewModel = ViewModelProviders.of(this).get(TodoListViewModel.class);

        RecyclerView recyclerView = binding.todoList;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TodoAdapter(this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        final FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditTodoActivity.class);
                startActivityForResult(intent, ADD_TODO_REQUEST_CODE);
            }
        });
        coordinatorLayout = binding.coordinatorLayout;

        todoListViewModel.getTodoList().observe(this, new Observer<List<Todo>>() {
            @Override
            public void onChanged(@Nullable List<Todo> todoList) {
                adapter.submitList(todoList);
                checkForEmptyView(todoList);
            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.deletecCoreMenuButton) {
            DeleteTodosDialog deleteTodosDialog = new DeleteTodosDialog();
            deleteTodosDialog.show(getSupportFragmentManager(), "DeleteTodosDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (ADD_TODO_REQUEST_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    Todo todo = createTodoItem(data);
                    todoListViewModel.insert(todo);
                }
                break;
            }
            case (EDIT_TODO_REQUEST_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    int todoId = data.getIntExtra(AddEditTodoActivity.TODO_ID, -1);
                    if (todoId == -1) {
                        Toast.makeText(this, R.string.task_update_invalid_id, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Todo todo = createTodoItem(data);
                    todo.setId(todoId);
                    todoListViewModel.update(todo);
                }
            }
        }
    }

    private Todo createTodoItem(Intent data) {
        String todoDescription = data.getStringExtra(AddEditTodoActivity.TODO_DESCRIPTION);
        int todoPriority = data.getIntExtra(AddEditTodoActivity.TODO_PRIORITY, 0);
        boolean hasNotification = data.getBooleanExtra(AddEditTodoActivity.HAS_NOTIFICATION, false);
        int todoNotificationId = data.getIntExtra(AddEditTodoActivity.NOTIFICATION_ID, 0);
        int year = data.getIntExtra(AddEditTodoActivity.NOTIFICATION_YEAR, 0);
        int month = data.getIntExtra(AddEditTodoActivity.NOTIFICATION_MONTH, 0);
        int day = data.getIntExtra(AddEditTodoActivity.NOTIFICATION_DAY, 0);
        int hour = data.getIntExtra(AddEditTodoActivity.NOTIFICATION_HOUR, 0);
        int minute = data.getIntExtra(AddEditTodoActivity.NOTIFICATION_MINUTE, 0);
        return new Todo(todoDescription, todoPriority, todoNotificationId, hasNotification, year, month, day, hour, minute);
    }

    @Override
    public void onDeleteTodosDialogInteraction(ArrayList<Integer> priorities) {
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final List<Todo> removedTodoListItems = todoListViewModel.deleteTodosWithPriorities(priorities, alarmManager);

        //The selected priorities didn't match any of the tasks, so no items will be removed.
        if (removedTodoListItems.size() == 0) {
            return;
        }

        Snackbar snackbar = Snackbar.make(
                coordinatorLayout,
                "Items deleted",
                Snackbar.LENGTH_LONG
        ).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoListViewModel.insertTodoItems(removedTodoListItems, alarmManager);
                Snackbar snackbar2 = Snackbar.make(
                        coordinatorLayout,
                        "Undo successful",
                        Snackbar.LENGTH_SHORT
                );
                snackbar2.show();
            }
        });
        snackbar.show();
    }


    @Override
    public void onItemClick(Todo todo) {
        Intent intent = new Intent(MainActivity.this, AddEditTodoActivity.class);
        intent.putExtra(AddEditTodoActivity.TODO_DESCRIPTION, todo.getDescription());
        intent.putExtra(AddEditTodoActivity.TODO_PRIORITY, todo.getPriority());
        intent.putExtra(AddEditTodoActivity.TODO_ID, todo.getId());
        if (todo.isNotificationEnabled()) {
            intent.putExtra(AddEditTodoActivity.NOTIFICATION_YEAR, todo.getNotifyYear());
            intent.putExtra(AddEditTodoActivity.NOTIFICATION_MONTH, todo.getNotifyMonth());
            intent.putExtra(AddEditTodoActivity.NOTIFICATION_DAY, todo.getNotifyDay());
            intent.putExtra(AddEditTodoActivity.NOTIFICATION_HOUR, todo.getNotifyHour());
            intent.putExtra(AddEditTodoActivity.NOTIFICATION_MINUTE, todo.getNotifyMinute());
            intent.putExtra(AddEditTodoActivity.NOTIFICATION_ID, todo.getNotificationId());
        }
        intent.putExtra(AddEditTodoActivity.HAS_NOTIFICATION, todo.isNotificationEnabled());
        startActivityForResult(intent, EDIT_TODO_REQUEST_CODE);
    }

    @Override
    public boolean onItemLongClick(int position) {
        if (actionMode != null) {
            return false;
        }

        lastItemLongClickedPosition = position;
        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.context_main, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        final Todo todoItem = adapter.getTodoAt(lastItemLongClickedPosition);
                        if (todoItem.isNotificationEnabled()) {
                            todoListViewModel.removeNotification((AlarmManager) getSystemService(ALARM_SERVICE), todoItem);
                        }
                        todoListViewModel.delete(todoItem);
                        actionMode.finish();
                        Snackbar snackbar = Snackbar.make(
                                coordinatorLayout,
                                R.string.item_deleted,
                                Snackbar.LENGTH_LONG
                        ).setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (todoItem.isNotificationEnabled()) {
                                    todoListViewModel.addNotification((AlarmManager) getSystemService(ALARM_SERVICE), todoItem);
                                }
                                todoListViewModel.insert(todoItem);
                                Snackbar snackbar2 = Snackbar.make(
                                        coordinatorLayout,
                                        R.string.undo_successful,
                                        Snackbar.LENGTH_SHORT
                                );
                                snackbar2.show();
                            }
                        });
                        snackbar.show();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                actionMode = null;
            }
        });
        return true;
    }

    public void checkForEmptyView(List<Todo> todoList) {
        if (todoList.isEmpty()) {
            binding.todoList.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.todoList.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }
}
