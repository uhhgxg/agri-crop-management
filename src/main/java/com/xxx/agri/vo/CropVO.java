package com.xxx.agri.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CropVO(
        Long id, String cropName, String varietyName, String cropType,
        String suitArea, Integer growthCycle, BigDecimal yieldLevel,
        Integer plantStatus, LocalDateTime createTime, LocalDateTime updateTime
) {}
