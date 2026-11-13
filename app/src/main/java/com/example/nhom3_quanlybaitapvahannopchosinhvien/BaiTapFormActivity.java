package com.example.nhom3_quanlybaitapvahannopchosinhvien;

import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.view.inputmethod.InputMethodManager;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class BaiTapFormActivity extends BaiTapActivity {
    private EditText ten;
    private EditText moTa;
    private Button mon;
    private DatePicker lich;
    private TimePicker gio;
    private boolean doiTenMon;
    private View khungLich;
    private final ArrayList<String> tenMonHoc = new ArrayList<>();
    private int maMonDaChon;
    private Button chonNgay;
    private TextView loiBieuMau;
    private ArrayList<Integer> maMon;
    private long maBaiTap;
    private String hanNop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!moManHinh(R.layout.activity_bai_tap_form)) return;

        maBaiTap = getIntent().getLongExtra(EXTRA_MA_BAI_TAP, 0);
        TextView title = findViewById(R.id.title);
        title.setText(maBaiTap == 0 ? "Thêm bài tập" : "Chỉnh sửa bài tập");
        ten = findViewById(R.id.ten);
        moTa = findViewById(R.id.mo_ta);
        mon = findViewById(R.id.mon);
        lich = findViewById(R.id.lich_han_nop);
        gio = findViewById(R.id.gio_han_nop);
        gio.setIs24HourView(true);
        khungLich = findViewById(R.id.khung_lich);
        chonNgay = findViewById(R.id.chon_ngay);
        loiBieuMau = findViewById(R.id.loi_bieu_mau);

        try {
            taiMonHoc();
            if (savedInstanceState != null) {
                hanNop = savedInstanceState.getString("han_nop");
                maMonDaChon = savedInstanceState.getInt("ma_mon_hoc");
            } else if (maBaiTap != 0) {
                if (!docBaiTap()) {
                    thongBao("Bài tập không còn tồn tại");
                    finish();
                    return;
                }
            }
        } catch (SQLiteException e) {
            baoLoi("Không tải được biểu mẫu", e);
            finish();
            return;
        }

        hienThiHanNop();
        if (savedInstanceState != null && savedInstanceState.getBoolean("dang_chon_han")) {
            khungLich.setVisibility(View.VISIBLE);
        }
        hienThiMon();
        if (savedInstanceState != null && savedInstanceState.getBoolean("dang_nhap_mon")) {
            moOThemMon(savedInstanceState.getBoolean("doi_ten_mon")
                    && maMon.contains(maMonDaChon));
        }
        mon.setOnClickListener(view -> chonMonHoc());
        chonNgay.setOnClickListener(view -> moLich());
        findViewById(R.id.xac_nhan_ngay).setOnClickListener(view -> xacNhanNgay());
        findViewById(R.id.them_mon).setOnClickListener(view -> moOThemMon(false));
        findViewById(R.id.doi_ten_mon).setOnClickListener(view -> {
            if (maMon.contains(maMonDaChon)) {
                moOThemMon(true);
            } else {
                hienThiLoi("Chọn môn học cần đổi tên.");
            }
        });
        findViewById(R.id.luu_mon_moi).setOnClickListener(view -> luuMonHoc());
        findViewById(R.id.huy_them_mon).setOnClickListener(view ->
                findViewById(R.id.khung_them_mon).setVisibility(View.GONE));
        findViewById(R.id.luu).setOnClickListener(view -> luu());
    }

    private boolean docBaiTap() {
        try (Cursor cursor = db.layChiTietBaiTap(maBaiTap)) {
            if (!cursor.moveToFirst()) return false;
            ten.setText(doc(cursor, "ten_bai_tap"));
            moTa.setText(doc(cursor, "mo_ta"));
            int monHienTai = cursor.getInt(cursor.getColumnIndexOrThrow("ma_mon_hoc"));
            maMonDaChon = monHienTai;
            hanNop = docHanNop(cursor);
            return true;
        }
    }

    private void taiMonHoc() {
        maMon = new ArrayList<>();
        tenMonHoc.clear();
        try (Cursor cursor = db.layMonHoc()) {
            while (cursor.moveToNext()) {
                maMon.add(cursor.getInt(cursor.getColumnIndexOrThrow("ma_mon_hoc")));
                tenMonHoc.add(doc(cursor, "ten_mon_hoc"));
            }
        }
    }

    private void hienThiMon() {
        int viTri = maMon.indexOf(maMonDaChon);
        mon.setText(viTri < 0 ? "Chọn môn học" : tenMonHoc.get(viTri));
    }

    private void chonMonHoc() {
        android.widget.RadioGroup danhSach = findViewById(R.id.danh_sach_mon);
        if (danhSach.getVisibility() == View.VISIBLE) {
            danhSach.setVisibility(View.GONE);
            return;
        }
        if (maMon.isEmpty()) {
            moOThemMon(false);
            return;
        }

        findViewById(R.id.khung_them_mon).setVisibility(View.GONE);
        danhSach.removeAllViews();
        for (int i = 0; i < maMon.size(); i++) {
            int ma = maMon.get(i);
            android.widget.RadioButton luaChon = new android.widget.RadioButton(this);
            luaChon.setId(View.generateViewId());
            luaChon.setText(tenMonHoc.get(i));
            danhSach.addView(luaChon);
            luaChon.setChecked(ma == maMonDaChon);
            luaChon.setOnClickListener(view -> {
                maMonDaChon = ma;
                hienThiMon();
                danhSach.setVisibility(View.GONE);
                loiBieuMau.setVisibility(View.GONE);
            });
        }
        danhSach.setVisibility(View.VISIBLE);
    }

    private void moOThemMon(boolean suaTen) {
        doiTenMon = suaTen;
        EditText tenMon = findViewById(R.id.ten_mon_moi);
        tenMon.setText(suaTen ? tenMonHoc.get(maMon.indexOf(maMonDaChon)) : "");
        tenMon.setError(null);
        Button luuMon = findViewById(R.id.luu_mon_moi);
        luuMon.setText(suaTen ? "Lưu tên mới" : "Lưu môn học");
        findViewById(R.id.danh_sach_mon).setVisibility(View.GONE);
        findViewById(R.id.khung_them_mon).setVisibility(View.VISIBLE);
        findViewById(R.id.ten_mon_moi).requestFocus();
    }

    private void luuMonHoc() {
        EditText tenMon = findViewById(R.id.ten_mon_moi);
        String tenMoi = tenMon.getText().toString().trim();
        if (tenMoi.isEmpty()) {
            tenMon.setError("Nhập tên môn học");
            return;
        }
        try {
            int maMoi = maMonDaChon;
            if (doiTenMon) {
                if (db.doiTenMonHoc(maMoi, tenMoi) == 0) {
                    tenMon.setError("Môn học không còn tồn tại");
                    return;
                }
            } else {
                maMoi = db.themMonHoc(tenMoi);
            }
            taiMonHoc();
            maMonDaChon = maMoi;
            hienThiMon();
            getSystemService(InputMethodManager.class)
                    .hideSoftInputFromWindow(tenMon.getWindowToken(), 0);
            tenMon.setText("");
            findViewById(R.id.khung_them_mon).setVisibility(View.GONE);
            loiBieuMau.setVisibility(View.GONE);
        } catch (SQLiteConstraintException e) {
            tenMon.setError("Tên môn học đã tồn tại");
        } catch (SQLiteException e) {
            baoLoi("Không thêm được môn học", e);
            tenMon.setError("Không lưu được môn học. Hãy thử lại.");
        }
    }

    private void moLich() {
        View dangNhap = getCurrentFocus();
        if (dangNhap != null) {
            getSystemService(InputMethodManager.class)
                    .hideSoftInputFromWindow(dangNhap.getWindowToken(), 0);
        }
        Calendar ngay = Calendar.getInstance();
        if (hanNop != null) {
            ngay.set(Integer.parseInt(hanNop.substring(0, 4)),
                    Integer.parseInt(hanNop.substring(5, 7)) - 1,
                    Integer.parseInt(hanNop.substring(8, 10)));
        }
        lich.updateDate(ngay.get(Calendar.YEAR), ngay.get(Calendar.MONTH),
                ngay.get(Calendar.DAY_OF_MONTH));
        gio.setHour(hanNop == null ? 23 : Integer.parseInt(hanNop.substring(11, 13)));
        gio.setMinute(hanNop == null ? 59 : Integer.parseInt(hanNop.substring(14, 16)));
        khungLich.setVisibility(View.VISIBLE);
        khungLich.post(() -> khungLich.requestRectangleOnScreen(
                new android.graphics.Rect(0, 0, khungLich.getWidth(), khungLich.getHeight())));
    }

    private void xacNhanNgay() {
        lich.clearFocus();
        gio.clearFocus();
        getSystemService(InputMethodManager.class)
                .hideSoftInputFromWindow(gio.getWindowToken(), 0);
        hanNop = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d",
                lich.getYear(), lich.getMonth() + 1, lich.getDayOfMonth(),
                gio.getHour(), gio.getMinute());
        khungLich.setVisibility(View.GONE);
        loiBieuMau.setVisibility(View.GONE);
        hienThiHanNop();
    }

    private void hienThiHanNop() {
        chonNgay.setText(hanNop == null ? "Chọn hạn nộp" : "Hạn nộp · " + hienThiNgay(hanNop));
    }

    private void luu() {
        loiBieuMau.setVisibility(View.GONE);
        String tenBai = ten.getText().toString().trim();
        if (tenBai.isEmpty()) {
            ten.setError("Nhập tên bài tập");
            ten.requestFocus();
            return;
        }
        if (!maMon.contains(maMonDaChon)) {
            hienThiLoi("Hãy chọn hoặc thêm môn học.");
            return;
        }
        if (hanNop == null || khungLich.getVisibility() == View.VISIBLE) {
            hienThiLoi("Hãy chọn ngày, giờ rồi bấm Xác nhận ngày và giờ.");
            return;
        }

        String moTaBai = moTa.getText().toString().trim();
        try {
            if (maBaiTap == 0) {
                db.themBaiTap(maMonDaChon, tenBai, moTaBai, hanNop);
                setResult(RESULT_OK, new Intent().putExtra("ma_mon_hoc", maMonDaChon));
            } else if (db.suaBaiTap(maBaiTap, maMonDaChon, tenBai, moTaBai, hanNop) == 0) {
                thongBao("Bài tập không còn tồn tại");
                finish();
                return;
            }
            thongBao("Đã lưu bài tập");
            finish();
        } catch (SQLiteException e) {
            baoLoi("Không lưu được bài tập", e);
            hienThiLoi("Không lưu được bài tập. Kiểm tra môn học và hạn nộp rồi thử lại.");
        }
    }

    private void hienThiLoi(String noiDung) {
        loiBieuMau.setText(noiDung);
        loiBieuMau.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("han_nop", hanNop);
        outState.putInt("ma_mon_hoc", maMonDaChon);
        outState.putBoolean("doi_ten_mon", doiTenMon);
        outState.putBoolean("dang_nhap_mon",
                findViewById(R.id.khung_them_mon).getVisibility() == View.VISIBLE);
        outState.putBoolean("dang_chon_han", khungLich != null
                && khungLich.getVisibility() == View.VISIBLE);
    }
}

