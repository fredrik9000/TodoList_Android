package com.github.fredrik9000.todolist.todolist;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.ListviewItemBinding;
import com.github.fredrik9000.todolist.model.Todo;

import java.util.Calendar;

public class TodoAdapter extends ListAdapter<Todo, TodoAdapter.ViewHolder> {

    private Context context;

    private static final DiffUtil.ItemCallback<Todo> DIFF_CALLBACK = new DiffUtil.ItemCallback<Todo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getNote().equals(newItem.getNote()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    oldItem.isNotificationEnabled() == newItem.isNotificationEnabled() &&
                    oldItem.getNotificationId() == newItem.getNotificationId() &&
                    oldItem.isCompleted() == newItem.isCompleted();
        }
    };
    private final OnItemInteractionListener interactionListener;

    TodoAdapter(Context context, OnItemInteractionListener interactionListener) {
        super(DIFF_CALLBACK);
        this.interactionListener = interactionListener;
        this.context = context;
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
        Todo todoItem = getItem(position);
        todoItemViewHolder.bind(todoItem);

        switch (todoItem.getPriority()) {
            case 0:
                CompoundButtonCompat.setButtonTintList(todoItemViewHolder.binding.completeCheckbox, ContextCompat.getColorStateList(context, R.color.low_priority));
                break;
            case 1:
                CompoundButtonCompat.setButtonTintList(todoItemViewHolder.binding.completeCheckbox, ContextCompat.getColorStateList(context, R.color.medium_priority));
                break;
            case 2:
                CompoundButtonCompat.setButtonTintList(todoItemViewHolder.binding.completeCheckbox, ContextCompat.getColorStateList(context, R.color.high_priority));
        }

        if (todoItem.isNotificationEnabled() && !isNotificationExpired(todoItem)) {
            todoItemViewHolder.binding.alarmImageView.setVisibility(View.VISIBLE);
        } else {
            todoItemViewHolder.binding.alarmImageView.setVisibility(View.GONE);
        }

        if (todoItem.getNote() != null && todoItem.getNote().length() > 0) {
            todoItemViewHolder.binding.noteImageView.setVisibility(View.VISIBLE);
        } else {
            todoItemViewHolder.binding.noteImageView.setVisibility(View.GONE);
        }

        if (todoItem.isCompleted()) {
            todoItemViewHolder.binding.descriptionTextView.setPaintFlags(todoItemViewHolder.binding.descriptionTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            todoItemViewHolder.binding.descriptionTextView.setPaintFlags(todoItemViewHolder.binding.descriptionTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private boolean isNotificationExpired(Todo todo) {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute(), 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        return notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis();
    }

    Todo getTodoAt(int position) {
        return getItem(position);
    }

    public interface OnItemInteractionListener {
        void onItemClick(View view, Todo todo);
        boolean onItemLongClick(int position);
        void onCompletedToggled(Todo todo, boolean isChecked);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ListviewItemBinding binding;

        ViewHolder(final ListviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (interactionListener != null && position != RecyclerView.NO_POSITION) {
                        interactionListener.onItemClick(view, getItem(position));
                    }
                }
            });

            binding.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (interactionListener != null && position != RecyclerView.NO_POSITION) {
                        return interactionListener.onItemLongClick(position);
                    } else {
                        return false;
                    }
                }
            });

            binding.completeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    interactionListener.onCompletedToggled(getItem(getAdapterPosition()), isChecked);
                }
            });
        }

        void bind(Todo todo) {
            binding.setTodo(todo);
            binding.executePendingBindings();
        }
    }
}
