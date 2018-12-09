package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.fredrik9000.todolist.model.TODO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DeleteChoresDialog.OnDeleteChoresDialogInteractionListener, TODOAdapter.OnItemClickListener {

    private ArrayList<TODO> todoList = new ArrayList<>();
    private ActionMode mActionMode;
    private RecyclerView.Adapter adapter;
    private int lastItemLongClickedPosition;

    private static final int ADD_TODO_REQUEST_CODE = 1;
    private static final int EDIT_TODO_REQUEST_CODE = 2;
    private static final String SHARED_PREFERENCES_CHORES_KEY = "CHORES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(R.string.title_activity_main);

        loadChores();

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
                Intent intent = new Intent(MainActivity.this, AddTODOActivity.class);
                startActivityForResult(intent, ADD_TODO_REQUEST_CODE);
            }
        });
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
                    todoList.remove(lastItemLongClickedPosition);
                    adapter.notifyDataSetChanged();
                    actionMode.finish();
                    saveChores();
                    return true;
                default:
                    return  false;
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
            DeleteChoresDialog deleteChoresDialog = new DeleteChoresDialog();
            deleteChoresDialog.show(getSupportFragmentManager(), "DeleteChoresDialog");
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
                    TODO todo = new TODO();
                    updateTodoItem(data, todo);
                    todoList.add(todo);
                    Collections.sort(todoList);
                }
                break;
            }
            case (EDIT_TODO_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    int chorePosition = data.getIntExtra(AddTODOActivity.CHORE_POSITION, 0);
                    TODO todo = todoList.get(chorePosition);
                    updateTodoItem(data, todo);
                    Collections.sort(todoList);
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
        saveChores();
    }

    private void updateTodoItem(Intent data, TODO todo) {
        String choreDescription = data.getStringExtra(AddTODOActivity.CHORE_DESCRIPTION);
        int chorePriority = data.getIntExtra(AddTODOActivity.CHORE_PRIORITY, 0);
        boolean hasNotification = data.getBooleanExtra(AddTODOActivity.HAS_NOTIFICATION, false);
        int choreNotificationId = data.getIntExtra(AddTODOActivity.NOTIFICATION_ID, 0);
        int year = data.getIntExtra(AddTODOActivity.NOTIFICATION_YEAR, 0);
        int month = data.getIntExtra(AddTODOActivity.NOTIFICATION_MONTH, 0);
        int day = data.getIntExtra(AddTODOActivity.NOTIFICATION_DAY, 0);
        int hour = data.getIntExtra(AddTODOActivity.NOTIFICATION_HOUR, 0);
        int minute = data.getIntExtra(AddTODOActivity.NOTIFICATION_MINUTE, 0);
        todo.updateTODO(choreDescription, chorePriority, choreNotificationId, hasNotification, year, month, day, hour,  minute);
    }

    @Override
    public void onDeleteChoresDialogInteraction(ArrayList<Integer> priorities) {
        for (Iterator<TODO> iterator = todoList.listIterator(); iterator.hasNext(); ) {
            TODO TODO = iterator.next();
            if (priorities.contains(TODO.getPriority())) {
                iterator.remove();
            }
        }
        adapter.notifyDataSetChanged();
        saveChores();
    }

    private void loadChores() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        if (appSharedPrefs.contains(SHARED_PREFERENCES_CHORES_KEY)) {
            Type type = new TypeToken<ArrayList<TODO>>() {
            }.getType();
            String json = appSharedPrefs.getString(SHARED_PREFERENCES_CHORES_KEY, "");
            todoList = new Gson().fromJson(json, type);
        }
    }

    private void saveChores() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(SHARED_PREFERENCES_CHORES_KEY, new Gson().toJson(todoList));
        prefsEditor.apply();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, AddTODOActivity.class);
        TODO todo = todoList.get(position);
        intent.putExtra(AddTODOActivity.CHORE_DESCRIPTION, todo.getDescription());
        intent.putExtra(AddTODOActivity.CHORE_PRIORITY, todo.getPriority());
        intent.putExtra(AddTODOActivity.CHORE_POSITION, position);
        if (todo.getHasNotification()) {
            intent.putExtra(AddTODOActivity.NOTIFICATION_YEAR, todo.getNotifyYear());
            intent.putExtra(AddTODOActivity.NOTIFICATION_MONTH, todo.getNotifyMonth());
            intent.putExtra(AddTODOActivity.NOTIFICATION_DAY, todo.getNotifyDay());
            intent.putExtra(AddTODOActivity.NOTIFICATION_HOUR, todo.getNotifyHour());
            intent.putExtra(AddTODOActivity.NOTIFICATION_MINUTE, todo.getNotifyMinute());
            intent.putExtra(AddTODOActivity.NOTIFICATION_ID, todo.getNotificationId());
        }
        intent.putExtra(AddTODOActivity.HAS_NOTIFICATION, todo.getHasNotification());
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
