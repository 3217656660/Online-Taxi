package com.zxy.work.service;




import com.zxy.work.entities.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * 远程调用 provider-user-service 服务提供者
 */
@FeignClient(name = "provider-user-service")
public interface UserServiceClient {


    @PostMapping("/user/update/register")
    ResponseEntity<String> register(@RequestBody User user);


    @GetMapping("/user/get/{mobile}")
    ResponseEntity<String>  getByMobile(@PathVariable("mobile")String mobile);


    @DeleteMapping("/user/update/delete")
    ResponseEntity<String>  delete(@RequestBody User user);


    @PostMapping("/user/login")
    ResponseEntity<String> login(@RequestBody User user);


    @PostMapping("/user/logout")
    void logout(@RequestBody User user);


    @PutMapping("/user/update/message")
    ResponseEntity<String>  updateMessage(@RequestBody User user);


    @PutMapping("/user/update/password")
    ResponseEntity<String> updatePassword(@RequestBody Map<String,Object> requestMapper);


    @GetMapping("/user/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id")Integer id);


}
