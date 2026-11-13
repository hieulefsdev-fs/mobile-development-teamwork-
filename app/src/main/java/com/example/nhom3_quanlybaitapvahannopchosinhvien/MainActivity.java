package com.example.nhom3_quanlybaitapvahannopchosinhvien;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends BaiTapActivity {
    private final ArrayList<Long> maBaiTap = new ArrayList<>();
    static final String EXTRA_TRANG_THAI = "trang_thai_moi";
    private Integer monDangLoc;
    private RadioGroup cacMon;
    private ArrayAdapter<String> adapter;
    private Button boLoc;
    private TabLayout tabs;
    private final ActivityResultLauncher<Intent> themBaiTap = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                int monMoi = result.getData().getIntExtra("ma_mon_hoc", -1);
                if (monDangLoc != null && monDangLoc != monMoi) {
                    monDangLoc = null;
                }
                capNhatBoLoc();
                tabs.selectTab(tabs.getTabAt(0));
                taiDanhSach();
            });

    private final ActivityResultLauncher<Intent> xemChiTiet = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null || tabs == null) {
                    return;
                }
                int trangThai = result.getData().getIntExtra(EXTRA_TRANG_THAI, 0);
                tabs.selectTab(tabs.getTabAt(trangThai));
                taiDanhSach();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!moManHinh(R.layout.activity_main)) return;

        ListView danhSach = findViewById(R.id.danh_sach);
        danhSach.setEmptyView(findViewById(R.id.trong));
        adapter = new ArrayAdapter<>(this, R.layout.dong_bai_tap);
        danhSach.setAdapter(adapter);
        danhSach.setOnItemClickListener((parent, view, position, id) ->
                xemChiTiet.launch(new Intent(this, BaiTapDetailActivity.class)
                        .putExtra(EXTRA_MA_BAI_TAP, maBaiTap.get(position).longValue())));
        findViewById(R.id.them).setOnClickListener(view ->
                themBaiTap.launch(new Intent(this, BaiTapFormActivity.class)));

        boLoc = findViewById(R.id.mon_loc);
        tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("Cần làm"));
        tabs.addTab(tabs.newTab().setText("Đã hoàn thành"));
        cacMon = findViewById(R.id.cac_mon);
        boLoc.setOnClickListener(view -> findViewById(R.id.khung_loc_mon).setVisibility(
                findViewById(R.id.khung_loc_mon).getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mon_loc")) {
                monDangLoc = savedInstanceState.getInt("mon_loc");
            }
            tabs.selectTab(tabs.getTabAt(savedInstanceState.getInt("tab")));
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                taiDanhSach();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cacMon == null) return;
        capNhatBoLoc();
        taiDanhSach();
    }

    private void capNhatBoLoc() {
        cacMon.removeAllViews();
        themLuaChonMon(null, "Tất cả môn học");
        try (Cursor cursor = db.layMonHoc()) {
            while (cursor.moveToNext()) {
                int ma = cursor.getInt(cursor.getColumnIndexOrThrow("ma_mon_hoc"));
                themLuaChonMon(ma, doc(cursor, "ten_mon_hoc"));
            }
        } catch (SQLiteException e) {
            baoLoi("Không tải được môn học", e);
        }
    }

    private void themLuaChonMon(Integer ma, String ten) {
        RadioButton luaChon = new RadioButton(this);
        luaChon.setId(View.generateViewId());
        luaChon.setText(ten);
        cacMon.addView(luaChon);
        if (java.util.Objects.equals(ma, monDangLoc)) {
            cacMon.check(luaChon.getId());
            boLoc.setText(ten);
        }
        luaChon.setOnClickListener(view -> {
            monDangLoc = ma;
            boLoc.setText(ten);
            findViewById(R.id.khung_loc_mon).setVisibility(View.GONE);
            taiDanhSach();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tabs != null) outState.putInt("tab", tabs.getSelectedTabPosition());
        if (monDangLoc != null) outState.putInt("mon_loc", monDangLoc);
    }

    private void taiDanhSach() {
        if (cacMon == null) return;
        int trangThai = tabs.getSelectedTabPosition();
        ArrayList<Long> maMoi = new ArrayList<>();
        ArrayList<String> dongMoi = new ArrayList<>();
        try (Cursor cursor = db.layDanhSachBaiTap(trangThai, monDangLoc)) {
            while (cursor.moveToNext()) {
                String han = docHanNop(cursor);
                String nhan = trangThai == 0 && quaHan(han) ? " • Quá hạn" : "";
                maMoi.add(cursor.getLong(cursor.getColumnIndexOrThrow("ma_bai_tap")));
                dongMoi.add(doc(cursor, "ten_bai_tap") + "\n"
                        + doc(cursor, "ten_mon_hoc") + " · " + hienThiNgay(han) + nhan);
            }
        } catch (SQLiteException e) {
            baoLoi("Không tải được danh sách bài tập", e);
            return;
        }
        maBaiTap.clear();
        maBaiTap.addAll(maMoi);
        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(dongMoi);
        adapter.notifyDataSetChanged();
    }
}

