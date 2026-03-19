package com.qrbarcode.studio.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qrbarcode.studio.data.ScanRecord;
import com.qrbarcode.studio.repository.ScanRepository;

import java.util.List;

public class ScanViewModel extends AndroidViewModel {

    private final ScanRepository repository;
    private final MutableLiveData<String> filterType = new MutableLiveData<>("ALL");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private LiveData<List<ScanRecord>> records;

    public ScanViewModel(@NonNull Application application) {
        super(application);
        repository = new ScanRepository(application);
        records = repository.getAllRecords();
    }

    public LiveData<List<ScanRecord>> getRecords() {
        return records;
    }

    public void setFilter(String type) {
        filterType.setValue(type);
        if ("ALL".equals(type)) {
            records = repository.getAllRecords();
        } else {
            records = repository.getRecordsByType(type);
        }
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            setFilter(filterType.getValue() != null ? filterType.getValue() : "ALL");
        } else {
            records = repository.searchRecords(query.trim());
        }
    }

    public void insert(ScanRecord record) {
        repository.insert(record);
    }

    public void delete(ScanRecord record) {
        repository.delete(record);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
