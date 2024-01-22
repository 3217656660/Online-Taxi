package com.zxy.work.controller;

import com.zxy.work.entities.Driver;
import com.zxy.work.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/driver")
public class DriverController {

    @Resource
    private DriverService driverService;


    /**
     * 注册司机（前提身份是用户）
     * @param driver 传来的用户信息json
     * @return 注册结果
     */
    @PostMapping("/update/register")
    public ResponseEntity<Object> registerDriver(@RequestBody Driver driver){
        log.info( "********司机注册服务8001：*********" );
        //参数校验

        return ResponseEntity.ok( driverService.create(driver) );
    }


    /**
     * 注销司机，逻辑删除
     * @param driver 传来的用户信息json
     * @return  注销结果
     */
    @DeleteMapping("/update/delete")
    public ResponseEntity<Object> deleteDriver(@RequestBody Driver driver){
        log.info( "********注销服务8001：*********" );
        //参数校验

        return ResponseEntity.ok( driverService.delete(driver) );
    }


    /**
     * 根据手机号获取司机信息
     * @param mobile    传来的司机手机号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByMobile/{mobile}")
    public ResponseEntity<Object> getDriverByMobile(@PathVariable("mobile")String mobile){
        log.info( "********通过手机号查询服务8001：*********" );
        //参数校验

        return  ResponseEntity.ok( driverService.selectByMobile(mobile) );
    }


    /**
     * 更新司机信息
     * @param driver 传来的信息json
     * @return  更新的信息结果
     */
    @PutMapping("/update/message")
    public ResponseEntity<Object> updateDriver(@RequestBody Driver driver){
        log.info( "********更新信息服务8001：*********" );
        //参数校验

        return ResponseEntity.ok( driverService.update(driver) );
    }


    /**
     * 通过id查找司机
     * @param id 司机id
     * @return  查找结果
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id") Integer id){
        log.info( "********通过id查找信息服务8001：*********" );
        return ResponseEntity.ok( driverService.selectById(id) );
    }



}
