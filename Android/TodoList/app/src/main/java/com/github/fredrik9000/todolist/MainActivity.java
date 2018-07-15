package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fredrik9000.todolist.model.Chore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Chore> chores = new ArrayList<>();
    private ActionMode mActionMode;
    private Dialog deleteChoresDialog;
    private ChoreAdapter adapter;
    private int positionOflastItemLongClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                positionOflastItemLongClicked = position;
                mActionMode = startSupportActionMode(mActionModeCallback);
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, EditChoreActivity.class);
                Chore chore = chores.get(i);
                intent.putExtra("choreName", chore.getTitle());
                intent.putExtra("chorePriority", chore.getPriority());
                intent.putExtra("chorePosition", i);
                startActivityForResult(intent, 2);
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
                    chores.remove(positionOflastItemLongClicked);
                    adapter.notifyDataSetChanged();
                    actionMode.finish();
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

    class priorityCheckBoxChanged implements CheckBox.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            Button deleteChoresButton = deleteChoresDialog.findViewById(R.id.deleteChoresPopupButton);
            CheckBox deleteLowPriorityCB = deleteChoresDialog.findViewById(R.id.deleteLowPriorityChoresCheckBox);
            CheckBox deleteMediumPriorityCB = deleteChoresDialog.findViewById(R.id.deleteMediumPriorityChoresCheckBox);
            CheckBox deleteHighPriorityCB = deleteChoresDialog.findViewById(R.id.deleteHighPriorityChoresCheckBox);

            if(deleteLowPriorityCB.isChecked() || deleteMediumPriorityCB.isChecked() || deleteHighPriorityCB.isChecked()) {
                deleteChoresButton.setEnabled(true);
            } else {
                deleteChoresButton.setEnabled(false);
            }
        }
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

        if (id == R.id.addcCoreMenuButton) {
            Intent intent = new Intent(this,AddChoreActivity.class);
            startActivityForResult(intent, 1);
        } else if (id == R.id.deletecCoreMenuButton) {
            deleteChoresDialog = new Dialog(MainActivity.this);
            deleteChoresDialog.setContentView(R.layout.activity_delete_chores_popup);

            TextView txtClose = deleteChoresDialog.findViewById(R.id.closeDialog);
            txtClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteChoresDialog.dismiss();
                }
            });

            Button deleteChoresButton = deleteChoresDialog.findViewById(R.id.deleteChoresPopupButton);
            deleteChoresButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox deleteLowPriorityCB = deleteChoresDialog.findViewById(R.id.deleteLowPriorityChoresCheckBox);
                    CheckBox deleteMediumPriorityCB = deleteChoresDialog.findViewById(R.id.deleteMediumPriorityChoresCheckBox);
                    CheckBox deleteHighPriorityCB = deleteChoresDialog.findViewById(R.id.deleteHighPriorityChoresCheckBox);

                    for (Iterator<Chore> iter = chores.listIterator(); iter.hasNext(); ) {
                        Chore chore = iter.next();
                        if (deleteLowPriorityCB.isChecked() && chore.getPriority() == 1) {
                            iter.remove();
                        } else if (deleteMediumPriorityCB.isChecked() && chore.getPriority() == 2) {
                            iter.remove();
                        } else if (deleteHighPriorityCB.isChecked() && chore.getPriority() == 3) {
                            iter.remove();
                        }
                    }

                    adapter.notifyDataSetChanged();
                    deleteChoresDialog.dismiss();
                }
            });

            CheckBox deleteLowPriorityCB = deleteChoresDialog.findViewById(R.id.deleteLowPriorityChoresCheckBox);
            CheckBox deleteMediumPriorityCB = deleteChoresDialog.findViewById(R.id.deleteMediumPriorityChoresCheckBox);
            CheckBox deleteHighPriorityCB = deleteChoresDialog.findViewById(R.id.deleteHighPriorityChoresCheckBox);
            deleteLowPriorityCB.setOnCheckedChangeListener(new priorityCheckBoxChanged());
            deleteMediumPriorityCB.setOnCheckedChangeListener(new priorityCheckBoxChanged());
            deleteHighPriorityCB.setOnCheckedChangeListener(new priorityCheckBoxChanged());

            deleteChoresDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            deleteChoresDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    String choreName = data.getStringExtra("CHORE_TITLE");
                    int chorePriority = data.getIntExtra("CHORE_PRIORITY", 0);
                    chores.add(new Chore(choreName, chorePriority));
                    Collections.sort(chores);
                }
                break;
            }
            case (2) : {
                if (resultCode == Activity.RESULT_OK) {
                    String choreName = data.getStringExtra("CHORE_TITLE");
                    int chorePriority = data.getIntExtra("CHORE_PRIORITY", 0);
                    int chorePosition = data.getIntExtra("CHORE_POSITION", 0);
                    Chore chore = chores.get(chorePosition);
                    chore.setPriority(chorePriority);
                    chore.setTitle(choreName);
                    Collections.sort(chores);
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }
}
