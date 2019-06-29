package com.github.fredrik9000.todolist;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.fredrik9000.todolist.databinding.ListviewItemBinding;
import com.github.fredrik9000.todolist.model.Todo;

import java.util.Calendar;

public class TodoAdapter extends ListAdapter<Todo, TodoAdapter.ViewHolder> {

    protected TodoAdapter(OnItemClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Todo todo);
        boolean onItemLongClick(int position);
    }

    private static final DiffUtil.ItemCallback<Todo> DIFF_CALLBACK = new DiffUtil.ItemCallback<Todo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    oldItem.isNotificationEnabled() == newItem.isNotificationEnabled() &&
                    oldItem.getNotificationId() == newItem.getNotificationId();
        }
    };

    private final OnItemClickListener clickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ListviewItemBinding binding = ListviewItemBinding.inflate(inflater, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder todoItemViewHolder, final int position) {
        Todo todoItem = getItem(position);
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
    }

    private boolean isNotificationExpired(Todo todo) {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute(), 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        return notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis();
    }

    public Todo getTodoAt(int position) {
        return getItem(position);
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

            parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (clickListener != null && position != RecyclerView.NO_POSITION) {
                        clickListener.onItemClick(getItem(position));
                    }
                }
            });

            parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (clickListener != null && position != RecyclerView.NO_POSITION) {
                        return clickListener.onItemLongClick(position);
                    } else {
                        return false;
                    }
                }
            });
        }

        void bind(Todo todo) {
            binding.setTodo(todo);
            binding.executePendingBindings();
        }
    }
}
