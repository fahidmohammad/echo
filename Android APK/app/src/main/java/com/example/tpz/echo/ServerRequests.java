package com.example.tpz.echo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Dell on 09/11/2015.
 */
public class ServerRequests {

    ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://192.168.1.78:3000/api/search";

    public ServerRequests(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    public void fetchKeywordInBackground(Keyword keyword, GetKeywordCallback keywordCallback){
        progressDialog.show();
        new fetchKeywordAsyncTask(keyword, keywordCallback).execute();
    }

    public class fetchKeywordAsyncTask extends AsyncTask<Void, Void, Result> {
        Keyword keyword;
        Result result = new Result();
        GetKeywordCallback keywordCallback;
        ResultArray resultArray = MainActivity.resultArray;

        public fetchKeywordAsyncTask(Keyword keyword, GetKeywordCallback keywordCallback){
            this.keyword = keyword;
            this.keywordCallback = keywordCallback;
        }

        @Override
        protected Result doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
//            dataToSend.add(new BasicNameValuePair("action", keyword.action));
            if(resultArray != null){
                for (int i = 0; i < resultArray.context_id.size(); i++) {
                    if(resultArray.context_id.get(i) != null) {
                        dataToSend.add(new BasicNameValuePair("context_id", resultArray.context_id.get(i)));
                        dataToSend.add(new BasicNameValuePair("context_action", "hotel.findSimilar"));
                    }
                }
            }
            dataToSend.add(new BasicNameValuePair("message", keyword.message));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS);

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);

                HttpEntity entity = httpResponse.getEntity();
                String result_str = EntityUtils.toString(entity);
                if(result_str.contains("original")){
                    JSONObject object = new JSONObject(result_str);
                    String object1 = object.getJSONObject("original").getString("id");
                    String object2 = object.getJSONObject("original")
                                           .getJSONObject("data")
                                           .getJSONObject("providers")
                                           .getJSONObject("EXPE")
                                           .getJSONArray("code").getString(0);
                    String object3 = object.getJSONObject("original")
                                            .getJSONObject("data")
                                            .getJSONObject("providers")
                                            .getJSONObject("sabre_tn")
                                            .getJSONArray("code")
                                            .getJSONArray(0)
                                            .getJSONObject(1).getString("value");
                    //JSONObject object2 = object.getJSONObject("data");
//                    BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
//
//                    StringBuffer resultStr = new StringBuffer();
//                    String line;
//                    while ((line = rd.readLine()) != null) {
//                        resultStr.append(line);
//                    }
                    String original1 = "Giata ID is "+object1+". Expedia ID is "+object2+". Sabre ID is "+object3;
                    result.original = original1;
                }else {
                    JSONArray jArray = new JSONArray(result_str);
                    if (jArray.length() == 0) {
                        result = null;
                    } else {
                        for (int n = 0; n < jArray.length() - 1; n++) {
                            JSONObject object = jArray.getJSONObject(n);
                            String name = object.getString("name");
                            String context_id = object.getString("id");
                            String context_action = jArray.getJSONObject(jArray.length() - 1).getString("action");
                            result.name.add(name);
                            result.context_id.add(context_id);
                            result.context_action.add(context_action);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Result result){
            progressDialog.dismiss();
            keywordCallback.done(result);
            super.onPostExecute(result);
        }
    }



//    public void storeDataInBackground(Result result, GetResultCallback resultCallback){
//        progressDialog.show();
//        new storeDataAsyncTask(result, resultCallback).execute();
//    }
//    public void fetchDataInBackground(String data, GetDataCallback dataCallback){
//        progressDialog.show();
//        new fetchDataAsyncTask(data, dataCallback).execute();
//    }

//    public class fetchDataAsyncTask extends AsyncTask<Void, Void, Keyword> {
//        String data;
//        Keyword keyword = null;
//        GetDataCallback dataCallback;
//
//        public fetchDataAsyncTask(String data, GetDataCallback dataCallback){
//            this.data = data;
//            this.dataCallback = dataCallback;
//        }
//
//        @Override
//        protected Keyword doInBackground(Void... params) {
//            String API_URL = "";
//            try {
//                API_URL += "https://api.api.ai/v1/query?v=20150910&query=i%20want%20a%20hotel&lang=en";
//            }catch (Exception e){}
//
//            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
//            dataToSend.add(new BasicNameValuePair("action", "testing"));
//
//            HttpParams httpRequestParams = new BasicHttpParams();
//            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
//            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);
//
//            HttpClient client = new DefaultHttpClient(httpRequestParams);
//            HttpGet get = new HttpGet(API_URL);
//
//            try {
//                get.setHeader("Authorization", "Bearer f1b35f4b7ddd4eb282f8fd1f4da049c5");
//                get.setHeader("Content-type", "application/json");
//
//                HttpResponse httpResponse = client.execute(get);
//
//                HttpEntity entity = httpResponse.getEntity();
//                String result_str = "";
//
//                if(entity != null){
//                    result_str = inputStreamToString(entity.getContent());
//                }
//                System.console().printf(result_str);
//                JSONObject jObject = new JSONObject(result_str);
//
//                if(jObject.length() == 0){
//                    keyword = null;
//                }else{
////                    String action = jObject.getJSONObject("result").getString("action");
//                    String message = jObject.getJSONObject("message").getString("message");
//                    keyword = new Keyword(message);
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//            return keyword;
//        }
//
//        @Override
//        protected void onPostExecute(Keyword keyword){
//            progressDialog.dismiss();
//            dataCallback.done(keyword);
//            super.onPostExecute(keyword);
//        }
//    }


//    public class storeDataAsyncTask extends AsyncTask<Void, Void, Void> {
//        Result result;
//        GetResultCallback resultCallback;
//
//        public storeDataAsyncTask(Result result, GetResultCallback resultCallback){
//            this.result = result;
//            this.resultCallback = resultCallback;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
//            dataToSend.add(new BasicNameValuePair("name", result.name));
//
//            HttpParams httpRequestParams = new BasicHttpParams();
//            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
//            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);
//
//            HttpClient client = new DefaultHttpClient(httpRequestParams);
//            HttpPost post = new HttpPost(SERVER_ADDRESS);
//
//            try {
//                post.setEntity(new UrlEncodedFormEntity(dataToSend));
//                client.execute(post);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void aVoid){
//            progressDialog.dismiss();
//            resultCallback.done(null);
//            super.onPostExecute(aVoid);
//        }
//    }
}
