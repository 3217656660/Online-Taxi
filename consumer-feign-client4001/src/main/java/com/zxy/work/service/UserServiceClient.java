package com.zxy.work.service;




import com.zxy.work.entities.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "provider-user-service")
public interface UserServiceClient {


    @PostMapping("/user/update/register")
    Map<String,Object> register(@RequestBody User user);


    @GetMapping("/user/get/{mobile}")
    Map<String,Object> getByMobile(@PathVariable("mobile")String mobile);


    @DeleteMapping("/user/update/delete")
    Map<String,Object> delete(@RequestBody User user);


    @PostMapping("/user/login")
    Map<String,Object> login(@RequestBody User user);


    @PutMapping("/user/update/message")
    Map<String,Object> updateMessage(@RequestBody User user);


    @PutMapping("/user/update/password")
    Map<String,Object> updatePassword(@RequestBody Map<String,Object> requestMapper);


    @GetMapping("/user/getById/{id}")
    Map<String,Object> getById(@PathVariable("id")Integer id);


}
