package com.zio.idfort.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OCRSpaceCall {

    private final String URL = "https://api.ocr.space/parse/image";//"https://httpbin.org/post";
    private final String TESTURL = "https://httpbin.org/post";
    private final String KEY = "K84751919488957";

    private final RequestQueue mRequestQue;
    Context context;

    public OCRSpaceCall(Context context) {

        this.context = context;
        this.mRequestQue = Volley.newRequestQueue(context);
        mRequestQue.getCache().clear();
    }

    public interface ResultListenerCallback {
        void onActionSuccess(String successMessage);

        void onActionFailure(String Error);
    }

    public void SetResultListener(String base64, ResultListenerCallback callback) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.TAG, "got" + response);
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("IsErroredOnProcessing"))
                                callback.onActionFailure("Errored on process");
                            else {
                                JSONArray parsedResultsArray = jsonResponse.getJSONArray("ParsedResults");
                                JSONObject firstResultObject = parsedResultsArray.getJSONObject(0);
                                String parsedText = firstResultObject.optString("ParsedText");
                                callback.onActionSuccess(parsedText);
                            }
                        } catch (JSONException e) {
                            callback.onActionFailure("Failed to parse JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.TAG, "no response from ocr");
                        callback.onActionFailure("Failed to get response");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                //header.put("Content-Type", "multipart/form-data");
                header.put("apikey", "helloworld");
                Log.d(Constants.TAG, "header added");
                return header;

            }
                /*@Override
                public byte[] getBody() {
                    final String requestBody = jsonBody.toString();
                    Log.d(Constants.TAG, "got body");
                    return requestBody.getBytes(StandardCharsets.UTF_8);
                }*/

                /*@Override
                public String getBodyContentType() {
                    Log.d(Constants.TAG, "cont type added");
                    return "multipart/form-data";
                }*/


            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.d(Constants.TAG, "params added");
                Map<String, String> param = new HashMap<>();
                //param.put("url", "http://dl.a9t9.com/ocrbenchmark/eng.png");
                param.put("base64Image", base64);
                //param.put("detectOrientation", "true");
                //param.put("scale", "true");
                return param;
            }
        };

        mRequestQue.add(stringRequest);

    }
}
