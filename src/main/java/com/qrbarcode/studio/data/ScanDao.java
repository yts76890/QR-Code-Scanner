package com.qrbarcode.studio.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ScanRecord record);

    @Delete
    void delete(ScanRecord record);

    @Query("DELETE FROM scan_records WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM scan_records")
    void deleteAll();

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    LiveData<List<ScanRecord>> getAllRecords();

    @Query("SELECT * FROM scan_records WHERE type = :type ORDER BY timestamp DESC")
    LiveData<List<ScanRecord>> getRecordsByType(String type);

    @Query("SELECT * FROM scan_records WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<ScanRecord>> searchRecords(String query);

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    List<ScanRecord> getAllRecordsSync();
}
