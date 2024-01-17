package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.StatusCode;

import com.zxy.work.service.DriverServiceClient;
import com.zxy.work.util.MyString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/consumer/driver")
public class DriverServiceClientController {

    @Resource
    private DriverServiceClient driverServiceClient;


    @PostMapping("/update/register")
    public Map<String,Object> register(@RequestBody Driver driver){
       return driverServiceClient.register(driver);
    }


    @DeleteMapping("/update/delete")
    public Map<String,Object> delete(@RequestBody Driver driver){
        return driverServiceClient.delete(driver);

    }


    @GetMapping("/get/{mobile}")
    public Map<String,Object> getByMobile(@PathVariable("mobile")String mobile){
        return driverServiceClient.getByMobile(mobile);

    }


    @PutMapping("/update/message")
    public Map<String,Object> updateDriver(@RequestBody Driver driver){
        return driverServiceClient.update(driver);
    }


}
