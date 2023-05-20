package com.vitargo.kpi_journal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Snackbar.make(findViewById(android.R.id.content), "Permission needed!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", v -> {

                            try {
                                Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                                startActivity(intent);
                            } catch (Exception ex) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean savedResult = sharedPref.getBoolean(getString(R.string.saved_high_result), false);
        Log.d("Check bpx saved Result", String.valueOf(savedResult));
        if (!savedResult) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View view = inflater.inflate(R.layout.pop_up_check_box, null);
            builder.setView(view)
                    .setPositiveButton(R.string.button_ok, (dialog, id) -> {
                        CheckBox checkBox = view.findViewById(R.id.checkbox_not_show);
                        boolean result = checkBox.isChecked();
                        SharedPreferences sharedPref1 = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref1.edit();
                        editor.putBoolean(getString(R.string.saved_high_result), result);
                        editor.apply();
                        dialog.cancel();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }


        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("KPI-Journal", "No SDCARD");
        } else {
            File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "special_dir");
            directory.mkdirs();
            Log.d("DIR Creation", "Special Directory is created!");
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Downloading file...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        ArticleListTask articleListTask = new ArticleListTask(this, mProgressDialog);
        articleListTask.execute("http://chemengine.kpi.ua/");

        Log.d("Check result", String.valueOf(sharedPref.getBoolean(getString(R.string.saved_high_result), false)));
    }

}