package com.minara.kirana.moneykas;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
import com.minara.kirana.moneykas.helper.Config;
import com.minara.kirana.moneykas.helper.SqliteHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tv_masuk, tv_keluar, tv_total;
    ListView list_kas;
    SwipeRefreshLayout swipe_refresh;
    ArrayList<HashMap<String, String>> aruskas = new ArrayList<>();

    public static TextView tv_filter;
    public static String transaksi_id, tgl_dari, tgl_ke;
    public static boolean filter;

    String query_kas, query_total;
    SqliteHelper sqliteHelper;
    Cursor cursor;
    public static String link, status, keterangan, jumlah, tanggal, tanggal2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        transaksi_id = "";
        tgl_dari= "";
        tgl_ke = "";
        query_kas = "";
        query_total = "";
        filter = false;
        sqliteHelper = new SqliteHelper(this);

        tv_filter = findViewById(R.id.text_filter);
        tv_masuk = findViewById(R.id.text_masuk);
        tv_keluar = findViewById(R.id.text_keluar);
        tv_total = findViewById(R.id.text_total);
        list_kas = findViewById(R.id.list_kas);
        swipe_refresh = findViewById(R.id.swipe_refresh);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                query_kas =
                        "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi ORDER BY transaksi_id DESC";
                query_total =
                        "SELECT SUM(jumlah) AS total, " +
                                "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK') AS masuk, " +
                                "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR') AS keluar " +
                                "FROM transaksi";

                link = Config.HOST + "read.php";
                readMysql();
                tv_filter.setText("SEMUA");
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddActivity.class));
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();

        query_kas =
                "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi ORDER BY transaksi_id DESC";
        query_total =
                "SELECT SUM(jumlah) AS total, " +
                        "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK') AS masuk, " +
                        "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR') AS keluar " +
                        "FROM transaksi";

        if (filter) {

            query_kas =
                    "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi  " +
                            "WHERE (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "') ORDER BY transaksi_id ASC ";
            query_total =
                    "SELECT SUM(jumlah) AS total, " +
                            "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK' AND (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "') ), " +
                            "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR' AND (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "')) " +
                            "FROM transaksi " +
                            "WHERE (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "') ";

            link = Config.HOST + "filter.php?dari=" + tgl_dari + "&ke=" + tgl_ke;
        } else {
            link = Config.HOST + "read.php";
        }

        readMysql();
    }

    private void readMysql(){
        swipe_refresh.setRefreshing(false);
        aruskas.clear();
        list_kas.setAdapter(null);
        Log.d("Linknya", link);

        AndroidNetworking.post(link)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

                            tv_masuk.setText("Rp." + rupiahFormat.format(response.getDouble("masuk")));
                            tv_keluar.setText("Rp." + rupiahFormat.format(response.getDouble("keluar")));
                            tv_total.setText(
                                    "Rp. " + rupiahFormat.format(response.getDouble("masuk") - response.getDouble("keluar"))
                            );

                            JSONArray jsonArray = response.getJSONArray("hasil");
                            for (int i =0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = ((JSONArray) jsonArray).getJSONObject(i);


                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("transaksi_id", jsonObject.getString("transaksi_id"));
                                map.put("status", jsonObject.getString("status"));
                                map.put("jumlah", jsonObject.getString("jumlah"));
                                map.put("keterangan", jsonObject.getString("keterangan"));
                                map.put("tanggal", jsonObject.getString("tanggal"));
                                map.put("tanggal2", jsonObject.getString("tanggal2"));
                                aruskas.add(map);
                            }
                            readAdapter();
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void readAdapter() {
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, aruskas, R.layout.list_content_main,
                new String[]{"transaksi_id", "status", "jumlah", "keterangan", "tanggal", "tanggal2"},
                new int[]{
                        R.id.text_transaksi_id, R.id.text_status, R.id.text_jumlah, R.id.text_keterangan,
                        R.id.text_tanggal, R.id.text_tanggal2
                });

        list_kas.setAdapter(simpleAdapter);
        list_kas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                transaksi_id = ((TextView) view.findViewById(R.id.text_transaksi_id)).getText().toString();
                status = ((TextView) view.findViewById(R.id.text_status)).getText().toString();
                jumlah = ((TextView) view.findViewById(R.id.text_jumlah)).getText().toString();
                keterangan = ((TextView) view.findViewById(R.id.text_keterangan)).getText().toString();
                tanggal = ((TextView) view.findViewById(R.id.text_tanggal)).getText().toString();
                tanggal2 = ((TextView) view.findViewById(R.id.text_tanggal2)).getText().toString();
                ListMenu();
            }
        });
    }

    private void ListMenu() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.list_menu);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        TextView text_edit = dialog.findViewById(R.id.text_edit);
        TextView text_hapus = dialog.findViewById(R.id.text_hapus);
        dialog.show();

        text_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        text_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                Hapus();
                hapusMysqli();
            }
        });
    }


    private void hapusMysqli(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Yakin untuk mengahapus transaksi ini?");
        builder.setPositiveButton(
                "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AndroidNetworking.post(Config.HOST+"delete.php" )
                                .addBodyParameter("transaksi_id", transaksi_id)
                                .setPriority(Priority.MEDIUM)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {

                                            if (response.getString("response").equals("success")) {
                                                Toast.makeText(getApplicationContext(), "Tranksaki berhasil dihapus",
                                                        Toast.LENGTH_LONG).show();

//                                                KasAdapter();
                                                readMysql();
                                            } else {
                                                Toast.makeText(MainActivity.this, response.getString("response"), Toast.LENGTH_SHORT).show();

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(ANError anError) {

                                    }
                                });
                    }
                }
        );

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    private void filterMysql(){
        SmoothDateRangePickerFragment smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(
                new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                    @Override
                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                               int yearStart,
                                               int monthStart,
                                               int dayStart,
                                               int yearEnd,
                                               int monthEnd,
                                               int dayEnd) {
                        MainActivity.tgl_dari = String.valueOf(yearStart) + "-" + String.valueOf(monthStart + 1) + "-" +
                                String.valueOf(dayStart);

                        MainActivity.tgl_ke = String.valueOf(yearEnd) + "-" + String.valueOf(monthEnd + 1) + "-" +
                                String.valueOf(dayEnd);

                        Log.d("_dari", tgl_dari);
                        Log.d("_ke", tgl_ke);

                        tv_filter.setText(
                                String.valueOf(dayStart) + "/" + String.valueOf(monthStart + 1) + "/" +
                                        String.valueOf(yearStart) +
                                        "-" +
                                        String.valueOf(dayEnd) + "/" + String.valueOf(monthEnd + 1) + "/" +
                                        String.valueOf(yearEnd)
                        );

                        link = Config.HOST + "filter.php?dari=" + tgl_dari + "&ke=" + tgl_ke;
                        readMysql();
                    }
                }
        );

        smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
