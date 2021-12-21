package com.github.fredrik9000.todolist.todolist

import android.app.AlarmManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.AddEditTodoFragment
import com.github.fredrik9000.todolist.databinding.FragmentTodoListBinding
import com.github.fredrik9000.todolist.model.Todo
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.snackbar.Snackbar

@ExperimentalMaterialApi
@ExperimentalFoundationApi
class TodoListFragment : Fragment() {

    private var _binding: FragmentTodoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var todoListViewModel: TodoListViewModel
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todoListViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]
        todoListViewModel.isSearching = false // Need to reset this after rotating

        binding.todoListComposeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.todoListComposeView.setContent {
            MdcTheme {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                actionMode?.finish()
                                Navigation.findNavController(requireView()).navigate(R.id.action_todoListFragment_to_addEditTodoFragment)
                            },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_add_black_24dp),
                                    contentDescription = stringResource(id = R.string.add_todo_item),
                                    tint = colorResource(id = R.color.fab_tint)
                                )
                            }
                        )
                    },
                    content = {
                        TodoListComposable(todoListViewModel, ::onItemClick, ::onItemLongClick)
                    }
                )
            }
        }
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

            showUndoSuccessfulSnackbar()
        }).show()
    }

    private fun showUndoSuccessfulSnackbar() {
        Snackbar.make(
                binding.coordinatorLayout,
                R.string.undo_successful,
                Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun onItemClick(todoItem: Todo) {
        if (!todoItem.isCompleted) {
            actionMode?.finish()
            Navigation.findNavController(requireView()).navigate(R.id.action_todoListFragment_to_addEditTodoFragment, AddEditTodoFragment.createBundleForTodoItem(todoItem))
        }
    }

    private fun onItemLongClick(todoItem: Todo): Boolean {
        if (actionMode != null) {
            return false
        }
        
        actionMode = (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                actionMode!!.menuInflater.inflate(R.menu.context_main, menu)
                menu?.findItem(R.id.menu_delete)?.title = getString(R.string.delete_specified_item, todoItem.title)
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_delete) {
                    actionMode.finish()
                    deleteSingleTodoItem(todoItem)
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

    private fun deleteSingleTodoItem(todo: Todo) {
        todoListViewModel.deleteTodo((activity as AppCompatActivity).getSystemService(Context.ALARM_SERVICE) as AlarmManager, todo)

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

            showUndoSuccessfulSnackbar()
        }).show()
    }
}