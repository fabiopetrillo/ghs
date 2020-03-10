package com.dabico.gseapp.github;

import com.dabico.gseapp.util.DateInterval;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import okhttp3.*;

import java.io.IOException;

import static com.google.gson.JsonParser.parseString;

@NoArgsConstructor
public class GitHubApiService extends HTTPService {
    public Response searchRepositories(String language,
                                       DateInterval interval,
                                       Integer page,
                                       String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.github.com/search/repositories?q=" +
                     "language:" + language +
                     "+created:" + interval.toString() +
                     "+fork:true" +
                     "+is:public" +
                     "&page=" + page +
                     "&per_page=100")
                .addHeader("Authorization", accessToken)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();

        Call call = client.newCall(request);
        return call.execute();
    }

    public boolean isTokenLimitExceeded(String accessToken) throws Exception {
        Request request = new Request.Builder()
                .url("https://api.github.com/rate_limit")
                .addHeader("Authorization", accessToken)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonObject bodyJson = parseString(responseBody.string()).getAsJsonObject();
            response.close();
            JsonObject resources = bodyJson.get("resources").getAsJsonObject();
            JsonObject search = resources.get("search").getAsJsonObject();
            int remaining = search.get("remaining").getAsInt();
            return remaining <= 0;
        } else {
            throw new RuntimeException();
        }
    }
}
