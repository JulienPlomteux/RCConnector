package com.plomteux.rcconnector.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GratuitiesCalculator {
    @Value("${rcc.daily.gratuity:0}")
    private double rccDailyGratuity;

    @Getter
    private static double dailyGratuity;

    @PostConstruct
    private void init() {
        dailyGratuity = rccDailyGratuity;
    }
}
