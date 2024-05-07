package com.zxy.work.controller;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.NotificationMessage;
import com.zxy.work.util.cache.CacheUtil;
import com.zxy.work.vo.ChatMessageVo;
import com.zxy.work.vo.DriverActionTakeOrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private CacheUtil redisUtil;

    private static final String COMMON_KEY = "message:order:id:";


    /**
     * 发送消息
     * @param chatMessageVo 消息体
     */
    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessageVo chatMessageVo) {
        //1.消息放入缓存
        redisUtil.lSet(COMMON_KEY + chatMessageVo.getOrderId(), chatMessageVo, 3 * 60 * 60);

        //2.根据sender决定推送给谁
        if (Objects.equals(chatMessageVo.getSender(), "乘客")){//推送给司机
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(chatMessageVo.getDriverId()),
                    "/queue/messageToDriver/notifications",
                    chatMessageVo
            );
        }else {//推送给乘客
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(chatMessageVo.getUserId()),
                    "/queue/messageToUser/notifications",
                    chatMessageVo
            );
        }

    }


    /**
     * 从redis中获取两人的历史信息
     * @param id 订单id
     * @return 信息列表
     */
    @GetMapping("/order/message/getHistory")
    public ApiResponse< List<Object> > getHistory(@RequestParam("id") long id){
        String key = COMMON_KEY + id;
        List<Object> list = redisUtil.lGet(key, 0, -1);
        if (list.size() == 0)
            return ApiResponse.error(600, "还未产生消息");
        return ApiResponse.success(list);
    }


    /**
     * 待司机接单成功后，为用户更新司机位置
     * @param driverActionTakeOrderVo 司机端传来的信息
     */
    @MessageMapping("/sendLocation")
    public void sendLocation(DriverActionTakeOrderVo driverActionTakeOrderVo) {
        List<Double> location = new ArrayList<>();
        assert driverActionTakeOrderVo != null;
        location.add(driverActionTakeOrderVo.getNowAddressLongitude());
        location.add(driverActionTakeOrderVo.getNowAddressLatitude());
        NotificationMessage message = new NotificationMessage();
        message.setType("locationUpdate")
                .setContent(location)
                .setUserId(driverActionTakeOrderVo.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(driverActionTakeOrderVo.getUserId()),
                "/queue/locationUpdate/notifications",
                message
        );
        log.info("将司机位置={}推送给用户", location);
    }




}
