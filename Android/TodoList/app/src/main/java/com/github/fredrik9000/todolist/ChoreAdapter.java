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
        ChoreViewHolder choreViewHolder;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            choreViewHolder = new ChoreViewHolder();
            choreViewHolder.textViewChoreName = convertView.findViewById(android.R.id.text1);
            convertView.setTag(choreViewHolder);
        } else {
            choreViewHolder = (ChoreViewHolder)convertView.getTag();
        }

        choreViewHolder.textViewChoreName.setText(chore.getTitle());

        switch (chore.getPriority()) {
            case 1:
                convertView.setBackgroundColor(Color.WHITE);
                break;
            case 2:
                convertView.setBackgroundColor(Color.LTGRAY);
                break;
            case 3:
                convertView.setBackgroundColor(Color.GRAY);
        }

        return convertView;
    }

    private static class ChoreViewHolder {
        public TextView textViewChoreName;
    }
}
