package com.qrbarcode.studio.ui.scanner;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.qrbarcode.studio.R;
import com.qrbarcode.studio.data.ScanRecord;
import com.qrbarcode.studio.databinding.FragmentScannerBinding;
import com.qrbarcode.studio.viewmodel.ScanViewModel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
public class ScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private FragmentScannerBinding binding;
    private ScanViewModel viewModel;
    private ExecutorService cameraExecutor;
    private boolean isFlashOn = false;
    private boolean isFrontCamera = false;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private boolean isScanning = true;
    private boolean sheetShown = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        binding.btnFlash.setOnClickListener(v -> toggleFlash());
        binding.btnFlipCamera.setOnClickListener(v -> flipCamera());

        // Start laser scan animation
        android.view.animation.Animation laserAnim =
                android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.laser_scan);
        binding.laserLine.startAnimation(laserAnim);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(requireContext(), "Camera error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = isFrontCamera
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_PDF417,
                        Barcode.FORMAT_DATA_MATRIX,
                        Barcode.FORMAT_AZTEC
                ).build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (!isScanning || sheetShown) {
                imageProxy.close();
                return;
            }

            @SuppressWarnings("UnsafeOptInUsageError")
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                com.google.mlkit.vision.common.InputImage inputImage =
                        com.google.mlkit.vision.common.InputImage.fromMediaImage(
                                mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                scanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            if (!barcodes.isEmpty() && !sheetShown) {
                                Barcode barcode = barcodes.get(0);
                                String rawValue = barcode.getRawValue();
                                String format = getBarcodeFormatName(barcode.getFormat());
                                if (rawValue != null && !rawValue.isEmpty()) {
                                    sheetShown = true;
                                    isScanning = false;
                                    vibrateOnScan();
                                    requireActivity().runOnUiThread(() ->
                                            showResultBottomSheet(rawValue, format));
                                }
                            }
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        });

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to bind camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResultBottomSheet(String content, String format) {
        ScanResultBottomSheet sheet = ScanResultBottomSheet.newInstance(content, format);
        sheet.setOnDismissListener(() -> {
            sheetShown = false;
            isScanning = true;
        });
        sheet.setOnSaveListener((c, f) -> {
            ScanRecord record = new ScanRecord(c, f, "SCANNED",
                    System.currentTimeMillis(), null);
            viewModel.insert(record);
            Toast.makeText(requireContext(), "Saved to history", Toast.LENGTH_SHORT).show();
        });
        sheet.show(getChildFragmentManager(), "ScanResult");
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            binding.btnFlash.setImageResource(isFlashOn
                    ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        } else {
            Toast.makeText(requireContext(), "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void flipCamera() {
        isFrontCamera = !isFrontCamera;
        isFlashOn = false;
        bindCameraUseCases();
    }

    private void vibrateOnScan() {
        try {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Exception ignored) {}
    }

    private String getBarcodeFormatName(int format) {
        switch (format) {
            case Barcode.FORMAT_QR_CODE: return "QR Code";
            case Barcode.FORMAT_CODE_128: return "Code 128";
            case Barcode.FORMAT_CODE_39: return "Code 39";
            case Barcode.FORMAT_EAN_13: return "EAN-13";
            case Barcode.FORMAT_EAN_8: return "EAN-8";
            case Barcode.FORMAT_UPC_A: return "UPC-A";
            case Barcode.FORMAT_PDF417: return "PDF417";
            case Barcode.FORMAT_DATA_MATRIX: return "Data Matrix";
            case Barcode.FORMAT_AZTEC: return "Aztec";
            default: return "Unknown";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        binding = null;
    }
}
