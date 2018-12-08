package com.github.fredrik9000.todolist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.TODO;

import java.util.ArrayList;

class ChoreAdapter extends ArrayAdapter<TODO> {
    ChoreAdapter(Context context, ArrayList<TODO> TODOS) {
        super(context, 0, TODOS);
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

        TODO TODO = getItem(position);
        choreViewHolder.textViewChoreName.setText(TODO.getDescription());

        switch (TODO.getPriority()) {
            case 0:
                view.setBackgroundColor(Color.rgb(244, 244, 244));
                break;
            case 1:
                view.setBackgroundColor(Color.rgb(220, 208, 192));
                break;
            case 2:
                view.setBackgroundColor(Color.rgb(192, 178, 131));
        }

        return view;
    }

    private static class ChoreViewHolder {
        TextView textViewChoreName;
    }
}
