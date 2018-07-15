package com.github.fredrik9000.todolist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.Chore;

import java.util.ArrayList;

public class ChoreAdapter extends ArrayAdapter<Chore> {
    public ChoreAdapter(Context context, ArrayList<Chore> chores) {
        super(context, 0, chores);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Chore chore = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(chore.getTitle());

        //TextView tv2 = convertView.findViewById(android.R.id.text2);

        switch (chore.getPriority()) {
            case 1:
                //tv2.setText("Low priority");
                convertView.setBackgroundColor(Color.WHITE);
                break;
            case 2:
                //tv2.setText("Medium priority");
                convertView.setBackgroundColor(Color.LTGRAY);
                break;
            case 3:
                //tv2.setText("High priority");
                convertView.setBackgroundColor(Color.GRAY);
        }

        return convertView;
    }
}
