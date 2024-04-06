package com.zxy.work.service;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.Driver;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-driver-service")
public interface DriverServiceClient {


    @PostMapping("/driver/update/register")
    ApiResponse<String> register(@RequestBody Driver driver);


    @DeleteMapping("/driver/update/delete")
    ApiResponse<String> delete(@RequestParam("mobile") String mobile);


    @PutMapping("/driver/update/message")
    ApiResponse<String> update(@RequestBody Driver driver);


    @GetMapping("/driver/getByMobile")
    ApiResponse<Object> getByMobile(@RequestParam("mobile") String mobile);

}
