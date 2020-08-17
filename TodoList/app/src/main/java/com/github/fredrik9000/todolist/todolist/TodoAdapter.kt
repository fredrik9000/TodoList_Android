package com.github.fredrik9000.todolist.todolist

import android.animation.ObjectAnimator
import android.content.Context
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
            todoItemViewHolder.binding.reminderNotificationLayout.visibility = View.VISIBLE
        } else {
            todoItemViewHolder.binding.reminderNotificationLayout.visibility = View.GONE
        }

        if (todoItem.geofenceNotificationEnabled) {
            todoItemViewHolder.binding.geofenceNotificationLayout.visibility = View.VISIBLE
        } else {
            todoItemViewHolder.binding.geofenceNotificationLayout.visibility = View.GONE
        }

        if (todoItem.notificationEnabled || todoItem.geofenceNotificationEnabled) {
            todoItemViewHolder.binding.notificationLayout.visibility = View.VISIBLE
        } else {
            todoItemViewHolder.binding.notificationLayout.visibility = View.GONE
        }

        if (!todoItem.description.isNullOrEmpty()) {
            todoItemViewHolder.binding.expandDescriptionImageView.visibility = View.VISIBLE
            todoItemViewHolder.binding.descriptionLayout.visibility = View.VISIBLE

            // The following code makes sure that the expandable description image is only visible when it needs to be.
            // In order to correctly fetch the line count and ellipsis count we need to post a Runnable in order to allow the text view to render.
            todoItemViewHolder.binding.descriptionTextView.post {
                val descriptionCollapsedMaxLines = context.resources.getInteger(R.integer.max_lines_collapsed_list_item_description)
                if (todoItemViewHolder.binding.descriptionTextView.maxLines == descriptionCollapsedMaxLines) {
                    val textViewLayout = todoItemViewHolder.binding.descriptionTextView.layout
                    val lineCount = textViewLayout.lineCount
                    if (lineCount == descriptionCollapsedMaxLines && textViewLayout.getEllipsisCount(1) > 0) {
                        todoItemViewHolder.binding.expandDescriptionImageView.visibility = View.VISIBLE
                    } else {
                        todoItemViewHolder.binding.expandDescriptionImageView.visibility = View.GONE
                    }
                }
            }
        } else {
            todoItemViewHolder.binding.expandDescriptionImageView.visibility = View.GONE
            todoItemViewHolder.binding.descriptionLayout.visibility = View.GONE
        }

        if (todoItem.isCompleted) {
            todoItemViewHolder.binding.listItemCardView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.cardViewCompletedBackgroundColor)
        } else {
            todoItemViewHolder.binding.listItemCardView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.cardViewBackgroundColor)
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
            binding.completeCheckbox.setOnCheckedChangeListener { view, isChecked ->
                // OnCheckedChangeListener will be called when the items are filtered through searching,
                // so if the user didn't initiate the action, then return
                if (!view.isPressed) {
                    return@setOnCheckedChangeListener
                }
                interactionListener.onCompletedToggled(getItem(bindingAdapterPosition), isChecked)
            }

            binding.expandDescriptionImageView.setOnClickListener {
                if (binding.descriptionTextView.maxLines == context.resources.getInteger(R.integer.max_lines_collapsed_list_item_description)) {
                    ObjectAnimator.ofInt(
                            binding.descriptionTextView,
                            "maxLines",
                            context.resources.getInteger(R.integer.max_lines_expanded_list_item_description))
                            .also { it.duration = context.resources.getInteger(R.integer.expand_collapse_animation_duration).toLong() }.start()
                    binding.expandDescriptionImageView.setImageResource(R.drawable.ic_description_arrow_up_24)
                } else {
                    ObjectAnimator.ofInt(
                            binding.descriptionTextView,
                            "maxLines",
                            context.resources.getInteger(R.integer.max_lines_collapsed_list_item_description))
                            .also { it.duration = context.resources.getInteger(R.integer.expand_collapse_animation_duration).toLong() }.start()
                    binding.expandDescriptionImageView.setImageResource(R.drawable.ic_description_arrow_down_24)
                }
            }
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
                        oldItem.notifyYear == newItem.notifyYear &&
                        oldItem.notifyMonth == newItem.notifyMonth &&
                        oldItem.notifyDay == newItem.notifyDay &&
                        oldItem.notifyHour == newItem.notifyHour &&
                        oldItem.notifyMinute == newItem.notifyMinute &&
                        oldItem.geofenceNotificationId == newItem.geofenceNotificationId &&
                        oldItem.geofenceLatitude == newItem.geofenceLatitude &&
                        oldItem.geofenceLongitude == newItem.geofenceLongitude &&
                        oldItem.isCompleted == newItem.isCompleted
            }
        }
    }
}