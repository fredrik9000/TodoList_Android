package com.github.fredrik9000.todolist.todolist;


import android.app.AlarmManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentTodoListBinding;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TodoListFragment extends Fragment implements TodoAdapter.OnItemInteractionListener {

    private FragmentTodoListBinding binding;
    private TodoListViewModel todoListViewModel;
    private ActionMode actionMode;
    private TodoAdapter adapter;
    private int lastItemLongClickedPosition;
    private long lastClickedUndoTime = 0;
    private static final int MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000;

    public TodoListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_todo_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.todoList;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TodoAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        final FloatingActionButton addTodoButton = binding.addTodoButton;
        addTodoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TodoListFragmentDirections.ActionTodoListFragmentToAddEditTodoFragment action = TodoListFragmentDirections.actionTodoListFragmentToAddEditTodoFragment(getString(R.string.title_add_todo), "");
                Navigation.findNavController(view).navigate(action);
            }
        });

        todoListViewModel = ViewModelProviders.of(this).get(TodoListViewModel.class);
        todoListViewModel.getTodoList().observe(this, new Observer<List<Todo>>() {
            @Override
            public void onChanged(@Nullable List<Todo> todoList) {
                adapter.submitList(todoList);
                checkForEmptyView(todoList);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSoftKeyboard();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mymenu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete_all_tasks_menu_item) {
            final AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            final List<Todo> removedTodoListItems = todoListViewModel.deleteAllTodoItems(alarmManager);

            if (removedTodoListItems.size() == 0) {
                return super.onOptionsItemSelected(item);
            }

            showDeletedItemsSnackbarWithUndo(removedTodoListItems, alarmManager);
            return true;
        } else if (id == R.id.delete_completed_tasks_menu_item) {
            final AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            final List<Todo> removedTodoListItems = todoListViewModel.deleteCompletedTodoItems(alarmManager);

            if (removedTodoListItems.size() == 0) {
                return super.onOptionsItemSelected(item);
            }

            showDeletedItemsSnackbarWithUndo(removedTodoListItems, alarmManager);
            return true;
        } else if (id == R.id.toggle_night_mode) {
            final int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else { // Assume light theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeletedItemsSnackbarWithUndo(final List<Todo> removedTodoListItems, final AlarmManager alarmManager) {
        Snackbar snackbar = Snackbar.make(
                binding.coordinatorLayout,
                R.string.items_deleted,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUndoDoubleClicked()) {
                    return;
                }
                lastClickedUndoTime = SystemClock.elapsedRealtime();

                todoListViewModel.insertTodoItems(removedTodoListItems, alarmManager);
                Snackbar snackbar2 = Snackbar.make(
                        binding.coordinatorLayout,
                        R.string.undo_successful,
                        Snackbar.LENGTH_SHORT
                );
                snackbar2.show();
            }
        });
        snackbar.show();
    }

    @Override
    public void onItemClick(View view, Todo todo) {
        // Tasks in completed state can't be edited
        if (todo.isCompleted()) {
            return;
        }

        TodoListFragmentDirections.ActionTodoListFragmentToAddEditTodoFragment action = TodoListFragmentDirections.actionTodoListFragmentToAddEditTodoFragment(getString(R.string.title_edit_todo), "");
        action.setId(todo.getId());
        action.setDescription(todo.getDescription());
        action.setPriority(todo.getPriority());
        action.setHasNotification(todo.isNotificationEnabled());

        if (todo.isNotificationEnabled()) {
            action.setNotificationId(todo.getNotificationId());
            action.setNotificationYear(todo.getNotifyYear());
            action.setNotificationMonth(todo.getNotifyMonth());
            action.setNotificationDay(todo.getNotifyDay());
            action.setNotificationHour(todo.getNotifyHour());
            action.setNotificationMinute(todo.getNotifyMinute());
        }

        Navigation.findNavController(view).navigate(action);
    }

    @Override
    public boolean onItemLongClick(int position) {
        if (actionMode != null) {
            return false;
        }

        lastItemLongClickedPosition = position;

        actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.context_main, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_delete) {
                    final Todo todo = adapter.getTodoAt(lastItemLongClickedPosition);
                    todoListViewModel.deleteTodo((AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE), todo);
                    actionMode.finish();
                    Snackbar snackbar = Snackbar.make(
                            binding.coordinatorLayout,
                            R.string.item_deleted,
                            Snackbar.LENGTH_LONG
                    ).setAction(R.string.undo, new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (isUndoDoubleClicked()) {
                                return;
                            }
                            lastClickedUndoTime = SystemClock.elapsedRealtime();

                            if (todo.isNotificationEnabled()) {
                                NotificationUtil.addNotification(getActivity().getApplicationContext(), (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE), todo.getNotificationId(), todo.getDescription(), todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute());
                            }
                            todoListViewModel.insert(todo);
                            Snackbar snackbar2 = Snackbar.make(
                                    binding.coordinatorLayout,
                                    R.string.undo_successful,
                                    Snackbar.LENGTH_SHORT
                            );
                            snackbar2.show();
                        }
                    });
                    snackbar.show();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                TodoListFragment.this.actionMode = null;
            }
        });
        return true;
    }

    @Override
    public void onCompletedToggled(Todo todo, boolean isChecked) {
        // If a notification is active for the completed task, remove it.
        if (isChecked && todo.isNotificationEnabled()) {
            NotificationUtil.removeNotification(getActivity().getApplicationContext(), (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE), todo.getNotificationId());
        }

        Todo todoUpdated = new Todo(todo.getDescription(), todo.getPriority(), 0, false, 0, 0, 0, 0, 0, isChecked);
        todoUpdated.setId(todo.getId());
        todoListViewModel.update(todoUpdated);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        adapter = null;
        super.onDestroyView();
    }

    private void hideSoftKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            // Verify if the soft keyboard is open
            if(imm != null && imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    // Not setting the list to GONE or INVISIBLE due to a bug with the FAB in AddEditTodoFragment when there is no visible list.
    // The bug is that when editing a new task (in which case the task list would have been invisible),
    // then when selecting the EditText (without selecting anything else first) the FAB doesn't float up above the keyboard.
    // However, when there is a visible list the FAB works as it should.
    private void checkForEmptyView(List<Todo> todoList) {
        if (todoList.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
        }
    }
}
