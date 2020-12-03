package com.jinx.authserver.auth;

import com.alibaba.fastjson.JSONObject;
import com.jinx.authserver.constant.Constant;
import com.jinx.authserver.bean.AuthRequestParam;
import com.jinx.authserver.bean.AuthResponseData;
import com.jinx.authserver.bean.GetTokenParam;
import com.jinx.authserver.utill.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("auth")
public class AuthHandler {
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    TokenUtils tokenUtils;

    @GetMapping("authorized")
    public ModelAndView authorized(AuthRequestParam authRequestParam) {
        redisTemplate.opsForValue().set(Constant.getAuthParamKey(authRequestParam.getClientId()), JSONObject.toJSONString(authRequestParam));
        ModelAndView modelAndView = new ModelAndView("AuthPage");
        modelAndView.addObject("clientId", authRequestParam.getClientId());
        return modelAndView;
    }

    @GetMapping("createToken/{clientId}")
    public String createToken(@PathVariable("clientId") String clientId) {
        AuthRequestParam authRequestParam = JSONObject.parseObject(redisTemplate.opsForValue().get(Constant.getAuthParamKey(clientId)), AuthRequestParam.class);
        assert authRequestParam != null;
        String code = tokenUtils.createTokenReturnCode(authRequestParam.getClientId()
                , authRequestParam.getClientSecret()
                , authRequestParam.getScope());
        String url = authRequestParam.getRedirectUrl() + "?code=" + code;
        String authCodeKey = Constant.getAuthCodeKey(authRequestParam.getClientId());
        String oldAuthCode = redisTemplate.opsForValue().get(authCodeKey);
        if (!StringUtils.isEmpty(oldAuthCode)){
            redisTemplate.opsForValue().getOperations().delete(oldAuthCode);
        }
        //clientId 和 code（tokenKey）对应关系
        redisTemplate.opsForValue().set(authCodeKey, code);
        return "redirect:" + url;
    }

    @GetMapping("getToken")
    @ResponseBody
    public AuthResponseData getToken(GetTokenParam getTokenParam) throws Exception {
        AuthResponseData authResponseData = tokenUtils.getToken(getTokenParam);
        return authResponseData;
    }
}
