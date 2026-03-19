package com.qrbarcode.studio.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.qrbarcode.studio.databinding.FragmentSettingsBinding;
import com.qrbarcode.studio.viewmodel.ScanViewModel;

public class SettingsFragment extends Fragment {

    public static final String PREF_VIBRATE = "pref_vibrate";
    public static final String PREF_BEEP = "pref_beep";
    public static final String PREF_AUTO_OPEN_URL = "pref_auto_open_url";

    private FragmentSettingsBinding binding;
    private ScanViewModel viewModel;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        binding.switchVibrate.setChecked(prefs.getBoolean(PREF_VIBRATE, true));
        binding.switchBeep.setChecked(prefs.getBoolean(PREF_BEEP, true));
        binding.switchAutoOpenUrl.setChecked(prefs.getBoolean(PREF_AUTO_OPEN_URL, false));
    }

    private void setupListeners() {
        binding.switchVibrate.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(PREF_VIBRATE, checked).apply());

        binding.switchBeep.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(PREF_BEEP, checked).apply());

        binding.switchAutoOpenUrl.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(PREF_AUTO_OPEN_URL, checked).apply());

        binding.btnClearHistory.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Clear History")
                        .setMessage("Are you sure you want to delete all scan history? This cannot be undone.")
                        .setPositiveButton("Clear", (d, w) -> {
                            viewModel.deleteAll();
                            Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        binding.tvAppVersion.setText("QR Barcode Studio v1.0");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
