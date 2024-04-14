package com.zxy.work.controller;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/taxi/review")
public class ReviewServiceClientController {

    @Resource
    private ReviewServiceClient reviewServiceClient;

    /**
     * 创建评论
     * @param review 传来的评论信息
     * @return  评论结果
     */
    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody Review review) throws MyException {
        log.info("创建评论：" + review);
        try{
            return reviewServiceClient.create(review);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 删除评论
     * @param   id 评论id
     * @return  评论删除结果
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam("id") Integer id) throws MyException {
        log.info("删除评论：" + id);
        try{
            return reviewServiceClient.delete(id);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 通过订单id获得评论
     * @param orderId   传来的订单id
     * @return  查询结果
     */
    @GetMapping("/getByOrderId")
    public ApiResponse<Object> getByOrderId(@RequestParam("orderId") Integer orderId) throws MyException {
        log.info("通过订单id获得评论：" + orderId);
        try{
            return reviewServiceClient.getByOrderId(orderId);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }

}
