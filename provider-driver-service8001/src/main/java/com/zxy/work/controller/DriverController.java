package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.StatusCode;

import com.zxy.work.service.DriverService;
import lombok.extern.slf4j.Slf4j;
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
    public CommonResult registerDriver(@RequestBody Driver driver){

        log.info( "********司机注册服务8001：*********" );

        //检查该手机号否已注册
        Driver registeredDriver = driverService.selectByMobile( driver.getMobile() );
        if ( registeredDriver != null ) return new CommonResult<>( StatusCode.FAILURE,"您当前手机号已经注册过了:" );


        //开始注册
        int result = driverService.create( driver );

        if (result > 0){
            log.info( driver + "注册成功" );
            return new CommonResult<>( StatusCode.SUCCESS,"司机注册成功" );
        }

        log.info( driver + "注册失败" );
        return new CommonResult<>( StatusCode.FAILURE,"司机注册失败" );
    }


    /**
     * 注销司机，逻辑删除
     * @param driver 传来的用户信息json
     * @return  注销结果
     */
    @DeleteMapping("/update/delete")
    public CommonResult deleteDriver(@RequestBody Driver driver){

        log.info( "********注销服务8001：*********" );

        int result = driverService.delete(driver);

        if (result > 0){
            log.info( driver + "注销成功" );
            return new CommonResult<>( StatusCode.SUCCESS,driver.getMobile() );
        }

        log.info( driver + "注销失败" );
        return new CommonResult<>( StatusCode.FAILURE,"注销失败" );
    }


    /**
     * 根据手机号获取司机信息
     * @param mobile    传来的司机手机号
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/{mobile}")
    public CommonResult getDriverByMobile(@PathVariable("mobile")String mobile){

        log.info( "********查询服务8001：*********" );

        Driver driver = driverService.selectByMobile(mobile);
        if (driver == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }

        log.info( driver + "查找成功" );
        return new CommonResult<>(StatusCode.SUCCESS,driver);
    }


    /**
     * 更新司机信息
     * @param driver 传来的信息json
     * @return  更新的信息结果
     */
    @PutMapping("/update/message")
    public CommonResult updateUser(@RequestBody Driver driver){

        log.info( "********更新信息服务8001：*********" );

        int result = driverService.update(driver);

        if (result > 0){
            log.info(driver + "信息更新成功");
            return new CommonResult(StatusCode.SUCCESS,driver);
        }

        log.info(driver + "信息更新失败");
        return new CommonResult(StatusCode.FAILURE,"修改失败");
    }


}
