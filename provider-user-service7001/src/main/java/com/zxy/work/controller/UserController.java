package com.zxy.work.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@Slf4j
@SaCheckLogin
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 注册用户
     * @param user 传来的用户信息json
     * @return 注册结果:注册成功时返回注册的账号
     */
    @SaIgnore
    @PostMapping("/update/register")
    public ApiResponse<String> registerUser(@RequestBody User user) throws MyException {
        log.info( "用户注册服务提供者:" + user.getMobile());
        return userService.create(user) == 1
                ? ApiResponse.success("注册成功")
                : ApiResponse.error(600, "注册失败");
    }


    /**
     * 注销用户，逻辑删除
     * @param mobile 传来的用户手机号
     * @return  注销结果
     */
    @SaCheckRole(value = "user", mode = SaMode.OR)
    @DeleteMapping("/update/delete")
    public ApiResponse<String> deleteUser(@RequestParam("mobile")String mobile) throws MyException {
        log.info( "用户注销服务提供者：,mobile={}", mobile );
        return userService.deleteByMobile(mobile) == 1
                ? ApiResponse.success("注销成功")
                : ApiResponse.error(600, "注销失败");
    }


    /**
     * 根据手机号获取用户信息，对密码信息脱敏
     * @param mobile   传来的用户手机号
     * @return  获取的结果以及数据
     */
    @SaCheckRole(value = "user", mode = SaMode.OR)
    @GetMapping("/get")
    public ApiResponse<Object> getUserByMobile(@RequestParam("mobile") String mobile) throws MyException {
        log.info( "用户查询服务提供者：mobile={}", mobile );
        User user = userService.selectByMobile(mobile);
        return user != null
                ? ApiResponse.success(user.setPassword("******"))
                : ApiResponse.error(600, "查询失败失败");
    }


    /**
     * 用户登录
     * @param user 传来的用于登录的用户json
     * @return 登录结果
     */
    @SaIgnore
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody User user) throws MyException {
        log.info( "用户登录服务提供者：mobile={}", user.getMobile() );
        if (!userService.login(user)){
            log.warn("手机号={}登录失败，原因密码输入错误", user.getMobile());
            return ApiResponse.error(600, "登录失败，密码输入错误");
        }
        //进行必要操作
        StpUtil.setStpLogic(new StpLogic("user"));
        StpUtil.login(user.getMobile());
        String tokenValue = StpUtil.getTokenValue();
        return ApiResponse.success(tokenValue);
    }


    /**
     * 判断用户登录状态，直接调用即可
     */
    @SaIgnore
    @GetMapping("/checkLogin")
    public ApiResponse<String> checkLogin(){
        return StpUtil.isLogin()
                ? ApiResponse.success("已登录")
                : ApiResponse.error(600, "未登录");
    }


    /**
     * 退出登录
     * @param mobile 登录时使用的手机号
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestParam("mobile") String mobile){
        log.info( "用户退出登录服务提供者mobile={}", mobile );
        //退出登录需要的处理逻辑
        return ApiResponse.success("退出登录成功");
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user 传来的用户信息json
     * @return  更新的用户信息结果
     */
    @SaCheckRole(value = "user", mode = SaMode.OR)
    @PutMapping("/update/message")
    public ApiResponse<String> updateUser(@RequestBody User user) throws MyException {
        log.info( "用户更新信息服务提供者：mobile={}", user.getMobile() );
        return userService.update(user) == 1
                ? ApiResponse.success("更新成功")
                : ApiResponse.error(600, "更新失败");
    }


    /**
     * 更新用户密码
     * @param mobile 传来的手机号
     * @param oldPassword 传来的旧密码
     * @param newPassword 传来的新密码
     * @return 更新结果
     */
    @SaCheckRole(value = "user", mode = SaMode.OR)
    @PutMapping("/update/password")
    public ApiResponse<String> updatePassword(
            @RequestParam("mobile") String mobile,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword
    ) throws MyException {
        log.info( "用户更新密码服务提供者：mobile={}", mobile );
        return userService.updatePassword(mobile, oldPassword, newPassword) == 1
                ? ApiResponse.success("密码更新成功")
                : ApiResponse.error(600, "密码更新失败");
    }

}
