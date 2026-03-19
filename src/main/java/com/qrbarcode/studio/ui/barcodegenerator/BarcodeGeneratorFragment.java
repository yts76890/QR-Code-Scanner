package com.qrbarcode.studio.ui.barcodegenerator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.qrbarcode.studio.R;
import com.qrbarcode.studio.data.ScanRecord;
import com.qrbarcode.studio.databinding.FragmentBarcodeGeneratorBinding;
import com.qrbarcode.studio.utils.QRCodeUtils;
import com.qrbarcode.studio.viewmodel.ScanViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BarcodeGeneratorFragment extends Fragment {

    private FragmentBarcodeGeneratorBinding binding;
    private ScanViewModel viewModel;
    private Bitmap currentBarcodeBitmap = null;
    private BarcodeFormat selectedFormat = BarcodeFormat.CODE_128;

    private final String[] FORMATS = {"Code 128", "Code 39", "EAN-13", "EAN-8", "UPC-A"};
    private final BarcodeFormat[] BARCODE_FORMATS = {
            BarcodeFormat.CODE_128, BarcodeFormat.CODE_39,
            BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.UPC_A
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBarcodeGeneratorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        setupFormatSpinner();
        setupInputListener();
        setupButtons();
    }

    private void setupFormatSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, FORMATS);
        binding.spinnerFormat.setAdapter(adapter);
        binding.spinnerFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFormat = BARCODE_FORMATS[position];
                generateBarcode();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupInputListener() {
        binding.etBarcodeContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { generateBarcode(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        binding.btnSaveBarcode.setOnClickListener(v -> saveBarcode());
        binding.btnShareBarcode.setOnClickListener(v -> shareBarcode());
    }

    private void generateBarcode() {
        String content = binding.etBarcodeContent.getText().toString().trim();
        if (content.isEmpty()) {
            binding.ivBarcodePreview.setImageResource(R.drawable.ic_barcode_placeholder);
            currentBarcodeBitmap = null;
            return;
        }

        // Validate EAN/UPC lengths
        if (selectedFormat == BarcodeFormat.EAN_13 && content.length() != 12 && content.length() != 13) {
            binding.tvBarcodeHint.setText("EAN-13 requires 12-13 digits");
            binding.tvBarcodeHint.setVisibility(View.VISIBLE);
            return;
        }
        if (selectedFormat == BarcodeFormat.EAN_8 && content.length() != 7 && content.length() != 8) {
            binding.tvBarcodeHint.setText("EAN-8 requires 7-8 digits");
            binding.tvBarcodeHint.setVisibility(View.VISIBLE);
            return;
        }
        if (selectedFormat == BarcodeFormat.UPC_A && content.length() != 11 && content.length() != 12) {
            binding.tvBarcodeHint.setText("UPC-A requires 11-12 digits");
            binding.tvBarcodeHint.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvBarcodeHint.setVisibility(View.GONE);

        try {
            currentBarcodeBitmap = QRCodeUtils.generateBarcode(content, selectedFormat, 900, 300);
            binding.ivBarcodePreview.setImageBitmap(currentBarcodeBitmap);
        } catch (WriterException e) {
            binding.tvBarcodeHint.setText("Invalid input for selected format");
            binding.tvBarcodeHint.setVisibility(View.VISIBLE);
        }
    }

    private void saveBarcode() {
        if (currentBarcodeBitmap == null) {
            Toast.makeText(requireContext(), "Generate a barcode first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = QRCodeUtils.saveImageToGallery(requireContext(), currentBarcodeBitmap,
                    "Barcode_" + System.currentTimeMillis());
            Toast.makeText(requireContext(), "Saved to gallery!", Toast.LENGTH_SHORT).show();
            viewModel.insert(new ScanRecord(
                    binding.etBarcodeContent.getText().toString().trim(),
                    selectedFormat.name(), "GENERATED",
                    System.currentTimeMillis(), uri != null ? uri.toString() : null));
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareBarcode() {
        if (currentBarcodeBitmap == null) {
            Toast.makeText(requireContext(), "Generate a barcode first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "barcode_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            currentBarcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            Uri contentUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Barcode"));
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Share failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
