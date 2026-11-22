package com.xxx.agri.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("crop")
public class Crop {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String cropName;
    private String varietyName;
    private String cropType;
    private String suitArea;
    private Integer growthCycle;
    private BigDecimal yieldLevel;
    private Integer plantStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getVarietyName() {
        return varietyName;
    }

    public void setVarietyName(String varietyName) {
        this.varietyName = varietyName;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getSuitArea() {
        return suitArea;
    }

    public void setSuitArea(String suitArea) {
        this.suitArea = suitArea;
    }

    public Integer getGrowthCycle() {
        return growthCycle;
    }

    public void setGrowthCycle(Integer growthCycle) {
        this.growthCycle = growthCycle;
    }

    public BigDecimal getYieldLevel() {
        return yieldLevel;
    }

    public void setYieldLevel(BigDecimal yieldLevel) {
        this.yieldLevel = yieldLevel;
    }

    public Integer getPlantStatus() {
        return plantStatus;
    }

    public void setPlantStatus(Integer plantStatus) {
        this.plantStatus = plantStatus;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
