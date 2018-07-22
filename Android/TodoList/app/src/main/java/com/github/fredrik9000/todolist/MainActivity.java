package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.fredrik9000.todolist.model.Chore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DeleteChoresDialog.OnDeleteChoresDialogInteractionListener {

    private ArrayList<Chore> chores = new ArrayList<>();
    private ActionMode mActionMode;
    private ChoreAdapter adapter;
    private int lastItemLongClickedPosition;

    private static final int ADD_CHORE_REQUEST_CODE = 1;
    private static final int EDIT_CHORE_REQUEST_CODE = 2;
    private static final String SHARED_PREFERENCES_CHORES_KEY = "CHORES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadChores();

        ListView listView = findViewById(R.id.todolist);
        adapter = new ChoreAdapter(this, chores);
        listView.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                lastItemLongClickedPosition = position;
                mActionMode = startSupportActionMode(mActionModeCallback);
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, EditChoreActivity.class);
                Chore chore = chores.get(i);
                intent.putExtra(AddChoreActivity.CHORE_DESCRIPTION, chore.getDescription());
                intent.putExtra(AddChoreActivity.CHORE_PRIORITY, chore.getPriority());
                intent.putExtra(AddChoreActivity.CHORE_POSITION, i);
                startActivityForResult(intent, EDIT_CHORE_REQUEST_CODE);
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
                    chores.remove(lastItemLongClickedPosition);
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

        if (id == R.id.addcCoreMenuButton) {
            Intent intent = new Intent(this,AddChoreActivity.class);
            startActivityForResult(intent, ADD_CHORE_REQUEST_CODE);
        } else if (id == R.id.deletecCoreMenuButton) {
            DeleteChoresDialog deleteChoresDialog = new DeleteChoresDialog();
            deleteChoresDialog.show(getSupportFragmentManager(), "DeleteChoresDialog");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (ADD_CHORE_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    String choreDescription = data.getStringExtra(AddChoreActivity.CHORE_DESCRIPTION);
                    int chorePriority = data.getIntExtra(AddChoreActivity.CHORE_PRIORITY, 0);
                    chores.add(new Chore(choreDescription, chorePriority));
                    Collections.sort(chores);
                }
                break;
            }
            case (EDIT_CHORE_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    String choreDescription = data.getStringExtra(EditChoreActivity.CHORE_DESCRIPTION);
                    int chorePriority = data.getIntExtra(EditChoreActivity.CHORE_PRIORITY, 0);
                    int chorePosition = data.getIntExtra(EditChoreActivity.CHORE_POSITION, 0);
                    Chore chore = chores.get(chorePosition);
                    chore.setPriority(chorePriority);
                    chore.setDescription(choreDescription);
                    Collections.sort(chores);
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
        saveChores();
    }

    @Override
    public void onDeleteChoresDialogInteraction(boolean[] priorities) {
        for (Iterator<Chore> iter = chores.listIterator(); iter.hasNext(); ) {
            Chore chore = iter.next();
            if (priorities[0] && chore.getPriority() == 1) {
                iter.remove();
            } else if (priorities[1] && chore.getPriority() == 2) {
                iter.remove();
            } else if (priorities[2] && chore.getPriority() == 3) {
                iter.remove();
            }
        }
        adapter.notifyDataSetChanged();
        saveChores();
    }

    private void loadChores() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        if (appSharedPrefs.contains(SHARED_PREFERENCES_CHORES_KEY)) {
            Type type = new TypeToken<ArrayList<Chore>>() {
            }.getType();
            String json = appSharedPrefs.getString(SHARED_PREFERENCES_CHORES_KEY, "");
            chores = new Gson().fromJson(json, type);
        }
    }

    private void saveChores() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(SHARED_PREFERENCES_CHORES_KEY, new Gson().toJson(chores));
        prefsEditor.apply();
    }
}
