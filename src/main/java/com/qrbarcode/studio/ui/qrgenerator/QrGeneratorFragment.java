package com.qrbarcode.studio.ui.qrgenerator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.zxing.WriterException;
import com.qrbarcode.studio.R;
import com.qrbarcode.studio.data.ScanRecord;
import com.qrbarcode.studio.databinding.FragmentQrGeneratorBinding;
import com.qrbarcode.studio.utils.QRCodeUtils;
import com.qrbarcode.studio.viewmodel.ScanViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QrGeneratorFragment extends Fragment {

    private FragmentQrGeneratorBinding binding;
    private ScanViewModel viewModel;
    private int fgColor = Color.BLACK;
    private int bgColor = Color.WHITE;
    private int qrSize = 512;
    private Bitmap currentLogoBitmap = null;
    private Bitmap currentQrBitmap = null;
    private String currentTabType = "URL";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        currentLogoBitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(requireContext().getContentResolver(), uri);
                        generateQrCode();
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQrGeneratorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        setupTabs();
        setupInputListeners();
        setupButtons();
        setupSizeSeekBar();
    }

    private void setupTabs() {
        String[] tabs = {"URL", "Text", "Email", "Phone", "WiFi", "vCard", "SMS"};
        for (String tab : tabs) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tab));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabType = tab.getText() != null ? tab.getText().toString() : "URL";
                updateInputFields();
                generateQrCode();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        updateInputFields();
    }

    private void updateInputFields() {
        binding.layoutUrl.setVisibility(View.GONE);
        binding.layoutText.setVisibility(View.GONE);
        binding.layoutEmail.setVisibility(View.GONE);
        binding.layoutPhone.setVisibility(View.GONE);
        binding.layoutWifi.setVisibility(View.GONE);
        binding.layoutVcard.setVisibility(View.GONE);
        binding.layoutSms.setVisibility(View.GONE);

        switch (currentTabType) {
            case "URL":   binding.layoutUrl.setVisibility(View.VISIBLE); break;
            case "Text":  binding.layoutText.setVisibility(View.VISIBLE); break;
            case "Email": binding.layoutEmail.setVisibility(View.VISIBLE); break;
            case "Phone": binding.layoutPhone.setVisibility(View.VISIBLE); break;
            case "WiFi":  binding.layoutWifi.setVisibility(View.VISIBLE); break;
            case "vCard": binding.layoutVcard.setVisibility(View.VISIBLE); break;
            case "SMS":   binding.layoutSms.setVisibility(View.VISIBLE); break;
        }
    }

    private void setupInputListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { generateQrCode(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        binding.etUrl.addTextChangedListener(watcher);
        binding.etText.addTextChangedListener(watcher);
        binding.etEmailTo.addTextChangedListener(watcher);
        binding.etEmailSubject.addTextChangedListener(watcher);
        binding.etEmailBody.addTextChangedListener(watcher);
        binding.etPhone.addTextChangedListener(watcher);
        binding.etWifiSsid.addTextChangedListener(watcher);
        binding.etWifiPassword.addTextChangedListener(watcher);
        binding.etVcardName.addTextChangedListener(watcher);
        binding.etVcardPhone.addTextChangedListener(watcher);
        binding.etVcardEmail.addTextChangedListener(watcher);
        binding.etVcardOrg.addTextChangedListener(watcher);
        binding.etSmsPhone.addTextChangedListener(watcher);
        binding.etSmsMessage.addTextChangedListener(watcher);
    }

    private void setupButtons() {
        binding.btnFgColor.setOnClickListener(v -> showColorPicker(true));
        binding.btnBgColor.setOnClickListener(v -> showColorPicker(false));
        binding.btnAddLogo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnRemoveLogo.setOnClickListener(v -> {
            currentLogoBitmap = null;
            generateQrCode();
        });
        binding.btnSave.setOnClickListener(v -> saveQrCode());
        binding.btnShare.setOnClickListener(v -> shareQrCode());
    }

    private void setupSizeSeekBar() {
        binding.seekBarSize.setMax(100);
        binding.seekBarSize.setProgress(50);
        binding.seekBarSize.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                qrSize = 256 + (progress * 5);
                binding.tvSizeLabel.setText("Size: " + qrSize + "px");
                generateQrCode();
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
    }

    private String buildQrContent() {
        switch (currentTabType) {
            case "URL":
                String url = binding.etUrl.getText().toString().trim();
                return url.isEmpty() ? "" : (url.startsWith("http") ? url : "https://" + url);
            case "Text":
                return binding.etText.getText().toString().trim();
            case "Email":
                return QRCodeUtils.buildEmailContent(
                        binding.etEmailTo.getText().toString().trim(),
                        binding.etEmailSubject.getText().toString().trim(),
                        binding.etEmailBody.getText().toString().trim());
            case "Phone":
                String phone = binding.etPhone.getText().toString().trim();
                return phone.isEmpty() ? "" : "tel:" + phone;
            case "WiFi":
                String encryption = "WPA";
                return QRCodeUtils.buildWifiContent(
                        binding.etWifiSsid.getText().toString().trim(),
                        binding.etWifiPassword.getText().toString().trim(),
                        encryption);
            case "vCard":
                return QRCodeUtils.buildVCardContent(
                        binding.etVcardName.getText().toString().trim(),
                        binding.etVcardPhone.getText().toString().trim(),
                        binding.etVcardEmail.getText().toString().trim(),
                        binding.etVcardOrg.getText().toString().trim());
            case "SMS":
                return QRCodeUtils.buildSmsContent(
                        binding.etSmsPhone.getText().toString().trim(),
                        binding.etSmsMessage.getText().toString().trim());
            default: return "";
        }
    }

    private void generateQrCode() {
        String content = buildQrContent();
        if (content.isEmpty()) {
            binding.ivQrPreview.setImageResource(R.drawable.ic_qr_placeholder);
            currentQrBitmap = null;
            return;
        }
        try {
            Bitmap qr = QRCodeUtils.generateQRCode(content, qrSize, fgColor, bgColor);
            if (currentLogoBitmap != null) {
                qr = QRCodeUtils.overlayLogo(qr, currentLogoBitmap);
            }
            currentQrBitmap = qr;
            binding.ivQrPreview.setImageBitmap(qr);
        } catch (WriterException e) {
            Toast.makeText(requireContext(), "Error generating QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQrCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(requireContext(), "Generate a QR code first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = QRCodeUtils.saveImageToGallery(requireContext(), currentQrBitmap,
                    "QR_" + System.currentTimeMillis());
            Toast.makeText(requireContext(), "Saved to gallery!", Toast.LENGTH_SHORT).show();
            String content = buildQrContent();
            viewModel.insert(new ScanRecord(content, "QR Code", "GENERATED",
                    System.currentTimeMillis(), uri != null ? uri.toString() : null));
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQrCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(requireContext(), "Generate a QR code first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "qr_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Share failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showColorPicker(boolean isForeground) {
        int[] colors = {
                Color.BLACK, Color.WHITE, Color.RED, Color.BLUE,
                Color.GREEN, Color.MAGENTA, Color.CYAN, Color.DKGRAY
        };
        String[] colorNames = {"Black", "White", "Red", "Blue", "Green", "Magenta", "Cyan", "Dark Gray"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(isForeground ? "Foreground Color" : "Background Color")
                .setItems(colorNames, (dialog, which) -> {
                    if (isForeground) {
                        fgColor = colors[which];
                        binding.btnFgColor.setBackgroundColor(fgColor);
                    } else {
                        bgColor = colors[which];
                        binding.btnBgColor.setBackgroundColor(bgColor);
                    }
                    generateQrCode();
                }).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
