package com.github.fredrik9000.todolist;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.Todo;

import java.util.ArrayList;
import java.util.Calendar;

class TODOAdapter extends RecyclerView.Adapter<TODOAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private final OnItemClickListener clickListener;

    private final ArrayList<Todo> todos;

    TODOAdapter(ArrayList<Todo> todos, OnItemClickListener clickListener) {
        this.todos = todos;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.listview_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder todoItemViewHolder, final int position) {
        Todo todoItem = todos.get(position);
        todoItemViewHolder.textViewTodoName.setText(todoItem.getDescription());

        switch (todoItem.getPriority()) {
            case 0:
                todoItemViewHolder.itemView.setBackgroundColor(Color.rgb(244, 244, 244));
                break;
            case 1:
                todoItemViewHolder.itemView.setBackgroundColor(Color.rgb(220, 208, 192));
                break;
            case 2:
                todoItemViewHolder.itemView.setBackgroundColor(Color.rgb(192, 178, 131));
        }

        if (todoItem.isNotificationEnabled() && !isNotificationExpired(todoItem)) {
            todoItemViewHolder.alarmImageView.setVisibility(View.VISIBLE);
        } else {
            todoItemViewHolder.alarmImageView.setVisibility(View.INVISIBLE);
        }

        todoItemViewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(todoItemViewHolder.getAdapterPosition());
            }
        });

        todoItemViewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return clickListener.onItemLongClick(todoItemViewHolder.getAdapterPosition());
            }
        });
    }

    private boolean isNotificationExpired(Todo todo) {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute(), 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        return notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis();
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTodoName;
        ImageView alarmImageView;
        LinearLayout parentLayout;
        ViewHolder(final View view) {
            super(view);
            textViewTodoName = view.findViewById(R.id.text1);
            alarmImageView = view.findViewById(R.id.alarmImageView);
            parentLayout = view.findViewById(R.id.parent_layout);
        }
    }
}
