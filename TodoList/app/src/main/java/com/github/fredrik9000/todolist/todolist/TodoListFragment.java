package com.github.fredrik9000.todolist.todolist;


import android.app.AlarmManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentTodoListBinding;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TodoListFragment extends Fragment implements TodoAdapter.OnItemInteractionListener, DeleteTodosDialog.OnDeleteTodosDialogInteractionListener {

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
        adapter = new TodoAdapter(this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        final FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mymenu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.deletecCoreMenuButton) {
            DeleteTodosDialog deleteTodosDialog = new DeleteTodosDialog();
            deleteTodosDialog.setTargetFragment(TodoListFragment.this, 1);
            deleteTodosDialog.show(getFragmentManager(), "DeleteTodosDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    if (todo.isNotificationEnabled()) {
                        NotificationUtil.removeNotification(getActivity().getApplicationContext(), (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE), todo.getNotificationId());
                    }
                    todoListViewModel.delete(todo);
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
    public void onCompletedChecked(Todo todo) {
        todo.setCompleted(true);

        // If a notification was active for the completed task, remove it.
        if (todo.isNotificationEnabled()) {
            NotificationUtil.removeNotification(getActivity().getApplicationContext(), (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE), todo.getNotificationId());
            todo.setNotificationEnabled(false);
            todo.setNotificationId(0);
            todo.setNotifyYear(0);
            todo.setNotifyMonth(0);
            todo.setNotifyDay(0);
            todo.setNotifyHour(0);
            todo.setNotifyMinute(0);
        }

        todoListViewModel.update(todo);
    }

    @Override
    public void onCompletedUnchecked(Todo todo) {
        todo.setCompleted(false);
        todoListViewModel.update(todo);
    }

    @Override
    public void onDeleteTodosDialogInteraction(ArrayList<Integer> priorities) {
        final AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        final List<Todo> removedTodoListItems = todoListViewModel.deleteTodosWithPriorities(priorities, alarmManager);

        //The selected priorities didn't match any of the tasks, so no items will be removed.
        if (removedTodoListItems.size() == 0) {
            return;
        }

        Snackbar snackbar = Snackbar.make(
                binding.coordinatorLayout,
                R.string.items_deleted,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUndoDoubleClicked()){
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

    private boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    private void checkForEmptyView(List<Todo> todoList) {
        if (todoList.isEmpty()) {
            binding.todoList.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.todoList.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }
}
