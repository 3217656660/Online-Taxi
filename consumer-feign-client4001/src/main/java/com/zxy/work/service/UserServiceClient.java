package com.zxy.work.service;


import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
/**
 * 远程调用 provider-user-service 服务提供者
 */
@FeignClient(name = "provider-user-service")
public interface UserServiceClient {


    @PostMapping("/user/update/register")
    ApiResponse<String> register(@RequestBody User user);

    @GetMapping("/user/get")
    ApiResponse<Object> getByMobile(@RequestParam("mobile") String mobile);

    @DeleteMapping("/user/update/delete")
    ApiResponse<String> delete(@RequestParam("mobile")String mobile);

    @PostMapping("/user/login")
    ApiResponse<String> login(@RequestBody User user);

    @GetMapping("/user/checkLogin")
    ApiResponse<String> checkLogin();

    @PostMapping("/user/logout")
    ApiResponse<String> logout(@RequestParam("mobile")String mobile);

    @PutMapping("/user/update/message")
    ApiResponse<String> updateMessage(@RequestBody User user);

    @PutMapping("/user/update/password")
    ApiResponse<String> updatePassword(
            @RequestParam("mobile") String mobile,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword
    );

}
