package com.example.nhom3_quanlybaitapvahannopchosinhvien;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class BaiTapDetailActivity extends BaiTapActivity {
    private long maBaiTap;

    private boolean daHoanThanh;
    private TextView title;
    private TextView thongTin;
    private Button doiTrangThai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!moManHinh(R.layout.activity_bai_tap_detail)) return;

        maBaiTap = getIntent().getLongExtra(EXTRA_MA_BAI_TAP, 0);
        title = findViewById(R.id.title);
        thongTin = findViewById(R.id.thong_tin);
        doiTrangThai = findViewById(R.id.doi_trang_thai);
        doiTrangThai.setOnClickListener(view -> doiTrangThai());
        findViewById(R.id.sua).setOnClickListener(view ->
                startActivity(new Intent(this, BaiTapFormActivity.class)
                        .putExtra(EXTRA_MA_BAI_TAP, maBaiTap)));
        findViewById(R.id.xoa).setOnClickListener(view ->
                new AlertDialog.Builder(this)
                        .setTitle("Xóa bài tập?")
                        .setMessage("Bạn có muốn xóa bài “" + title.getText() + "”?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (dialog, which) -> xacNhanXoa())
                        .show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (title != null) taiChiTiet();
    }

    private void taiChiTiet() {
        try (Cursor cursor = db.layChiTietBaiTap(maBaiTap)) {
            if (!cursor.moveToFirst()) {
                thongBao("Bài tập không còn tồn tại");
                finish();
                return;
            }
            String tenBai = doc(cursor, "ten_bai_tap");
            String han = docHanNop(cursor);
            daHoanThanh = cursor.getInt(cursor.getColumnIndexOrThrow("trang_thai")) == 1;
            String trangThai = daHoanThanh ? "Đã hoàn thành" : "Cần làm";
            if (!daHoanThanh && quaHan(han)) trangThai += " · Quá hạn";
            String moTa = doc(cursor, "mo_ta");

            title.setText(tenBai);
            String noiDung = "Môn học · " + doc(cursor, "ten_mon_hoc")
                    + "\nHạn nộp · " + hienThiNgay(han)
                    + "\nTrạng thái · " + trangThai;
            if (moTa != null && !moTa.isEmpty()) noiDung += "\n\n" + moTa;
            thongTin.setText(noiDung);
            doiTrangThai.setText(daHoanThanh ? "Chuyển về Cần làm" : "Đánh dấu hoàn thành");
        } catch (SQLiteException e) {
            baoLoi("Không tải được bài tập", e);
            finish();
        }
    }

    private void doiTrangThai() {
        try {
            boolean hoanThanhMoi = !daHoanThanh;
            if (db.capNhatTrangThai(maBaiTap, hoanThanhMoi) > 0) {
                setResult(RESULT_OK, new Intent().putExtra(
                        MainActivity.EXTRA_TRANG_THAI, hoanThanhMoi ? 1 : 0));
            } else {
                thongBao("Bài tập không còn tồn tại");
            }
            finish();
        } catch (SQLiteException e) {
            baoLoi("Không cập nhật được trạng thái", e);
        }
    }

    private void xacNhanXoa() {
        try {
            int soDong = db.xoaBaiTap(maBaiTap);
            thongBao(soDong > 0 ? "Đã xóa bài tập" : "Bài tập không còn tồn tại");
            finish();
        } catch (SQLiteException e) {
            baoLoi("Không xóa được bài tập. Hãy thử lại.", e);
        }
    }
}