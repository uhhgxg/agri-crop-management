package com.xxx.agri.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import java.math.BigDecimal;

public record CropDTO(
        @Null(groups = Add.class, message = "新增时请勿传入id")
        @NotNull(groups = Update.class, message = "修改时id不能为空")
        @Min(value = 1, message = "id必须为正整数")
        Long id,

        @NotBlank(message = "作物名称不能为空")
        @Size(max = 50, message = "作物名称不能超过50个字符")
        String cropName,

        @NotBlank(message = "品种名称不能为空")
        @Size(max = 50, message = "品种名称不能超过50个字符")
        String varietyName,

        @NotBlank(message = "作物类型不能为空")
        @Pattern(regexp = "粮食|蔬菜|水果|油料", message = "作物类型只能是粮食、蔬菜、水果或油料")
        String cropType,

        @NotBlank(message = "适宜种植区域不能为空")
        @Size(max = 120, message = "适宜种植区域不能超过120个字符")
        String suitArea,

        @NotNull(message = "生长周期不能为空")
        @Min(value = 1, message = "生长周期不能小于1天")
        @Max(value = 3650, message = "生长周期不能超过3650天")
        Integer growthCycle,

        @NotNull(message = "亩产预估不能为空")
        @DecimalMin(value = "0", message = "亩产预估不能为负数")
        @DecimalMax(value = "100000", message = "亩产预估不能超过100000kg")
        @Digits(integer = 6, fraction = 2, message = "亩产预估最多允许两位小数")
        BigDecimal yieldLevel,

        @NotNull(message = "推广状态不能为空")
        @Min(value = 0, message = "推广状态只能是0或1")
        @Max(value = 1, message = "推广状态只能是0或1")
        Integer plantStatus
) {
    public interface Add extends Default {}

    public interface Update extends Default {}
}
