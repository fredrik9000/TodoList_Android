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

public class AddChoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        Button button = findViewById(R.id.addChoreButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String choreName = ((EditText)findViewById(R.id.addChoreEditText)).getText().toString();
                if (choreName.length() == 0) {
                    Toast.makeText(AddChoreActivity.this, "Please write a task name", Toast.LENGTH_SHORT).show();
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
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
