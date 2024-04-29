package com.zxy.work.controller;

import com.zxy.work.entities.NotificationMessage;
import com.zxy.work.vo.DriverActionTakeOrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;


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
