package com.example.nhom3_quanlybaitapvahannopchosinhvien;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

abstract class BaiTapActivity extends AppCompatActivity {
    static final String EXTRA_MA_BAI_TAP = "ma_bai_tap";
    DatabaseHelper db;

    boolean moManHinh(int layout) {
        EdgeToEdge.enable(this);
        setContentView(layout);
        android.view.View troVe = findViewById(R.id.tro_ve);
        if (troVe != null) {
            troVe.setOnClickListener(view -> finish());
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            int bars = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
            var padding = insets.getInsets(bars);
            var banPhim = insets.getInsets(WindowInsetsCompat.Type.ime());
            int le = (int) (16 * getResources().getDisplayMetrics().density);
            view.setPadding(padding.left + le, padding.top + le, padding.right + le,
                    Math.max(padding.bottom, banPhim.bottom) + le);
            return insets;
        });
        try {
            db = new DatabaseHelper(getApplicationContext());
            db.getWritableDatabase();
            return true;
        } catch (IOException | SQLiteException | IllegalStateException e) {
            baoLoi("Không mở được cơ sở dữ liệu", e);
            finish();
            return false;
        }
    }

    ArrayList<Integer> napMon(Spinner spinner, boolean coTatCa) {
        ArrayList<Integer> maMon = new ArrayList<>();
        ArrayList<String> tenMon = new ArrayList<>();
        maMon.add(null);
        tenMon.add(coTatCa ? "Tất cả môn học" : "Chọn môn học");
        try (Cursor cursor = db.layMonHoc()) {
            while (cursor.moveToNext()) {
                maMon.add(cursor.getInt(cursor.getColumnIndexOrThrow("ma_mon_hoc")));
                tenMon.add(doc(cursor, "ten_mon_hoc"));
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tenMon);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return maMon;
    }

    static String doc(Cursor cursor, String cot) {
        return cursor.getString(cursor.getColumnIndexOrThrow(cot));
    }

    static String docHanNop(Cursor cursor) {
        return doc(cursor, "han_nop") + " " + doc(cursor, "gio_nop");
    }

    static String hienThiNgay(String ngay) {
        return ngay.substring(8, 10) + "/" + ngay.substring(5, 7) + "/" + ngay.substring(0, 4) + " · " + ngay.substring(11);
    }

    static boolean quaHan(String hanNop) {
        String homNay = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        return hanNop.compareTo(homNay) < 0;
    }

    void thongBao(String noiDung) {
        Toast.makeText(this, noiDung, Toast.LENGTH_SHORT).show();
    }

    void baoLoi(String noiDung, Exception loi) {
        android.util.Log.e("QuanLyBaiTap", noiDung, loi);
        thongBao(noiDung);
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }
}

