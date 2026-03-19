package com.qrbarcode.studio;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qrbarcode.studio.databinding.ActivityMainBinding;
import com.qrbarcode.studio.ui.barcodegenerator.BarcodeGeneratorFragment;
import com.qrbarcode.studio.ui.history.HistoryFragment;
import com.qrbarcode.studio.ui.qrgenerator.QrGeneratorFragment;
import com.qrbarcode.studio.ui.scanner.ScannerFragment;
import com.qrbarcode.studio.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new ScannerFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_scanner) {
                fragment = new ScannerFragment();
            } else if (id == R.id.nav_qr_generator) {
                fragment = new QrGeneratorFragment();
            } else if (id == R.id.nav_barcode_generator) {
                fragment = new BarcodeGeneratorFragment();
            } else if (id == R.id.nav_history) {
                fragment = new HistoryFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
