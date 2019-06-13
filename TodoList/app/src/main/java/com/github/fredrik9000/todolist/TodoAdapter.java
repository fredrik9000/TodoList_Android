package com.github.fredrik9000.todolist;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.fredrik9000.todolist.databinding.ListviewItemBinding;
import com.github.fredrik9000.todolist.model.Todo;

import java.util.ArrayList;
import java.util.Calendar;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private final OnItemClickListener clickListener;

    private final ArrayList<Todo> todoList;

    TodoAdapter(ArrayList<Todo> todoList, OnItemClickListener clickListener) {
        this.todoList = todoList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ListviewItemBinding binding = ListviewItemBinding.inflate(inflater, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder todoItemViewHolder, final int position) {
        Todo todoItem = todoList.get(position);
        todoItemViewHolder.bind(todoItem);

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
        return todoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ListviewItemBinding binding;
        ImageView alarmImageView;
        LinearLayout parentLayout;

        ViewHolder(ListviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            alarmImageView = binding.alarmImageView;
            parentLayout = binding.parentLayout;
        }

        void bind(Todo todo) {
            binding.setTodo(todo);
            binding.executePendingBindings();
        }
    }
}
