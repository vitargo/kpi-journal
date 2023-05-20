package com.vitargo.kpi_journal;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.core.content.FileProvider;
import com.vitargo.kpi_journal.model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArticleListTask extends AsyncTask<String, Void, List<Article>> {

    private final MainActivity activity;
    private final ProgressDialog mProgressDialog;
    public int count;

    public ArticleListTask(MainActivity activity, ProgressDialog mProgressDialog) {
        this.mProgressDialog = mProgressDialog;
        this.activity = activity;
        this.count = 0;
    }

    @Override
    protected List<Article> doInBackground(String... urls) {
        List<Article> result = new ArrayList<>();
        Document doc;
        try {
            doc = Jsoup.connect(urls[0])
                    .timeout(20000)
                    .get();
            Elements elts = doc.getElementsByClass("obj_galley_link pdf");

            for (int i = 0; i < elts.size(); i++) {
                Article art = new Article();
                String url = elts.get(i).attr("abs:href");
                String newUrl = url.replace("view", "download");
                art.setUrl(newUrl);
                String artId = elts.get(i).attr("aria-labelledby");
                art.setCode(artId);
                art.setName(doc.getElementById(artId).text());
                result.add(art);
            }
            Article a = new Article();
            a.setName("Not working url");
            a.setUrl("http://chemengine.kpi.ua/");
            result.add(a);
        } catch (IOException e) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Internet Connection Lost! You can just see the history!", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(List<Article> articles) {
        TableLayout table = activity.findViewById(R.id.article_list);
        updateTable(table, articles);
        super.onPostExecute(articles);
    }

    private void updateTable(TableLayout table, List<Article> articles) {
        int count = 0;
        for (Article article : articles) {
            count++;
            TableRow row = new TableRow(table.getContext());
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(rowParams);

            TextView name = new TextView(table.getContext());
            name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            name.setSingleLine(false);
            name.setText(article.getName());
            row.addView(name);

            table.addView(row);

            TableRow btnRow = new TableRow(table.getContext());
            LinearLayout btnL = new LinearLayout(table.getContext());

            Button download = new Button(table.getContext());
            download.setText(R.string.btn_download);

            Button delete = new Button(table.getContext());
            delete.setText(R.string.btn_delete);
            delete.setEnabled(false);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(article.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                    table.removeView(row);
                    table.removeView(btnRow);
                }
            });
            Button open = new Button(table.getContext());
            open.setText(R.string.btn_open);
            open.setEnabled(false);
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPDF(article.getPath());
                }
            });

            btnL.addView(download);
            btnL.addView(open);
            btnL.addView(delete);

            btnRow.addView(btnL);
            table.addView(btnRow);

            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PdfDownloader downloadTask = new PdfDownloader(activity, mProgressDialog, article);
                    downloadTask.execute(article.getUrl());
                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            downloadTask.cancel(true);
                        }
                    });
                    open.setEnabled(true);
                    delete.setEnabled(true);
                }
            });
        }
    }

    private void openPDF(String path) {
        File file = new File(path);
        Uri uriPdfPath;
        Log.d("File Exists Check", "File to open exists - " + String.valueOf(file.exists()));
        if (file.exists()) {
            uriPdfPath = FileProvider.getUriForFile(activity.getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            Log.d("PDF File URI", "PDF File URI - " + uriPdfPath);
            Intent pdfOpenIntent = new Intent(Intent.ACTION_VIEW);
            pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfOpenIntent.setClipData(ClipData.newRawUri("", uriPdfPath));
            pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf");
            pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                activity.startActivity(pdfOpenIntent);
            } catch (ActivityNotFoundException activityNotFoundException) {
                Toast.makeText(activity, "There is no app to load corresponding PDF", Toast.LENGTH_LONG).show();
            }
        }
    }
}
