package com.example.user.stockwatch1;

import android.net.Uri;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.HashMap;

/**
 * Created by user on 02-03-2018.
 */

public class AsyncTaskLoadFinancialData extends AsyncTask<String, Void, String> {

    private static final String TAG = "AsyncTaskLoadFinancialD";
    private MainActivity mainActivity;

    private final String stockDataURL = "https://api.iextrading.com";
    private String inputSymbol;

    public AsyncTaskLoadFinancialData(MainActivity mainActi) {
        this.mainActivity = mainActi;
    }

    @Override
    protected String doInBackground(String... strings) {

        inputSymbol = strings[0];

        Uri.Builder buildUri = Uri.parse(stockDataURL).buildUpon();

        buildUri.appendPath("1.0");
        buildUri.appendPath("stock");
        buildUri.appendPath(inputSymbol);
        buildUri.appendPath("quote");

        String urlToUse = buildUri.build().toString();
        Log.d(TAG, "doInBackground: the url obtained is "+urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inpStrm = conn.getInputStream();
            BufferedReader buf_reader = new BufferedReader(new InputStreamReader(inpStrm));

            String line;
            while ((line = buf_reader.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "doInBackground: The string obtained from internet is "+sb.toString());
        }
        catch (Exception e)
        {
            Log.d(TAG, "doInBackground: Exception in loading stock financial data");
            e.printStackTrace();
            return null;
        }

        String jsonFinData = sb.toString();
        return jsonFinData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.d(TAG, "onPostExecute: ");
        HashMap map = parseFinJSONData(s);
        mainActivity.getFromAsyncTaskStockFinancialData(map);
    }

    private HashMap parseFinJSONData(String str)
    {
        //No financial data found
        if(str == null)
            return null;

        HashMap map_stockData = new HashMap();
        try {
            JSONObject jObj = new JSONObject(str);

            String company_name = jObj.getString("companyName");
            String price = jObj.getString("latestPrice");
            String price_change = jObj.getString("change");
            String change_percent = jObj.getString("changePercent");

            map_stockData.put(R.string.map_sym, inputSymbol);
            map_stockData.put(R.string.map_cname, company_name);
            map_stockData.put(R.string.map_price, price);
            map_stockData.put(R.string.map_ch, price_change);
            map_stockData.put(R.string.map_chPer, change_percent);

            Log.d(TAG, "parseFinJSONData: price= "+price+" chande = "+price_change +" per = "+change_percent);
        }
        catch (Exception e)
        {
            Log.d(TAG, "parseFinJSONData: Error while parsing JSON Financial data");;
            e.printStackTrace();
            return null;
        }
        return map_stockData;
    }
}
