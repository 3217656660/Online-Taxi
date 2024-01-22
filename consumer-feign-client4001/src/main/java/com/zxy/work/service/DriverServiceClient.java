package com.zxy.work.service;

import com.zxy.work.entities.Driver;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-driver-service")
public interface DriverServiceClient {


    @PostMapping("/driver/update/register")
    ResponseEntity<String> register(@RequestBody Driver driver);


    @DeleteMapping("/driver/update/delete")
    ResponseEntity<String> delete(@RequestBody Driver driver);


    @GetMapping("/driver/getByMobile/{mobile}")
    ResponseEntity<String> getByMobile(@PathVariable("mobile")String mobile);


    @PutMapping("/driver/update/message")
    ResponseEntity<String> update(@RequestBody Driver driver);


    @GetMapping("/driver/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id") Integer id);

}
