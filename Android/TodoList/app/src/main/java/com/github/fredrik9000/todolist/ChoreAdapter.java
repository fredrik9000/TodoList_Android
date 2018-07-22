package com.github.fredrik9000.todolist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.Chore;

import java.util.ArrayList;

public class ChoreAdapter extends ArrayAdapter<Chore> {
    ChoreAdapter(Context context, ArrayList<Chore> chores) {
        super(context, 0, chores);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        ChoreViewHolder choreViewHolder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            choreViewHolder = new ChoreViewHolder();
            choreViewHolder.textViewChoreName = view.findViewById(android.R.id.text1);
            view.setTag(choreViewHolder);
        } else {
            choreViewHolder = (ChoreViewHolder)view.getTag();
        }

        Chore chore = getItem(position);
        choreViewHolder.textViewChoreName.setText(chore.getDescription());

        switch (chore.getPriority()) {
            case 1:
                view.setBackgroundColor(Color.WHITE);
                break;
            case 2:
                view.setBackgroundColor(Color.LTGRAY);
                break;
            case 3:
                view.setBackgroundColor(Color.GRAY);
        }

        return view;
    }

    private static class ChoreViewHolder {
        TextView textViewChoreName;
    }
}
