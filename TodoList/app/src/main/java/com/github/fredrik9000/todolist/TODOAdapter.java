package com.github.fredrik9000.todolist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.TODO;

import java.util.ArrayList;

class TODOAdapter extends ArrayAdapter<TODO> {
    TODOAdapter(Context context, ArrayList<TODO> todos) {
        super(context, 0, todos);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        TODOItemViewHolder todoItemViewHolder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.listview_item, parent, false);
            todoItemViewHolder = new TODOItemViewHolder();
            todoItemViewHolder.textViewChoreName = view.findViewById(R.id.text1);
            todoItemViewHolder.alarmImageView = view.findViewById(R.id.alarmImageView);
            view.setTag(todoItemViewHolder);
        } else {
            todoItemViewHolder = (TODOItemViewHolder)view.getTag();
        }

        TODO todo = getItem(position);
        todoItemViewHolder.textViewChoreName.setText(todo.getDescription());

        switch (todo.getPriority()) {
            case 0:
                view.setBackgroundColor(Color.rgb(244, 244, 244));
                break;
            case 1:
                view.setBackgroundColor(Color.rgb(220, 208, 192));
                break;
            case 2:
                view.setBackgroundColor(Color.rgb(192, 178, 131));
        }

        if (todo.getHasNotification()) {
            todoItemViewHolder.alarmImageView.setVisibility(View.VISIBLE);
        } else {
            todoItemViewHolder.alarmImageView.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private static class TODOItemViewHolder {
        TextView textViewChoreName;
        ImageView alarmImageView;
    }
}
