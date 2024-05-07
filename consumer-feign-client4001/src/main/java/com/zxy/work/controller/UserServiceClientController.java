package com.zxy.work.controller;


import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserServiceClient;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@Slf4j
@RequestMapping("/taxi/user")
public class UserServiceClientController {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private CacheUtil redisUtil;


    /**
     * 用户注册.成功后，存入缓存中
     * @param user 前端传来的用户信息json
     * @return 注册结果：成功时返回注册的用户信息，失败时返回失败信息
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) throws MyException {
        log.info("用户注册:" + user.getMobile());
        try{
            return userServiceClient.register(user);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 用户注销
     * @param mobile 前端传来的用户手机号
     * @return 注册结果信息
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam("mobile") String mobile){
        log.info("用户注销：" + mobile);
        try{
            return userServiceClient.delete(mobile);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 通过手机号查询用户
     * @param mobile 前端传来的用户手机号
     * @return 查询结果：成功时返回查询到的用户信息（已脱敏），失败时返回失败信息
     */
    @GetMapping("/getByMobile")
    public ApiResponse<Object> getByMobile(@RequestParam("mobile")String mobile){
        log.info("通过手机号获取用户" + mobile);
        try{
            return userServiceClient.getUserByMobile(mobile);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 用户登录
     * @param user 用户json
     * @return  登录结果：成功时将用户信息一并返回，失败时返回失败信息
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody User user){
        log.info("通过手机号登录用户" + user.getMobile());
        try{
            return userServiceClient.login(user);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 判断用户登录状态，直接调用即可
     */
    @GetMapping("/checkLogin")
    public ApiResponse<String> checkLogin(){
        log.info("检查用户登录状态");
        try{
            return userServiceClient.checkLogin();
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 退出登录
     * @param mobile 用户手机号
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestParam("mobile") String mobile){
        log.info("手机号：" + mobile + "退出登录");
        try{
            return userServiceClient.logout(mobile);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user  前端传来的json
     * @return 更新信息的结果
     */
    @PutMapping("/message")
    public ApiResponse<String>  updateMessage(@RequestBody User user){
        log.info("用户更新信息：" + user);
        try{
            return userServiceClient.updateMessage(user);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 更新用户密码
     * @param mobile 传来的手机号
     * @param oldPassword 传来的旧密码
     * @param newPassword 传来的新密码
     * @return 更新结果
     */
    @PutMapping("/password")
    public ApiResponse<String> updatePassword(
            @RequestParam("mobile") String mobile,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword
    ){
        log.info("用户更新密码：" + mobile);
        try{
            return userServiceClient.updatePassword(mobile, oldPassword, newPassword);
        }catch (Exception e){
            log.warn(e.getMessage());
            return ApiResponse.error(600, e.getMessage());
        }
    }


}
