package com.example.user.stockwatch1;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by user on 01-03-2018.
 */

public class AsyncTaskStockSymbol extends AsyncTask<String, Void, String> {

    private static final String TAG = "AsyncTaskStockSymbol";
    private MainActivity mainActivity;

    private final String stockSymbolURL = "http://d.yimg.com/aq/autoc";
    //private HashMap map_stockSymb = new HashMap();
    private String inputStr;

    public AsyncTaskStockSymbol(MainActivity mainActi) {
        this.mainActivity = mainActi;
    }

    @Override
    protected String doInBackground(String... strings) {

        inputStr = strings[0];

        Uri.Builder buildUri = Uri.parse(stockSymbolURL).buildUpon();

        buildUri.appendQueryParameter("region","US");
        buildUri.appendQueryParameter("lang","en-US");
        buildUri.appendQueryParameter("query", inputStr);
        //eg. http://d.yimg.com/aq/autoc?region=US&lang=en-US&query=CAI

        String urlToUse = buildUri. build().toString();
        Log.d(TAG, "doInBackground: Build URL is " + urlToUse);

        StringBuilder sb =  new StringBuilder();
        try{
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inpStrm = conn.getInputStream();
            BufferedReader buf_reader = new BufferedReader(new InputStreamReader(inpStrm));

            String line;
            while((line = buf_reader.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "doInBackground: The string obtained from internet is " + sb.toString());
        }
        catch (Exception e)
        {
            Log.d(TAG, "doInBackground: Exception in Loading Stock Symbol");
            e.printStackTrace();
            return null;
        }

        String jsonData = sb.toString();
        return jsonData; //Goes to onPostExecute
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.d(TAG, "onPostExecute:");

        //NOW parse the obtained stock symbol JSON string to find the matching stock symbol
        HashMap map = parseStockSymbolJSON(s);
        mainActivity.getFromAsyncTaskStockSymbolData(map, inputStr);
    }

    private HashMap parseStockSymbolJSON(String str)
    {
        HashMap map_stockSymb = new HashMap();
        try {
            JSONObject jObjMain = new JSONObject(str);
            //Get the object within object
            JSONObject jResultSet = (JSONObject)jObjMain.getJSONObject("ResultSet");
            //Gets the array of objects
            JSONArray jResultArray = jResultSet.getJSONArray("Result");

            //Log.d(TAG, "parseStockSymbolJSON: comp=obtained from internet is {\"ResultSet\":{\"Query\":\"CIA\",\"Result\":"+jResultArray);
            for(int i=0; i<jResultArray.length(); i++)
            {
                JSONObject jObj = (JSONObject)jResultArray.get(i);

                String type = jObj.getString("type");
                String symb = jObj.getString("symbol");
                String cname = jObj.getString("name");

                if(type.equals("S") && (symb.startsWith(inputStr) || cname.startsWith(inputStr)) && !symb.contains(".")) {
                    Log.d(TAG, "parseStockSymbolJSON: symbol found= "+symb+", name = "+cname);

                    //Add to hashMap
                    map_stockSymb.put(symb, cname);
                }
            }
            //mainActivity.getStockSymbolDataFromAsyncTask(map_stockSymb, inputStr);
        }
        catch (Exception e)
        {
            Log.d(TAG, "parseStockSymbolJSON: Error while parsing JSON symbol data");
            e.printStackTrace();
            return null;
        }
        return map_stockSymb;
    }

}
