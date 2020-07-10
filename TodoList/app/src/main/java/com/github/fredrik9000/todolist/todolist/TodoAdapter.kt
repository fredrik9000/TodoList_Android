package com.github.fredrik9000.todolist.todolist

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.AddEditTodoViewModel
import com.github.fredrik9000.todolist.databinding.ListviewItemBinding
import com.github.fredrik9000.todolist.model.Todo
import java.util.*

class TodoAdapter(private val context: Context, private val interactionListener: OnItemInteractionListener) : ListAdapter<Todo, TodoAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return ViewHolder(ListviewItemBinding.inflate(inflater, viewGroup, false))
    }

    override fun onBindViewHolder(todoItemViewHolder: ViewHolder, position: Int) {
        val todoItem = getItem(position)!!
        todoItemViewHolder.bind(todoItem)
        when (todoItem.priority) {
            AddEditTodoViewModel.PRIORITY_LOW -> CompoundButtonCompat.setButtonTintList(todoItemViewHolder.binding.completeCheckbox, ContextCompat.getColorStateList(context, R.color.low_priority))
            AddEditTodoViewModel.PRIORITY_MEDIUM -> CompoundButtonCompat.setButtonTintList(todoItemViewHolder.binding.completeCheckbox, ContextCompat.getColorStateList(context, R.color.medium_priority))
            AddEditTodoViewModel.PRIORITY_HIGH -> CompoundButtonCompat.setButtonTintList(todoItemViewHolder.binding.completeCheckbox, ContextCompat.getColorStateList(context, R.color.high_priority))
        }
        if (todoItem.notificationEnabled && !isNotificationExpired(todoItem)) {
            todoItemViewHolder.binding.alarmImageView.visibility = View.VISIBLE
        } else {
            todoItemViewHolder.binding.alarmImageView.visibility = View.GONE
        }
        if (todoItem.geofenceNotificationEnabled) {
            todoItemViewHolder.binding.geofenceImageView.visibility = View.VISIBLE
        } else {
            todoItemViewHolder.binding.geofenceImageView.visibility = View.GONE
        }
        if (!todoItem.description.isNullOrEmpty()) {
            todoItemViewHolder.binding.descriptionImageView.visibility = View.VISIBLE
        } else {
            todoItemViewHolder.binding.descriptionImageView.visibility = View.GONE
        }
        if (todoItem.isCompleted) {
            todoItemViewHolder.binding.titleTextView.paintFlags = todoItemViewHolder.binding.titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            todoItemViewHolder.binding.titleTextView.paintFlags = todoItemViewHolder.binding.titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private fun isNotificationExpired(todo: Todo): Boolean {
        val notificationCalendar = Calendar.getInstance()
        notificationCalendar[todo.notifyYear, todo.notifyMonth, todo.notifyDay, todo.notifyHour, todo.notifyMinute] = 0
        return notificationCalendar.timeInMillis < Calendar.getInstance().timeInMillis
    }

    fun getTodoAt(position: Int): Todo {
        return getItem(position)
    }

    interface OnItemInteractionListener {
        fun onItemClick(view: View, todo: Todo)
        fun onItemLongClick(position: Int): Boolean
        fun onCompletedToggled(todo: Todo, isChecked: Boolean)
    }

    inner class ViewHolder(val binding: ListviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.parentLayout.setOnClickListener { view ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    interactionListener.onItemClick(view, getItem(bindingAdapterPosition))
                }
            }
            binding.parentLayout.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    interactionListener.onItemLongClick(bindingAdapterPosition)
                } else {
                    false
                }
            }
            binding.completeCheckbox.setOnCheckedChangeListener { _, isChecked -> interactionListener.onCompletedToggled(getItem(bindingAdapterPosition), isChecked) }
        }

        fun bind(todo: Todo?) {
            binding.todo = todo
            binding.executePendingBindings()
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.title == newItem.title &&
                        oldItem.description == newItem.description &&
                        oldItem.priority == newItem.priority &&
                        oldItem.notificationEnabled == newItem.notificationEnabled &&
                        oldItem.geofenceNotificationEnabled == newItem.geofenceNotificationEnabled &&
                        oldItem.notificationId == newItem.notificationId &&
                        oldItem.geofenceNotificationId == newItem.geofenceNotificationId &&
                        oldItem.isCompleted == newItem.isCompleted
            }
        }
    }
}