package life.majiang.community.provide;

import com.alibaba.fastjson.JSON;
import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.dto.GithubUser;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Chowzzx
 * @date 2019/10/14 - 10:27 AM
 */
@Component   //把当前的类初始到spring的上下文（容器）中，加了注解后使用这个类时不需要是是实例化这个对象，用一个注解就可以使用它。
public class GithubProvide {
    //该方法传入参数，返回token
    public String getAccessToken(AccessTokenDTO accessTokenDTO){  //参数超过两个，把参数封装成一个对象。
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
                                                        //把对象转为json格式
        RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(accessTokenDTO));//传递过来的accessTokenDTO是一个对象，通过fastJson转化成json格式
        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token") //需要的调用的地址
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String s = response.body().string();
            String[] split = s.split("&");
            String token = split[0].split("=")[1];
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public GithubUser getUser(String accessToken){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/user?access_token="+accessToken)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            GithubUser githubUser = JSON.parseObject(string, GithubUser.class);//把字符串转成对应的Java对象
            return githubUser;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  //如果抛异常或者其他情况直接返回null
    }

}
