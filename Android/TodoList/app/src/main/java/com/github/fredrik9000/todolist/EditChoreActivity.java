package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EditChoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        Intent intent = getIntent();
        String choreName = intent.getStringExtra("choreName");
        int chorePriority = intent.getIntExtra("chorePriority", 0);
        final int chorePosition = intent.getIntExtra("chorePosition", 0);

        EditText choreTitleET = findViewById(R.id.addChoreEditText);
        choreTitleET.setText(choreName);
        choreTitleET.setSelection(choreTitleET.getText().length());

        switch (chorePriority) {
            case 1:
                ((RadioGroup)findViewById(R.id.priorityRadioGroup)).check(R.id.lowPriority);
                break;
            case 2:
                ((RadioGroup)findViewById(R.id.priorityRadioGroup)).check(R.id.mediumPriority);
                break;
            case 3:
                ((RadioGroup)findViewById(R.id.priorityRadioGroup)).check(R.id.highPriority);
        }

        Button button = findViewById(R.id.addChoreButton);
        button.setText("Edit task");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String choreName = ((EditText)findViewById(R.id.addChoreEditText)).getText().toString();
                if (choreName.length() == 0) {
                    Toast.makeText(EditChoreActivity.this, "Please write a task name", Toast.LENGTH_SHORT).show();
                    return;
                }

                int priorityRBID = ((RadioGroup)findViewById(R.id.priorityRadioGroup)).getCheckedRadioButtonId();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("CHORE_TITLE", choreName);
                int priority ;
                switch (priorityRBID) {
                    case R.id.lowPriority:
                        priority = 1;
                        break;
                    case R.id.mediumPriority:
                        priority = 2;
                        break;
                    case R.id.highPriority:
                        priority = 3;
                        break;
                    default:
                        priority = -1;
                }
                resultIntent.putExtra("CHORE_PRIORITY", priority);
                resultIntent.putExtra("CHORE_POSITION", chorePosition);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
