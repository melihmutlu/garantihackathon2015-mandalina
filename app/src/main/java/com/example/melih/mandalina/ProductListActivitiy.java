package com.example.melih.mandalina;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ProductListActivitiy extends AppCompatActivity {

    private GridView gridView;
    ProgressDialog dialog;
    private GridAdapter adapter;
    private List<Product> products = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        gridView = (GridView) findViewById(R.id.gridView);
        dialog = new ProgressDialog(ProductListActivitiy.this);
        gridView.setNumColumns(2);
        gridView.setColumnWidth(getResources().getDisplayMetrics().widthPixels / 2);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ProductListActivitiy.this,Details.class);
                Bundle extras = new Bundle();
                extras.putString("price",products.get(position).getPrice());
                extras.putString("name",products.get(position).getName());
                extras.putString("productUrl",products.get(position).getUrl());
                extras.putString("imgUrl",products.get(position).getImgUrl());
                i.putExtras(extras);
                startActivity(i);
            }
        });
        adapter = new GridAdapter(products,this);
        gridView.setAdapter(adapter);
        String query = getIntent().getStringExtra("query");
        String url = null;
        try {
            url = "http://www.gittigidiyor.com/arama/?k=" + URLEncoder.encode(query, "utf-8") + "&kst=some";
            Log.d("URL", url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new HTMLParse().execute(url);

    }


    public class HTMLParse extends AsyncTask<String , Void , String> {

        String imgUrl ="";
        String name = "";
        String productUrl = "";
        String price = "";

        @Override
        protected void onPreExecute() {

            dialog.setMessage("Loading..");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            Document doc = null;
            try{
                doc = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36").timeout(9000).get();

                Elements container = doc.select("div.cell-border-css");

                for(Element e : container){
                    name = e.select("h4.product-title").text();
                    productUrl = e.select("a.picture").get(0).absUrl("href");
                    price = e.select("span[itemprop=price]").text();
                    imgUrl = e.select("meta[itemprop=image]").attr("content");

        		Log.d("INFO",name);
                    products.add(new Product(name , price ,imgUrl , productUrl));

                }

            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            dialog.dismiss();
            super.onPostExecute(aVoid);
            gridView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }
    }
}
