package cn.usbfacedetect.netokhttp;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NetOkhttp {

    private static final MediaType JSON = MediaType.parse("Application/json; charset=utf-8");

    public static void sync_doGet(final String URL, final ReceiveData receiveData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    client.newBuilder().readTimeout(9000, TimeUnit.MILLISECONDS).writeTimeout(9000, TimeUnit.MILLISECONDS).connectTimeout(9000, TimeUnit.MILLISECONDS).build();
                    Request request = new Request.Builder().url(URL).build();
                    Response response = client.newCall(request).execute();
                    Logger.i("请求数据：" + request.toString());
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        Logger.i("响应数据：" + data);
                        receiveData.getData(new JSONObject(data), new Gson());

                    } else {
                        Logger.i("响应失败");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void asyn_doGet(String url, final ReceiveData receiveData) {
        try {
            Logger.i("main thread id is " + Thread.currentThread().getId());
            OkHttpClient client = new OkHttpClient();
            client.newBuilder().readTimeout(9000, TimeUnit.MILLISECONDS).writeTimeout(9000, TimeUnit.MILLISECONDS).connectTimeout(9000, TimeUnit.MILLISECONDS).build();
            Request request = new Request.Builder().url(url).build();
            Logger.i("请求数据：" + request.toString());
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {

                    //可以做一些加密解密的东西
                    // 注：该回调是子线程，非主线程
                    try {
                        Logger.i("callback thread id is " + Thread.currentThread().getId());
                        if (response.isSuccessful()) {
                            String data = response.body().string();
                            Logger.i("响应数据：" + data);
                            receiveData.getData(new JSONObject(data), new Gson());
                        } else {
                            Logger.i("响应失败");
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doJsonPost(final String url, final Object info, final ReceiveData receiveData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    client.newBuilder()
                            .readTimeout(9000, TimeUnit.MILLISECONDS)
                            .writeTimeout(9000, TimeUnit.MILLISECONDS)
                            .connectTimeout(9000, TimeUnit.MILLISECONDS)
                            .build();

                    RequestBody body = RequestBody.create(JSON, new Gson().toJson(info));
                    Request request = new Request.Builder().url(url).post(body).build();
                    Logger.i("请求数据：" + request.toString());
                    Response response = client.newCall(request).execute();

                    String data = response.body().string();
                    Logger.i("响应数据：" + data);

                    if (response.isSuccessful()) {
                        receiveData.getData(new JSONObject(data), new Gson());
                    } else {
                        receiveData.fail(new JSONObject(data), new Gson());
                        Logger.i("响应失败");
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void doPostFromParameters(final String url, final FormBody formBody, final ReceiveData receiveData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    client.newBuilder().readTimeout(9000, TimeUnit.MILLISECONDS).writeTimeout(9000, TimeUnit.MILLISECONDS).connectTimeout(9000, TimeUnit.MILLISECONDS).build();
                    Request request = new Request.Builder().url(url).post(formBody).build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        Logger.i("响应数据：" + data);
                        receiveData.getData(new JSONObject(data), new Gson());
                    } else {
                        Logger.i("响应失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public interface ReceiveData {
        void getData(JSONObject object, Gson gson) throws JSONException;
        void fail(JSONObject object, Gson gson) throws JSONException;
    }
}
