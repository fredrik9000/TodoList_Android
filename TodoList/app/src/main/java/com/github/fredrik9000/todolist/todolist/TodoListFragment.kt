package com.github.fredrik9000.todolist.todolist

import android.app.AlarmManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.AddEditTodoFragment
import com.github.fredrik9000.todolist.databinding.FragmentTodoListBinding
import com.github.fredrik9000.todolist.model.Todo
import com.github.fredrik9000.todolist.notifications.NotificationUtil
import com.github.fredrik9000.todolist.todolist.TodoAdapter.OnItemInteractionListener
import com.google.android.material.snackbar.Snackbar

class TodoListFragment : Fragment(), OnItemInteractionListener {

    private var _binding: FragmentTodoListBinding? = null
    private val binding get() = _binding!!
    private var _adapter: TodoAdapter? = null
    private val adapter get() = _adapter!!

    private lateinit var todoListViewModel: TodoListViewModel
    private var actionMode: ActionMode? = null
    private var lastItemLongClickedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.todoList
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        _adapter = TodoAdapter(requireContext(), this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, layoutManager.orientation))

        todoListViewModel = ViewModelProvider(this).get(TodoListViewModel::class.java)
        todoListViewModel.isSearching = false // Need to reset this after rotating
        todoListViewModel.getTodoList().observe(viewLifecycleOwner, Observer { todoList ->
            adapter.submitList(todoList)
            showOrHideOnboardingView(todoList)
        })

        val addTodoButton = binding.addTodoButton
        addTodoButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_todoListFragment_to_addEditTodoFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // The keyboard could be open from when editing the task
        hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.mymenu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                todoListViewModel.searchTodoList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                todoListViewModel.searchTodoList(newText)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_all_tasks_menu_item -> {
                val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val removedTodoListItems = todoListViewModel.deleteAllTodoItems(alarmManager)
                if (removedTodoListItems.size == 0) {
                    return super.onOptionsItemSelected(item)
                }
                showDeletedItemsSnackbarWithUndo(removedTodoListItems, alarmManager)
                return true
            }
            R.id.delete_completed_tasks_menu_item -> {
                val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val removedTodoListItems = todoListViewModel.deleteAllCompletedTodoItems(alarmManager)
                if (removedTodoListItems.size == 0) {
                    return super.onOptionsItemSelected(item)
                }
                showDeletedItemsSnackbarWithUndo(removedTodoListItems, alarmManager)
                return true
            }
            R.id.toggle_night_mode -> {
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else { // Assume light theme
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showDeletedItemsSnackbarWithUndo(removedTodoListItems: MutableList<Todo>, alarmManager: AlarmManager) {
        Snackbar.make(
                binding.coordinatorLayout,
                R.string.items_deleted,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.undo, View.OnClickListener {
            if (todoListViewModel.isUndoDoubleClicked) {
                return@OnClickListener
            }

            todoListViewModel.updateLastClickedUndoTime()
            todoListViewModel.insertTodoItems(removedTodoListItems, alarmManager)

            Snackbar.make(
                    binding.coordinatorLayout,
                    R.string.undo_successful,
                    Snackbar.LENGTH_SHORT
            ).show()
        }).show()
    }

    override fun onItemClick(view: View, todo: Todo) {
        // Disable editing for completed tasks
        if (todo.isCompleted) {
            return
        }
        Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_addEditTodoFragment, AddEditTodoFragment.createBundleForTodoItem(todo))
    }

    override fun onItemLongClick(position: Int): Boolean {
        if (actionMode != null) {
            return false
        }

        lastItemLongClickedPosition = position

        actionMode = (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                actionMode!!.menuInflater.inflate(R.menu.context_main, menu)
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_delete) {
                    val todo = adapter.getTodoAt(lastItemLongClickedPosition)
                    todoListViewModel.deleteTodo((activity as AppCompatActivity).getSystemService(Context.ALARM_SERVICE) as AlarmManager, todo)
                    actionMode.finish()

                    Snackbar.make(
                            binding.coordinatorLayout,
                            R.string.item_deleted,
                            Snackbar.LENGTH_LONG
                    ).setAction(R.string.undo, View.OnClickListener {
                        if (todoListViewModel.isUndoDoubleClicked) {
                            return@OnClickListener
                        }

                        todoListViewModel.updateLastClickedUndoTime()
                        todoListViewModel.insertTodo(todo, (activity as AppCompatActivity).getSystemService(Context.ALARM_SERVICE) as AlarmManager)

                        Snackbar.make(
                                binding.coordinatorLayout,
                                R.string.undo_successful,
                                Snackbar.LENGTH_SHORT
                        ).show()
                    }).show()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(actionMode: ActionMode?) {
                this@TodoListFragment.actionMode = null
            }
        })
        return true
    }

    override fun onCompletedToggled(todo: Todo, isChecked: Boolean) {
        // If a notification is active for the completed task, remove it.
        if (isChecked) {
            if (todo.notificationEnabled) {
                val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                NotificationUtil.removeNotification(requireActivity().applicationContext, alarmManager, todo.notificationId)
            }
            if (todo.geofenceNotificationEnabled) {
                NotificationUtil.removeGeofenceNotification(requireActivity().applicationContext, todo.geofenceNotificationId)
            }
        }

        todoListViewModel.update(Todo(
                todo.id,
                todo.title,
                todo.description,
                todo.priority,
                0,
                0,
                false,
                0,
                0,
                0,
                0,
                0,
                false,
                0.0,
                0.0,
                0,
                isChecked))
    }

    private fun hideSoftKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // Verify if the soft keyboard is open
            if (imm.isAcceptingText) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    // Not setting the list to GONE or INVISIBLE due to a bug with the FAB in AddEditTodoFragment when there is no visible list.
    // The bug is that when editing a new task (in which case the task list would have been invisible),
    // then when selecting the EditText (without selecting anything else first) the FAB doesn't float up above the keyboard.
    // However, when there is a visible list the FAB works as it should.
    private fun showOrHideOnboardingView(todoList: MutableList<Todo>) {
        if (todoList.isEmpty() && !todoListViewModel.isSearching) {
            binding.onboardingView.visibility = View.VISIBLE
        } else {
            binding.onboardingView.visibility = View.GONE
        }
    }
}