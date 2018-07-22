package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EditChoreActivity extends AppCompatActivity {

    public static final String CHORE_DESCRIPTION = "CHORE_DESCRIPTION";
    public static final String CHORE_PRIORITY = "CHORE_PRIORITY";
    public static final String CHORE_POSITION = "CHORE_POSITION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        Intent intent = getIntent();
        String choreDescription = intent.getStringExtra(CHORE_DESCRIPTION);
        int chorePriority = intent.getIntExtra(CHORE_PRIORITY, 0);
        final int chorePosition = intent.getIntExtra(CHORE_POSITION, 0);

        EditText choreDescriptionET = findViewById(R.id.addChoreEditText);
        choreDescriptionET.setText(choreDescription);
        choreDescriptionET.setSelection(choreDescriptionET.getText().length());

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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String choreDescription = ((EditText)findViewById(R.id.addChoreEditText)).getText().toString();
                if (choreDescription.length() == 0) {
                    Toast.makeText(EditChoreActivity.this, R.string.task_description_not_entered, Toast.LENGTH_SHORT).show();
                    return;
                }

                int priorityRBID = ((RadioGroup)findViewById(R.id.priorityRadioGroup)).getCheckedRadioButtonId();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(CHORE_DESCRIPTION, choreDescription);
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
                resultIntent.putExtra(CHORE_PRIORITY, priority);
                resultIntent.putExtra(CHORE_POSITION, chorePosition);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
