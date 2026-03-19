package com.qrbarcode.studio.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_records")
public class ScanRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "format")
    public String format;

    @ColumnInfo(name = "type")
    public String type; // "SCANNED" or "GENERATED"

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "image_path")
    public String imagePath;

    public ScanRecord(String content, String format, String type, long timestamp, String imagePath) {
        this.content = content;
        this.format = format;
        this.type = type;
        this.timestamp = timestamp;
        this.imagePath = imagePath;
    }
}
