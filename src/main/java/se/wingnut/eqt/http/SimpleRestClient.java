package se.wingnut.eqt.http;

import com.google.gson.Gson;

import okhttp3.*;

import java.io.IOException;
import java.io.Serializable;

public class SimpleRestClient implements Serializable {

    public <T> T get(String url, Class<T> clazz) {
        T returnObject = null;

        try (Response response = new OkHttpClient().newCall(new Request.Builder().url(url).build()).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Failed to retrieve data from URL: " + url + " with status code: " + response.code());
            }

            returnObject = new Gson().fromJson(response.body().string(), clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnObject;
    }

}

