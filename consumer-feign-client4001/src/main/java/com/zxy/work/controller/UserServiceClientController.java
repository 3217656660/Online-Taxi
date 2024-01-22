package com.zxy.work.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.zxy.work.entities.User;
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
@RequestMapping("/taxi/user/")
@SaCheckLogin
public class UserServiceClientController {

    @Resource
    private UserServiceClient userServiceClient;


    /**
     * 用户注册
     * @param user 前端传来的用户信息json
     * @return 注册结果：成功时返回注册的用户信息，失败时返回失败信息
     */
    @PostMapping("/register")
    @SaIgnore
    public ResponseEntity<String>  register(@RequestBody User user){
        return userServiceClient.register(user);
    }


    /**
     * 用户注销
     * @param user 前端传来的用户信息json
     * @return 注册结果信息
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String>  delete(@RequestBody User user){
        return userServiceClient.delete(user);
    }


    /**
     * 通过手机号查询用户
     * @param mobile 前端传来的用户手机号
     * @return 查询结果：成功时返回查询到的用户信息（已脱敏），失败时返回失败信息
     */
    @GetMapping("/getByMobile/{mobile}")
    public ResponseEntity<String> getByMobile(@PathVariable("mobile")String mobile){
        return userServiceClient.getByMobile(mobile);
    }


    /**
     * 用户登录
     * @param user 用户json
     * @return  登录结果：成功时将用户信息一并返回，失败时返回失败信息
     */
    @PostMapping("/login")
    @SaIgnore
    public ResponseEntity <Object> login(@RequestBody User user){
        Object result =  userServiceClient.login(user).getBody();
        // 登录失败
        if ( Objects.equals(result,MyString.ACCOUNT_ERROR) || Objects.equals(result,MyString.PASSWORD_ERROR) )
            return ResponseEntity.ok(result);
        // 登录成功
        StpUtil.login( user.getMobile() );
        String token = StpUtil.getTokenValue();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Token", token);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }


    /**
     * 退出登录
     * @param user 前端传来的json
     */
    @PostMapping("/logout")
    public void logout(@RequestBody User user){
        userServiceClient.logout(user);
        //使token失效
        StpUtil.logout( user.getMobile() );
        log.info("手机号：" + user.getMobile() + "退出登录成功");
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user  前端传来的json
     * @return 更新信息的结果
     */
    @PutMapping("/message")
    public ResponseEntity<String>  updateMessage(@RequestBody User user){
        return userServiceClient.updateMessage(user);
    }


    /**
     * 更新用户密码
     * @param requestMapper 前端传来的json
     * @return 更新密码的结果
     */
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String,Object> requestMapper){
        return userServiceClient.updatePassword(requestMapper);
    }


    /**
     * 通过id获取用户
     * @param id 前端传来的用户id
     * @return 查找结果：成功查询返回查询到的用户，失败时返回失败信息
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<String> getById(@PathVariable("id")Integer id){
        return userServiceClient.getById(id);
    }

}
