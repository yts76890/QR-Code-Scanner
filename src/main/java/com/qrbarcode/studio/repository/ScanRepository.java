package com.qrbarcode.studio.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.qrbarcode.studio.data.AppDatabase;
import com.qrbarcode.studio.data.ScanDao;
import com.qrbarcode.studio.data.ScanRecord;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanRepository {

    private final ScanDao scanDao;
    private final ExecutorService executor;

    public ScanRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        scanDao = db.scanDao();
        executor = Executors.newFixedThreadPool(4);
    }

    public void insert(ScanRecord record) {
        executor.execute(() -> scanDao.insert(record));
    }

    public void delete(ScanRecord record) {
        executor.execute(() -> scanDao.delete(record));
    }

    public void deleteById(int id) {
        executor.execute(() -> scanDao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(scanDao::deleteAll);
    }

    public LiveData<List<ScanRecord>> getAllRecords() {
        return scanDao.getAllRecords();
    }

    public LiveData<List<ScanRecord>> getRecordsByType(String type) {
        return scanDao.getRecordsByType(type);
    }

    public LiveData<List<ScanRecord>> searchRecords(String query) {
        return scanDao.searchRecords(query);
    }
}
