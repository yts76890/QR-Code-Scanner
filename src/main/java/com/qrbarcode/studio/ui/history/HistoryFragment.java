package com.qrbarcode.studio.ui.history;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.qrbarcode.studio.R;
import com.qrbarcode.studio.data.ScanRecord;
import com.qrbarcode.studio.databinding.FragmentHistoryBinding;
import com.qrbarcode.studio.viewmodel.ScanViewModel;

import java.util.Set;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private ScanViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupSearch();
        setupSelectionToolbar();
        observeRecords();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        adapter.setOnItemClickListener(record -> showDetailDialog(record));

        adapter.setOnSelectionChangedListener(count -> {
            if (count > 0) {
                binding.selectionToolbar.setVisibility(View.VISIBLE);
                binding.tvSelectionCount.setText(count + " selected");
            } else {
                binding.selectionToolbar.setVisibility(View.GONE);
                adapter.setMultiSelectMode(false);
            }
        });

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No-op: handle in subclass
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvHistory);
    }

    private void setupFilters() {
        binding.chipAll.setOnClickListener(v -> {
            viewModel.setFilter("ALL");
            observeRecords();
        });
        binding.chipScanned.setOnClickListener(v -> {
            viewModel.setFilter("SCANNED");
            observeRecords();
        });
        binding.chipGenerated.setOnClickListener(v -> {
            viewModel.setFilter("GENERATED");
            observeRecords();
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
                observeRecords();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSelectionToolbar() {
        binding.btnDeleteSelected.setOnClickListener(v -> {
            Set<Integer> ids = adapter.getSelectedIds();
            for (int id : ids) {
                viewModel.deleteById(id);
            }
            adapter.setMultiSelectMode(false);
            binding.selectionToolbar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), ids.size() + " items deleted", Toast.LENGTH_SHORT).show();
        });

        binding.btnCancelSelection.setOnClickListener(v -> {
            adapter.setMultiSelectMode(false);
            binding.selectionToolbar.setVisibility(View.GONE);
        });
    }

    private void observeRecords() {
        viewModel.getRecords().observe(getViewLifecycleOwner(), records -> {
            if (records == null || records.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvHistory.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvHistory.setVisibility(View.VISIBLE);
                adapter.setRecords(records);
            }
        });
    }

    private void showDetailDialog(ScanRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle(record.format)
                .setMessage(record.content)
                .setPositiveButton("Copy", (d, w) -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", record.content));
                    Toast.makeText(requireContext(), "Copied!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Share", (d, w) -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, record.content);
                    startActivity(Intent.createChooser(intent, "Share"));
                })
                .setNegativeButton("Delete", (d, w) -> {
                    viewModel.delete(record);
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
