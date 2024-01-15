package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public CommonResult registerUser(@RequestBody User user){

        log.info( "********注册服务LoginAndRegister7001：*********" );

        //检查该手机号否已注册
        User registeredUser = userService.selectByMobile( user.getMobile() );
        if ( registeredUser != null ) return new CommonResult<>( StatusCode.FAILURE,"您当前手机号已有账号:" + registeredUser.getId() );


        //开始注册
        int result = userService.create( user );

        if (result > 0){
            log.info( user + "注册成功" );
            return new CommonResult<>( StatusCode.SUCCESS,result );
        }
        else{
            log.info( user + "注册失败" );
            return new CommonResult<>( StatusCode.FAILURE,"注册失败" );
        }

    }

    @PostMapping("/delete")
    public CommonResult deleteUser(@RequestBody User user){

        log.info( "********注销服务LoginAndRegister7001：*********" );


        int result = userService.delete( user );

        if (result > 0){
            log.info( user + "注销成功" );
            return new CommonResult<>( StatusCode.SUCCESS,user.getId() );
        }
        else{
            log.info( user + "注销失败" );
            return new CommonResult<>( StatusCode.FAILURE,"注销失败" );
        }

    }

    @GetMapping("/get/{id}")
    public CommonResult getUserById(@PathVariable("id")Integer id){

        log.info( "********查询服务LoginAndRegister7001：*********" );

        User user = userService.selectById(id);
        if (user == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }else {
            log.info( user + "查找成功" );

            //用户信息脱敏
            user.setPassword("**********");
            return new CommonResult<>(StatusCode.SUCCESS,user);
        }
    }

    @PostMapping("/login")
    public CommonResult login(@RequestBody User user){

        log.info( "********登录服务LoginAndRegister7001：*********" );

        //获取输入的密码
        String inputPassword = user.getPassword();

        //获取该账号的加密密码
        User resultUser = userService.selectById(user.getId());
        String encodedPassword = resultUser.getPassword();

        //匹配
        boolean matches = PasswordEncoder.matches(inputPassword, encodedPassword);
        if (!matches){
            log.info(user.getId() + "登录失败");
            return new CommonResult<>(StatusCode.FAILURE,"密码不正确");
        }
        log.info(user.getId() + "登录成功");
        resultUser.setPassword("**************");
        return new CommonResult<>(StatusCode.SUCCESS,resultUser);
    }



}
