package com.example.nhom3_quanlybaitapvahannopchosinhvien;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TEN_TEP = "quan_ly_bai_tap.db";
    private static final int PHIEN_BAN = 2;

    public DatabaseHelper(Context context) throws IOException {
        super(context, TEN_TEP, null, PHIEN_BAN);
        saoChepDatabaseLanDau(context);
    }

    private static synchronized void saoChepDatabaseLanDau(Context context)
            throws IOException {
        File tepDich = context.getDatabasePath(TEN_TEP);
        if (tepDich.isFile()) {
            return;
        }

        File thuMuc = tepDich.getParentFile();
        if (thuMuc == null || (!thuMuc.isDirectory() && !thuMuc.mkdirs())) {
            throw new IOException("Không tạo được thư mục cơ sở dữ liệu");
        }

        File tepTam = File.createTempFile("baitap-", ".tmp", thuMuc);
        try {
            try (InputStream input = context.getAssets().open(TEN_TEP);
                 FileOutputStream output = new FileOutputStream(tepTam)) {
                byte[] boDem = new byte[8192];
                int soByte;
                while ((soByte = input.read(boDem)) != -1) {
                    output.write(boDem, 0, soByte);
                }
            }
            if (!tepTam.renameTo(tepDich)) {
                throw new IOException("Không sao chép được " + TEN_TEP);
            }
        } finally {
            tepTam.delete();
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        throw new IllegalStateException("Thiếu cơ sở dữ liệu SQLite đóng gói");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE BaiTap ADD COLUMN gio_nop TEXT NOT NULL DEFAULT '23:59' "
                    + "CHECK (gio_nop GLOB '[0-2][0-9]:[0-5][0-9]' AND gio_nop < '24:00')");
            return;
        }
        throw new IllegalStateException("Thiếu mã nâng cấp SQLite từ "
                + oldVersion + " lên " + newVersion);
    }

    public int themMonHoc(String tenMon) {
        ContentValues values = new ContentValues();
        values.put("ten_mon_hoc", tenMon);
        return Math.toIntExact(getWritableDatabase().insertOrThrow("MonHoc", null, values));
    }

    public int doiTenMonHoc(int maMon, String tenMoi) {
        ContentValues values = new ContentValues();
        values.put("ten_mon_hoc", tenMoi);
        return getWritableDatabase().update("MonHoc", values,
                "ma_mon_hoc = ?", new String[]{String.valueOf(maMon)});
    }

    public Cursor layMonHoc() {
        return getReadableDatabase().query("MonHoc", null, null, null,
                null, null, "ten_mon_hoc");
    }

    public Cursor layDanhSachBaiTap(int trangThai, Integer maMon) {
        String sql = "SELECT b.*, m.ten_mon_hoc FROM BaiTap b "
                + "JOIN MonHoc m ON b.ma_mon_hoc = m.ma_mon_hoc "
                + "WHERE b.trang_thai = ?";
        String[] args = {String.valueOf(trangThai)};
        if (maMon != null) {
            sql += " AND b.ma_mon_hoc = ?";
            args = new String[]{String.valueOf(trangThai), String.valueOf(maMon)};
        }
        return getReadableDatabase().rawQuery(
                sql + " ORDER BY b.han_nop, b.gio_nop, b.ma_bai_tap", args);
    }

    public Cursor layChiTietBaiTap(long maBaiTap) {
        return getReadableDatabase().rawQuery(
                "SELECT b.*, m.ten_mon_hoc FROM BaiTap b "
                        + "JOIN MonHoc m ON b.ma_mon_hoc = m.ma_mon_hoc "
                        + "WHERE b.ma_bai_tap = ?",
                new String[]{String.valueOf(maBaiTap)});
    }

    public long themBaiTap(int maMon, String ten, String moTa, String hanNop) {
        return getWritableDatabase().insertOrThrow("BaiTap", null,
                taoDuLieuBaiTap(maMon, ten, moTa, hanNop));
    }

    public int suaBaiTap(long maBaiTap, int maMon, String ten,
                        String moTa, String hanNop) {
        return getWritableDatabase().update("BaiTap",
                taoDuLieuBaiTap(maMon, ten, moTa, hanNop),
                "ma_bai_tap = ?", new String[]{String.valueOf(maBaiTap)});
    }

    public int capNhatTrangThai(long maBaiTap, boolean daHoanThanh) {
        ContentValues values = new ContentValues();
        values.put("trang_thai", daHoanThanh ? 1 : 0);
        return getWritableDatabase().update("BaiTap", values,
                "ma_bai_tap = ?", new String[]{String.valueOf(maBaiTap)});
    }

    public int xoaMonHoc(int maMon) {
        SQLiteDatabase database = getWritableDatabase();
        String[] args = {String.valueOf(maMon)};
        database.beginTransaction();
        try {
            database.delete("BaiTap", "ma_mon_hoc = ?", args);
            int soDong = database.delete("MonHoc", "ma_mon_hoc = ?", args);
            database.setTransactionSuccessful();
            return soDong;
        } finally {
            database.endTransaction();
        }
    }

    public int xoaBaiTap(long maBaiTap) {
        return getWritableDatabase().delete("BaiTap",
                "ma_bai_tap = ?", new String[]{String.valueOf(maBaiTap)});
    }

    private ContentValues taoDuLieuBaiTap(int maMon, String ten,
                                         String moTa, String hanNop) {
        ContentValues values = new ContentValues();
        values.put("ma_mon_hoc", maMon);
        values.put("ten_bai_tap", ten.trim());
        values.put("mo_ta", moTa);
        values.put("han_nop", hanNop.substring(0, 10));
        if (hanNop.length() > 10) {
            values.put("gio_nop", hanNop.substring(11));
        }
        return values;
    }
}

