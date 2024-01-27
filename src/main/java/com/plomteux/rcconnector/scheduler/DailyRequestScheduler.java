package com.plomteux.rcconnector.scheduler;

import com.plomteux.rcconnector.controller.RCControllerApi;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DailyRequestScheduler {
    private RCControllerApi rcControllerApi;

    @Scheduled(cron = "0 0 7 * * ?")
    public void triggerDailyCruiseDetailsRequest() {
        rcControllerApi.getCruiseDetails();
    }

}
