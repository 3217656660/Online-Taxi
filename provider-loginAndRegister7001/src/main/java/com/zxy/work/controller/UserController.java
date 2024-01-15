package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public CommonResult registerUser(@RequestBody User user){

        log.info("********注册服务LoginAndRegister7001：");

        //先检查是否已注册


        //检查无误后再插入
        int result = userService.create(user);

        if (result > 0){
            log.info(user + "注册成功");
            return new CommonResult<>(StatusCode.SUCCESS,result);
        }
        else{
            log.info(user + "注册失败");
            return new CommonResult<>(StatusCode.FAILURE,"注册失败");
        }


    }

}
