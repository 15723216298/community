package life.majiang.community.controller;

import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.dto.GithubUser;
import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.User;
import life.majiang.community.provide.GithubProvide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author Chowzzx
 * @date 2019/10/14 - 10:03 AM
 */
@Controller
public class AuthorizeController {

    @Autowired
    private GithubProvide githubProvide;

    @Autowired
    private UserMapper userMapper;    //无视错误

    @Value("${github.client.id}")  //在配置文件中注入，在这里使用value注解使用
    private String clientId;
    @Value("${github.client.secret}")
    private String clientSecret;
    @Value("${github.redirect.uri}")
    private String redirectUri;

    @GetMapping("/callback")    //这个接口是github方携带code、state主动调用的
    public String callback(@RequestParam(name="code") String code,   //使用@RequestParam接收通过get方式（？）传递的参数
                           @RequestParam(name="state") String state,
                           HttpServletRequest request
    ){
        //需要做的，使用GitHub传递过来的code、state调用GitHub的access_token接口（post方式）
        //此时需要用Java模拟post请求，使用OkHttp携带code、state调用GitHub的access_token接口，拿到token后，再次调用user接口，返回user信息。
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);
        String accessToken = githubProvide.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvide.getUser(accessToken);
        if(githubUser != null){
            User user = new User();
            user.setToken(UUID.randomUUID().toString());
            user.setName(githubUser.getName());
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            user.setAccountId(String.valueOf(githubUser.getId()));
            userMapper.insert(user);
            //登录成功，写cookie和session
            request.getSession().setAttribute("user",githubUser);
            return "redirect:/";//让地址简洁
        }else{
            //登录失败，重新登录
            return "redirect:/";
        }
    }
}
