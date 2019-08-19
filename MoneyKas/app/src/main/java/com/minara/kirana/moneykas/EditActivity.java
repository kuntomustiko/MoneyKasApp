package com.minara.kirana.moneykas;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.minara.kirana.moneykas.helper.Config;
import com.minara.kirana.moneykas.helper.CurrentDate;
import com.minara.kirana.moneykas.helper.SqliteHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class EditActivity extends AppCompatActivity {
    RadioGroup radio_status;
    RadioButton radio_masuk, radio_keluar;

    EditText edt_jumlah, edt_keterangan, edt_tanggal;
    Button btn_simpan;
    RippleView rip_simpan;

    SqliteHelper sqliteHelper;
    Cursor cursor;

    String status, tanggal;
    DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit");

        status = "";
        tanggal = "";

        radio_status = findViewById(R.id.radio_status);
        radio_masuk = findViewById(R.id.radio_masuk);
        radio_keluar = findViewById(R.id.radio_keluar);

        edt_jumlah = findViewById(R.id.edit_jumlah);
        edt_keterangan = findViewById(R.id.edit_keterangan);
        edt_tanggal = findViewById(R.id.edit_tanggal);
        btn_simpan = findViewById(R.id.btn_simpan);
        rip_simpan = findViewById(R.id.rip_simpan);

        editMysql();
    }

    private void editMysql() {
        status = MainActivity.status;
        switch (status) {
            case "MASUK":
                radio_masuk.setChecked(true);
                break;
            case "KELUAR":
                radio_keluar.setChecked(true);
                break;
        }

        radio_status.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_masuk:
                        status = "MASUK";
                        break;
                    case R.id.radio_keluar:
                        status = "KELUAR";
                        break;
                }

                Log.d("Log status", status);
            }
        });

        edt_jumlah.setText(MainActivity.jumlah);
        edt_keterangan.setText(MainActivity.keterangan);

        tanggal = MainActivity.tanggal2;
        edt_tanggal.setText(MainActivity.tanggal);

        edt_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month_of_year, int day_of_month) {
                        // set day of month , month and year value in the edit text
                        NumberFormat numberFormat = new DecimalFormat("00");
                        tanggal = year + "-" + numberFormat.format((month_of_year + 1)) + "-" +
                                numberFormat.format(day_of_month);
                        edt_tanggal.setText(numberFormat.format(day_of_month) + "/" + numberFormat.format((month_of_year + 1)) +
                                "/" + year);
                    }
                }, CurrentDate.year, CurrentDate.month, CurrentDate.day);
                datePickerDialog.show();
            }
        });

        rip_simpan.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (status.equals("") || edt_jumlah.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Isi data dengan benar",
                            Toast.LENGTH_LONG).show();
                } else {
                    AndroidNetworking.post(Config.HOST + "update.php")
                            .addBodyParameter("transaksi_id", MainActivity.transaksi_id)
                            .addBodyParameter("status", status)
                            .addBodyParameter("jumlah", edt_jumlah.getText().toString())
                            .addBodyParameter("keterangan", edt_keterangan.getText().toString())
                            .addBodyParameter("tanggal", tanggal)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // do anything with response
                                    try {

                                        if (response.getString("response").equals("success")) {
                                            Toast.makeText(getApplicationContext(), "Perubahan berhasil disimpan", Toast.LENGTH_LONG).show();
                                            finish();
                                        } else {
                                            Toast.makeText(EditActivity.this, response.getString("response"), Toast.LENGTH_SHORT).show();

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError error) {
                                    // handle error
                                }
                            });

                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
