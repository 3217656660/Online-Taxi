package com.zxy.work.service;

import com.zxy.work.entities.Driver;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "provider-driver-service")
public interface DriverServiceClient {


    @PostMapping("/driver/update/register")
    Map<String,Object> register(@RequestBody Driver driver);



    @DeleteMapping("/driver/update/delete")
    Map<String,Object> delete(@RequestBody Driver driver);



    @GetMapping("/driver/get/{mobile}")
    Map<String,Object> getByMobile(@PathVariable("mobile")String mobile);



    @PutMapping("/driver/update/message")
    Map<String,Object> update(@RequestBody Driver driver);


}
