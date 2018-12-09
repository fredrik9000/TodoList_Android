package com.github.fredrik9000.todolist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.TODO;

import java.util.ArrayList;

class TODOAdapter extends RecyclerView.Adapter<TODOAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private final OnItemClickListener clickListener;

    private final ArrayList<TODO> todos;

    TODOAdapter(ArrayList<TODO> todos, OnItemClickListener clickListener) {
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
        TODO todoItem = todos.get(position);
        todoItemViewHolder.textViewChoreName.setText(todoItem.getDescription());

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

        if (todoItem.getHasNotification()) {
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

    @Override
    public int getItemCount() {
        return todos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChoreName;
        ImageView alarmImageView;
        LinearLayout parentLayout;
        ViewHolder(final View view) {
            super(view);
            textViewChoreName = view.findViewById(R.id.text1);
            alarmImageView = view.findViewById(R.id.alarmImageView);
            parentLayout = view.findViewById(R.id.parent_layout);
        }
    }
}