package com.example.melih.mandalina;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Details extends AppCompatActivity {

    String name = "unknown" , price = "" , url ="" , imgUrl ="" ;
    TextView priceTextView, nameTextView, descTextView;
    Button purchaseBtn;
    Activity context;
    ImageView imgUrlImageView;
    private String responseXML = "";
    private static String hostIP = "http://garanti-sanalpos.eu-gb.mybluemix.net/VPServlet";
    public ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                price = "-1 $";
            } else {
                price = extras.getString("price");
                name = extras.getString("name");
                imgUrl = extras.getString("imgUrl");
                url = extras.getString("productUrl");
            }
        } else {
            price = (String) savedInstanceState.getSerializable("price");
            name = (String) savedInstanceState.getSerializable("name");
            imgUrl = (String) savedInstanceState.getSerializable("imgUrl");
            url = (String) savedInstanceState.getSerializable("url");
        }
        priceTextView = (TextView) findViewById(R.id.priceTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        descTextView = (TextView) findViewById(R.id.descTextView);
        purchaseBtn = (Button) findViewById(R.id.purchaseBtn);
        pd = new ProgressDialog(Details.this);
        context = this;

        purchaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Islemi onayliyor musunuz?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Evet",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                new BackgroundTask().execute();
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton("Hayir",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();


            }
        });

        priceTextView.setText(price);
        nameTextView.setText(name);
        imgUrlImageView = (ImageView) findViewById(R.id.imgUrlImageView);
        Picasso.with(this).load(imgUrl).fit().placeholder(R.drawable.abc_list_focused_holo).into(imgUrlImageView);

        new GetDetailsTask().execute(url);


    }
class GetDetailsTask extends AsyncTask<String,Void,String>{

