package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.fredrik9000.todolist.model.Todo;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DeleteTodosDialog.OnDeleteTodosDialogInteractionListener, TODOAdapter.OnItemClickListener {

    private CoordinatorLayout coordinatorLayout;
    private ArrayList<Todo> todoList = new ArrayList<>();
    private ActionMode mActionMode;
    private RecyclerView.Adapter adapter;
    private int lastItemLongClickedPosition;

    private static final int ADD_TODO_REQUEST_CODE = 1;
    private static final int EDIT_TODO_REQUEST_CODE = 2;
    private static final String SHARED_PREFERENCES_TODOS_KEY = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(R.string.title_activity_main);

        loadTodos();

        RecyclerView recyclerView = findViewById(R.id.todoList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TODOAdapter(todoList, this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                startActivityForResult(intent, ADD_TODO_REQUEST_CODE);
            }
        });
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
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
                    final Todo todoItem = todoList.get(lastItemLongClickedPosition);
                    if (todoItem.isNotificationEnabled()) {
                        removeNotification(todoItem);
                    }
                    todoList.remove(lastItemLongClickedPosition);
                    adapter.notifyDataSetChanged();
                    actionMode.finish();
                    saveTodos();
                    Snackbar snackbar = Snackbar.make(
                            coordinatorLayout,
                            "Item deleted",
                             Snackbar.LENGTH_LONG
                    ).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            todoList.add(todoItem);
                            if (todoItem.isNotificationEnabled()) {
                                addNotification(todoItem);
                            }
                            adapter.notifyDataSetChanged();
                            saveTodos();
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
                    todoList.add(todo);
                    Collections.sort(todoList);
                }
                break;
            }
            case (EDIT_TODO_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    int todoPosition = data.getIntExtra(AddTodoActivity.TODO_POSITION, 0);
                    Todo todo = todoList.get(todoPosition);
                    updateTodoItem(data, todo);
                    Collections.sort(todoList);
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
        saveTodos();
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
        for (Iterator<Todo> iterator = todoList.listIterator(); iterator.hasNext(); ) {
            Todo todo = iterator.next();
            if (priorities.contains(todo.getPriority())) {
                todoListCopy.add(todo);
                if (todo.isNotificationEnabled()) {
                    removeNotification(todo);
                }

                iterator.remove();
            }
        }
        adapter.notifyDataSetChanged();
        saveTodos();
        Snackbar snackbar = Snackbar.make(
                coordinatorLayout,
                "Items deleted",
                Snackbar.LENGTH_LONG
        ).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Todo todoItem : todoListCopy) {
                    todoList.add(todoItem);
                    if (todoItem.isNotificationEnabled()) {
                        addNotification(todoItem);
                    }
                }
                adapter.notifyDataSetChanged();
                saveTodos();
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

    private void removeNotification(Todo todo) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this.getApplicationContext(), todo.getNotificationId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void addNotification(Todo todo) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        Intent notificationIntent = new Intent(MainActivity.this.getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.TODO_DESCRIPTION, todo.getDescription());
        PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this.getApplicationContext(), todo.getNotificationId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        }
    }

    private void loadTodos() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        if (appSharedPrefs.contains(SHARED_PREFERENCES_TODOS_KEY)) {
            Type type = new TypeToken<ArrayList<Todo>>() {
            }.getType();
            String json = appSharedPrefs.getString(SHARED_PREFERENCES_TODOS_KEY, "");
            todoList = new Gson().fromJson(json, type);
        }
    }

    private void saveTodos() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(SHARED_PREFERENCES_TODOS_KEY, new Gson().toJson(todoList));
        prefsEditor.apply();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
        Todo todo = todoList.get(position);
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
}
