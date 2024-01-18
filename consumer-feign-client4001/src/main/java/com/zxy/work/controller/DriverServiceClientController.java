package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.User;
import com.zxy.work.service.DriverServiceClient;
import com.zxy.work.service.UserServiceClient;
import com.zxy.work.util.MyString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/taxi/driver")
@SaCheckLogin
public class DriverServiceClientController {

    @Resource
    private DriverServiceClient driverServiceClient;


    @Resource
    private UserServiceClient userServiceClient;


    @PostMapping("/register")
    @SaIgnore
    public Map<String,Object> register(@RequestBody Driver driver){
       return driverServiceClient.register(driver);
    }


    @PostMapping("/login")
    @SaIgnore
    public ResponseEntity <Map<String,Object> > login(@RequestBody User user){
        Map<String, Object> result = userServiceClient.login(user);
        Object data = result.get("data");

        if ( Objects.equals(data,MyString.ACCOUNT_ERROR) || Objects.equals(data,MyString.PASSWORD_ERROR) ) {
            // 登录失败，返回未授权的错误响应实体
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        } else {// 账号密码均正确
            //检查有没有注册成司机
            Map<String, Object> resultMap = driverServiceClient.getByMobile(user.getMobile());
            if ( Objects.equals(resultMap.get("data"),MyString.FIND_ERROR) ) {
                //司机没有注册
                return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
            }

            //司机已注册,通过手机号获取该用户的唯一token
            StpUtil.login( user.getMobile() );
            String token = StpUtil.getTokenValue();
            //设置响应头并返回成功响应实体
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Token", token);
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        }

    }


    @PostMapping("/logout")
    public void logout(@RequestBody User user){
        userServiceClient.logout(user);

        driverServiceClient.logout( new Driver().setMobile( user.getMobile() ) );
        //使token失效
        StpUtil.logout( user.getMobile() );
        log.info("手机号：" + user.getMobile() + "退出登录成功");
    }


    @DeleteMapping("/delete")
    public Map<String,Object> delete(@RequestBody Driver driver){
        return driverServiceClient.delete(driver);

    }


    @GetMapping("/getByMobile/{mobile}")
    public Map<String,Object> getByMobile(@PathVariable("mobile")String mobile){
        return driverServiceClient.getByMobile(mobile);

    }


    @PutMapping("/message")
    public Map<String,Object> updateMessage(@RequestBody Driver driver){
        return driverServiceClient.update(driver);
    }


}
