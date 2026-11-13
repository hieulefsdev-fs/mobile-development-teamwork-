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
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class BaiTapFormActivity extends BaiTapActivity {
    private EditText ten;
    private EditText moTa;
    private Button mon;
    private EditText ngayNop;
    private EditText thangNop;
    private EditText namNop;
    private EditText gioNop;
    private EditText phutNop;
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
        ngayNop = findViewById(R.id.han_ngay);
        thangNop = findViewById(R.id.han_thang);
        namNop = findViewById(R.id.han_nam);
        gioNop = findViewById(R.id.han_gio);
        phutNop = findViewById(R.id.han_phut);
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
        LinearLayout danhSach = findViewById(R.id.danh_sach_mon);
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
            String tenMon = tenMonHoc.get(i);
            LinearLayout dong = new LinearLayout(this);
            dong.setGravity(android.view.Gravity.CENTER_VERTICAL);
            RadioButton luaChon = new RadioButton(this);
            luaChon.setText(tenMon);
            dong.addView(luaChon, new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            Button xoa = new Button(this, null, androidx.appcompat.R.attr.borderlessButtonStyle);
            xoa.setText("×");
            xoa.setContentDescription("Xóa môn " + tenMon);
            int kichThuoc = Math.round(48 * getResources().getDisplayMetrics().density);
            dong.addView(xoa, new LinearLayout.LayoutParams(kichThuoc, kichThuoc));
            xoa.setOnClickListener(view -> hoiXoaMon(ma, tenMon));
            danhSach.addView(dong);
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

    private void hoiXoaMon(int ma, String tenMon) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa môn học?")
                .setMessage("Xóa môn “" + tenMon + "” và tất cả bài tập thuộc môn này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> xoaMon(ma))
                .show();
    }

    private void xoaMon(int ma) {
        try {
            int soDong = db.xoaMonHoc(ma);
            thongBao(soDong > 0 ? "Đã xóa môn học" : "Môn học không còn tồn tại");
            if (maBaiTap != 0) {
                try (Cursor cursor = db.layChiTietBaiTap(maBaiTap)) {
                    if (!cursor.moveToFirst()) {
                        finish();
                        return;
                    }
                }
            }
            taiMonHoc();
            if (maMonDaChon == ma) maMonDaChon = 0;
            hienThiMon();
            findViewById(R.id.danh_sach_mon).setVisibility(View.GONE);
            if (!maMon.isEmpty()) chonMonHoc();
        } catch (SQLiteException e) {
            baoLoi("Không xóa được môn học", e);
        }
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
        if (khungLich.getVisibility() == View.VISIBLE) return;
        Calendar homNay = Calendar.getInstance();
        ngayNop.setText(hanNop == null ? String.valueOf(homNay.get(Calendar.DAY_OF_MONTH))
                : hanNop.substring(8, 10));
        thangNop.setText(hanNop == null ? String.valueOf(homNay.get(Calendar.MONTH) + 1)
                : hanNop.substring(5, 7));
        namNop.setText(hanNop == null ? String.valueOf(homNay.get(Calendar.YEAR))
                : hanNop.substring(0, 4));
        gioNop.setText(hanNop == null ? "23" : hanNop.substring(11, 13));
        phutNop.setText(hanNop == null ? "59" : hanNop.substring(14, 16));
        khungLich.setVisibility(View.VISIBLE);
        khungLich.post(() -> khungLich.requestRectangleOnScreen(
                new android.graphics.Rect(0, 0, khungLich.getWidth(), khungLich.getHeight())));
    }

    private void xacNhanNgay() {
        try {
            int ngay = Integer.parseInt(ngayNop.getText().toString());
            int thang = Integer.parseInt(thangNop.getText().toString());
            int nam = Integer.parseInt(namNop.getText().toString());
            int gio = Integer.parseInt(gioNop.getText().toString());
            int phut = Integer.parseInt(phutNop.getText().toString());
            if (nam < 1 || nam > 9999) {
                namNop.setError("Nhập năm từ 1 đến 9999");
                return;
            }
            Calendar moc = new GregorianCalendar();
            moc.clear();
            moc.setLenient(false);
            moc.set(nam, thang - 1, ngay, gio, phut);
            moc.getTimeInMillis();
            hanNop = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d",
                    nam, thang, ngay, gio, phut);
        } catch (IllegalArgumentException e) {
            hienThiLoi("Nhập ngày hợp lệ, giờ từ 0 đến 23 và phút từ 0 đến 59.");
            return;
        }

        getSystemService(InputMethodManager.class)
                .hideSoftInputFromWindow(khungLich.getWindowToken(), 0);
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

