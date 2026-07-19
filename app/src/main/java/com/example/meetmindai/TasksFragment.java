package com.example.meetmindai;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView rvTasks;
    private FloatingActionButton fabAddTask;
    private EditText etSearch;
    private TextView tvTotalTugas, tvBelumDikerjakan, tvSedangDikerjakan, tvSelesai;
    private TaskAdapter adapter;
    private List<TaskModel> taskList = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        etSearch = view.findViewById(R.id.etSearch); 
        
        tvTotalTugas = view.findViewById(R.id.tvTotalTugas);
        tvBelumDikerjakan = view.findViewById(R.id.tvBelumDikerjakan);
        tvSedangDikerjakan = view.findViewById(R.id.tvSedangDikerjakan);
        tvSelesai = view.findViewById(R.id.tvSelesai);

        fabAddTask.setOnClickListener(v -> {
            AddTaskFragment addTaskFragment = new AddTaskFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, addTaskFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        rvTasks.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDatabase = FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("tasks");

        loadTasks();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadTasks() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    TaskModel task = data.getValue(TaskModel.class);
                    if (task != null) {
                        task.setId(data.getKey());
                        taskList.add(task);
                    }
                }
                applyFilters();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TasksFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void applyFilters() {
        List<TaskModel> filteredList = new ArrayList<>();
        int total = 0, todo = 0, inProgress = 0, completed = 0;
        String query = etSearch.getText().toString().toLowerCase().trim();

        for (TaskModel task : taskList) {
            total++;
            String status = task.getStatus();
            if ("To Do".equalsIgnoreCase(status)) todo++;
            else if ("In Progress".equalsIgnoreCase(status)) inProgress++;
            else if ("Completed".equalsIgnoreCase(status)) completed++;

            if (task.getTitle() != null && task.getTitle().toLowerCase().contains(query)) {
                filteredList.add(task);
            }
        }

        // Update Summary Cards
        tvTotalTugas.setText(String.valueOf(total));
        tvBelumDikerjakan.setText(String.valueOf(todo));
        tvSedangDikerjakan.setText(String.valueOf(inProgress));
        tvSelesai.setText(String.valueOf(completed));

        // Update List
        adapter = new TaskAdapter(filteredList);
        rvTasks.setAdapter(adapter);
    }
}
