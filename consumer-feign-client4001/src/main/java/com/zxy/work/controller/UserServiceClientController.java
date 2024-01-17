package com.zxy.work.controller;


import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/consumer/user/")
public class UserServiceClientController {

    @Resource
    private UserServiceClient userServiceClient;


    @PostMapping("/update/register")
    public Map<String,Object> register(@RequestBody User user){
        return userServiceClient.register(user);
    }


    @DeleteMapping("/update/delete")
    public Map<String,Object> delete(@RequestBody User user){
        return userServiceClient.delete(user);
    }


    @GetMapping("/get/{mobile}")
    public Map<String,Object> getByMobile(@PathVariable("mobile")String mobile){
        return userServiceClient.getByMobile(mobile);
    }


    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody User user){
        return userServiceClient.login(user);
    }


    @PutMapping("/update/message")
    public Map<String,Object> updateMessage(@RequestBody User user){
        return userServiceClient.updateMessage(user);
    }


    @PutMapping("/update/password")
    public Map<String,Object> updatePassword(@RequestBody Map<String,Object> requestMapper){
        return userServiceClient.updatePassword(requestMapper);
    }


    @GetMapping("/getById/{id}")
    public Map<String,Object> getById(@PathVariable("id")Integer id){
        return userServiceClient.getById(id);
    }



}
