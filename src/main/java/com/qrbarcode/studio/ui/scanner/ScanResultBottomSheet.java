package com.qrbarcode.studio.ui.scanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qrbarcode.studio.databinding.BottomSheetScanResultBinding;

public class ScanResultBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CONTENT = "content";
    private static final String ARG_FORMAT = "format";

    private BottomSheetScanResultBinding binding;
    private Runnable onDismissListener;
    private OnSaveListener onSaveListener;

    public interface OnSaveListener {
        void onSave(String content, String format);
    }

    public static ScanResultBottomSheet newInstance(String content, String format) {
        ScanResultBottomSheet sheet = new ScanResultBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT, content);
        args.putString(ARG_FORMAT, format);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.onSaveListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetScanResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String content = getArguments() != null ? getArguments().getString(ARG_CONTENT, "") : "";
        String format = getArguments() != null ? getArguments().getString(ARG_FORMAT, "") : "";

        binding.tvContent.setText(content);
        binding.tvFormat.setText(format);

        // Detect if it's a URL
        boolean isUrl = content.startsWith("http://") || content.startsWith("https://");
        binding.btnOpenUrl.setVisibility(isUrl ? View.VISIBLE : View.GONE);

        binding.btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Code", content));
            Toast.makeText(requireContext(), "Copied!", Toast.LENGTH_SHORT).show();
        });

        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        binding.btnOpenUrl.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(content));
            startActivity(browserIntent);
        });

        binding.btnSave.setOnClickListener(v -> {
            if (onSaveListener != null) {
                onSaveListener.onSave(content, format);
            }
            dismiss();
        });

        binding.btnScanAgain.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.run();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
