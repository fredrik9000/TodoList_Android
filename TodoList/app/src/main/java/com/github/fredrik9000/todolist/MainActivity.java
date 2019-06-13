package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;

import com.github.fredrik9000.todolist.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.fredrik9000.todolist.model.Todo;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DeleteTodosDialog.OnDeleteTodosDialogInteractionListener, TodoAdapter.OnItemClickListener {

    private CoordinatorLayout coordinatorLayout;
    private ActionMode mActionMode;
    private RecyclerView.Adapter adapter;
    private int lastItemLongClickedPosition;

    private static final int ADD_TODO_REQUEST_CODE = 1;
    private static final int EDIT_TODO_REQUEST_CODE = 2;

    TodoListViewModel todoListViewModel;

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
        adapter = new TodoAdapter(todoListViewModel.getTodoList(), this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        final FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                startActivityForResult(intent, ADD_TODO_REQUEST_CODE);
            }
        });
        coordinatorLayout = binding.coordinatorLayout;

        checkForEmptyView();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
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
                    final Todo todoItem = todoListViewModel.getTodoList().get(lastItemLongClickedPosition);
                    if (todoItem.isNotificationEnabled()) {
                        todoListViewModel.removeNotification((AlarmManager)getSystemService(ALARM_SERVICE), todoItem);
                    }
                    todoListViewModel.getTodoList().remove(lastItemLongClickedPosition);
                    adapter.notifyItemRemoved(lastItemLongClickedPosition);
                    actionMode.finish();
                    todoListViewModel.saveTodoList();
                    checkForEmptyView();
                    Snackbar snackbar = Snackbar.make(
                            coordinatorLayout,
                            "Item deleted",
                             Snackbar.LENGTH_LONG
                    ).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            todoListViewModel.getTodoList().add(todoItem);
                            if (todoItem.isNotificationEnabled()) {
                                todoListViewModel.addNotification((AlarmManager)getSystemService(ALARM_SERVICE), todoItem);
                            }
                            Collections.sort(todoListViewModel.getTodoList());
                            adapter.notifyItemInserted(lastItemLongClickedPosition);
                            todoListViewModel.saveTodoList();
                            checkForEmptyView();
                            Snackbar snackbar2 = Snackbar.make(
                                    coordinatorLayout,
                                    "Undo successful",
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
            mActionMode = null;
        }
    };

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
        switch(requestCode) {
            case (ADD_TODO_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    Todo todo = new Todo();
                    updateTodoItem(data, todo);
                    todoListViewModel.addTodo(todo);
                    Collections.sort(todoListViewModel.getTodoList());
                    checkForEmptyView();
                }
                break;
            }
            case (EDIT_TODO_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    int todoPosition = data.getIntExtra(AddTodoActivity.TODO_POSITION, 0);
                    Todo todo = todoListViewModel.getTodoList().get(todoPosition);
                    updateTodoItem(data, todo);
                    Collections.sort(todoListViewModel.getTodoList());
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
        todoListViewModel.saveTodoList();
    }

    private void updateTodoItem(Intent data, Todo todo) {
        String todoDescription = data.getStringExtra(AddTodoActivity.TODO_DESCRIPTION);
        int todoPriority = data.getIntExtra(AddTodoActivity.TODO_PRIORITY, 0);
        boolean hasNotification = data.getBooleanExtra(AddTodoActivity.HAS_NOTIFICATION, false);
        int todoNotificationId = data.getIntExtra(AddTodoActivity.NOTIFICATION_ID, 0);
        int year = data.getIntExtra(AddTodoActivity.NOTIFICATION_YEAR, 0);
        int month = data.getIntExtra(AddTodoActivity.NOTIFICATION_MONTH, 0);
        int day = data.getIntExtra(AddTodoActivity.NOTIFICATION_DAY, 0);
        int hour = data.getIntExtra(AddTodoActivity.NOTIFICATION_HOUR, 0);
        int minute = data.getIntExtra(AddTodoActivity.NOTIFICATION_MINUTE, 0);
        todo.updateTODO(todoDescription, todoPriority, todoNotificationId, hasNotification, year, month, day, hour,  minute);
    }

    @Override
    public void onDeleteTodosDialogInteraction(ArrayList<Integer> priorities) {
        final ArrayList<Todo> todoListCopy = new ArrayList<>();
        int todoListLength = todoListViewModel.getTodoList().size();
        for (Iterator<Todo> iterator = todoListViewModel.getTodoList().listIterator(); iterator.hasNext(); ) {
            Todo todo = iterator.next();
            if (priorities.contains(todo.getPriority())) {
                todoListCopy.add(todo);
                if (todo.isNotificationEnabled()) {
                    todoListViewModel.removeNotification((AlarmManager)getSystemService(ALARM_SERVICE), todo);
                }

                iterator.remove();
            }
        }

        //The selected priorities didn't match any of the tasks, so no items will be removed.
        if (todoListLength == todoListViewModel.getTodoList().size()) {
            return;
        }

        adapter.notifyDataSetChanged();
        todoListViewModel.saveTodoList();
        checkForEmptyView();
        Snackbar snackbar = Snackbar.make(
                coordinatorLayout,
                "Items deleted",
                Snackbar.LENGTH_LONG
        ).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Todo todoItem : todoListCopy) {
                    todoListViewModel.getTodoList().add(todoItem);
                    if (todoItem.isNotificationEnabled()) {
                        todoListViewModel.addNotification((AlarmManager)getSystemService(ALARM_SERVICE), todoItem);
                    }
                }
                Collections.sort(todoListViewModel.getTodoList());
                adapter.notifyDataSetChanged();
                todoListViewModel.saveTodoList();
                checkForEmptyView();
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
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
        Todo todo = todoListViewModel.getTodoList().get(position);
        intent.putExtra(AddTodoActivity.TODO_DESCRIPTION, todo.getDescription());
        intent.putExtra(AddTodoActivity.TODO_PRIORITY, todo.getPriority());
        intent.putExtra(AddTodoActivity.TODO_POSITION, position);
        if (todo.isNotificationEnabled()) {
            intent.putExtra(AddTodoActivity.NOTIFICATION_YEAR, todo.getNotifyYear());
            intent.putExtra(AddTodoActivity.NOTIFICATION_MONTH, todo.getNotifyMonth());
            intent.putExtra(AddTodoActivity.NOTIFICATION_DAY, todo.getNotifyDay());
            intent.putExtra(AddTodoActivity.NOTIFICATION_HOUR, todo.getNotifyHour());
            intent.putExtra(AddTodoActivity.NOTIFICATION_MINUTE, todo.getNotifyMinute());
            intent.putExtra(AddTodoActivity.NOTIFICATION_ID, todo.getNotificationId());
        }
        intent.putExtra(AddTodoActivity.HAS_NOTIFICATION, todo.isNotificationEnabled());
        startActivityForResult(intent, EDIT_TODO_REQUEST_CODE);
    }

    @Override
    public boolean onItemLongClick(int position) {
        if (mActionMode != null) {
            return false;
        }

        lastItemLongClickedPosition = position;
        mActionMode = startSupportActionMode(mActionModeCallback);
        return true;
    }

    public void checkForEmptyView() {
        if (todoListViewModel.getTodoList().isEmpty()) {
            binding.todoList.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        }
        else {
            binding.todoList.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }
}