    @Override
    protected String doInBackground(String... params) {
        String desc="";
        try {
            Document doc = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36").timeout(9000).get();
            Elements elements = doc.select("div#user-product-desc");
            desc = elements.text();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return desc;
    }

    @Override
    protected void onPostExecute(String s) {
        descTextView.setText(s);
        super.onPostExecute(s);
    }
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
        if(id==android.R.id.home){
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class BackgroundTask extends AsyncTask<Void , Void , Void>{



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Isleminiz Gerceklestiriliyor...");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String orderId = "123";
            //transaction amount degerindeki nokta isaretlerinin kaldirilmasi
            String amount = getFormattedAmount(price);
            String hash = "A4BBFB8B305F0E6E899B362A951E4060CDEE5277";

            String requestXML = generateXML("PROVAUT", hash, "PROVAUT", "1.1.1.1",
                    "123", "1212", "123", orderId, amount, "949");

            //request olarak gonderilen xml console ekranina yazdiriliyor
            System.out.println(requestXML);
            String hostAddress = hostIP + ":9999/VPServlet";

            try {
                //POST ?stegi atilarak cevap donuyor
                //Donen ddata XML formatindadir
                responseXML = createPostRequest(requestXML, hostAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.hide();
            //Cevap konsola yazdiriliyor
            System.out.println("XML output from Server .... \n");
            System.out.println(responseXML);
            //Response datasi Android ekranina yazdiriliyor
        }
    }


    public static String getFormattedAmount(String amount) {
        String formattedAmount = amount.replaceAll("," , "");
        Log.d("amount", formattedAmount);
        return formattedAmount;
    }

    private static String generateXML(String userName, String hash,
                                      String userID, String ipAddress, String paymentToolNumber,
                                      String expDate, String cvc, String orderID, String amount,
                                      String currency) {

        try {
            // Create instance of DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            // Get the DocumentBuilder
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            // Create blank DOM Document
            org.w3c.dom.Document doc = docBuilder.newDocument();

            Element root = doc.createElement("GVPSRequest");
            doc.appendChild(root);

            Element Mode = doc.createElement("Mode");
            Mode.appendChild(doc.createTextNode("PROD"));
            root.appendChild(Mode);

            Element Version = doc.createElement("Version");
            Version.appendChild(doc.createTextNode("v0.01"));
            root.appendChild(Version);

            Element Terminal = doc.createElement("Terminal");
            root.appendChild(Terminal);

            Element ProvUserID = doc.createElement("ProvUserID");
            // ProvUserID.appendChild(doc.createTextNode(userName));
            ProvUserID.appendChild(doc.createTextNode("PROVAUT"));
            Terminal.appendChild(ProvUserID);

            Element HashData_ = doc.createElement("HashData");
            HashData_.appendChild(doc.createTextNode(hash));
            Terminal.appendChild(HashData_);

            Element UserID = doc.createElement("UserID");
            UserID.appendChild(doc.createTextNode("deneme"));
            Terminal.appendChild(UserID);

            Element ID = doc.createElement("ID");
            ID.appendChild(doc.createTextNode("10000039"));
            Terminal.appendChild(ID);

            Element MerchantID = doc.createElement("MerchantID");
            MerchantID.appendChild(doc.createTextNode(userID));
            Terminal.appendChild(MerchantID);

            Element Customer = doc.createElement("Customer");
            root.appendChild(Customer);

            Element IPAddress = doc.createElement("IPAddress");
            IPAddress.appendChild(doc.createTextNode(ipAddress));
            Customer.appendChild(IPAddress);

            Element EmailAddress = doc.createElement("EmailAddress");
            EmailAddress.appendChild(doc.createTextNode("aa@b.com"));
            Customer.appendChild(EmailAddress);

            Element Card = doc.createElement("Card");
            root.appendChild(Card);

            Element Number = doc.createElement("Number");
            Number.appendChild(doc.createTextNode(paymentToolNumber));
            Card.appendChild(Number);

            Element ExpireDate = doc.createElement("ExpireDate");
            ExpireDate.appendChild(doc.createTextNode("1212"));
            Card.appendChild(ExpireDate);

            Element CVV2 = doc.createElement("CVV2");
            CVV2.appendChild(doc.createTextNode(cvc));
            Card.appendChild(CVV2);

            Element Order = doc.createElement("Order");
            root.appendChild(Order);

            Element OrderID = doc.createElement("OrderID");
            OrderID.appendChild(doc.createTextNode(orderID));
            Order.appendChild(OrderID);

            Element GroupID = doc.createElement("GroupID");
            GroupID.appendChild(doc.createTextNode(""));
            Order.appendChild(GroupID);

			/*
             * Element Description=doc.createElement("Description");
			 * Description.appendChild(doc.createTextNode(""));
			 * Order.appendChild(Description);
			 */

            Element Transaction = doc.createElement("Transaction");
            root.appendChild(Transaction);

            Element Type = doc.createElement("Type");
            Type.appendChild(doc.createTextNode("sales"));
            Transaction.appendChild(Type);

            Element InstallmentCnt = doc.createElement("InstallmentCnt");
            InstallmentCnt.appendChild(doc.createTextNode(""));
            Transaction.appendChild(InstallmentCnt);

            Element Amount = doc.createElement("Amount");
            Amount.appendChild(doc.createTextNode(amount));
            Transaction.appendChild(Amount);

            Element CurrencyCode = doc.createElement("CurrencyCode");
            CurrencyCode.appendChild(doc.createTextNode(currency));
            Transaction.appendChild(CurrencyCode);

            Element CardholderPresentCode = doc
                    .createElement("CardholderPresentCode");
            CardholderPresentCode.appendChild(doc.createTextNode("0"));
            Transaction.appendChild(CardholderPresentCode);

            Element MotoInd = doc.createElement("MotoInd");
            MotoInd.appendChild(doc.createTextNode("N"));
            Transaction.appendChild(MotoInd);

            // Convert dom to String
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            aTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            aTransformer
                    .transform(new DOMSource(doc), new StreamResult(buffer));
            return buffer.toString();

        } catch (Exception e) {
            return null;
        }

    }

    private static String createPostRequest(String request, String hostAddress)
            throws Exception {

        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                System.out.println("Warning: URL Host: " + urlHostName
                        + " vs. " + session.getPeerHost());
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        URL garanti = new URL(hostAddress);

        //servis url ine connection acilmasi. request datasi stream olarak aktariliyor.
        URLConnection connection = garanti.openConnection();
        connection.setDoOutput(true);

        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream());
        out.write("data=" + request);
        out.flush();
        out.close();

        //sunucudan gelen cevap okunuyor.
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));

        StringBuilder responseXMLBuilder = new StringBuilder();

        String line;
        //response datasi string icerisine aktariliyor.
        while ((line = in.readLine()) != null)
            responseXMLBuilder.append(line);
        responseXMLBuilder.append("\n");
        in.close();

        return responseXMLBuilder.toString();

    }



}
