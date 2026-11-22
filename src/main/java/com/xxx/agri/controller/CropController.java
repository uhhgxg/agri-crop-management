package com.xxx.agri.controller;

import com.xxx.agri.common.PageResult;
import com.xxx.agri.common.Result;
import com.xxx.agri.dto.CropDTO;
import com.xxx.agri.service.CropService;
import com.xxx.agri.vo.CropVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crop")
public class CropController {

    private static final Logger log = LoggerFactory.getLogger(CropController.class);
    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping("/add")
    public Result<CropVO> add(@RequestBody @Validated(CropDTO.Add.class) CropDTO input) {
        log.info("新增请求 crop={}", input);
        CropVO result = cropService.add(input);
        log.info("新增响应 id={}", result.id());
        return Result.success(result);
    }

    @PutMapping("/update")
    public Result<CropVO> update(@RequestBody @Validated(CropDTO.Update.class) CropDTO input) {
        log.info("修改请求 crop={}", input);
        CropVO result = cropService.update(input);
        log.info("修改响应 id={} updateTime={}", result.id(), result.updateTime());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<CropVO> getById(@PathVariable @Min(value = 1, message = "id必须为正整数") long id) {
        log.info("详情请求 id={}", id);
        CropVO result = cropService.getById(id);
        log.info("详情响应 id={}", result.id());
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "id必须为正整数") long id) {
        log.info("删除请求 id={}", id);
        cropService.delete(id);
        log.info("删除响应 id={}", id);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<PageResult<CropVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于等于1") int pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数必须大于等于1")
            @Max(value = 100, message = "每页最多100条") int pageSize,
            @RequestParam(required = false) @Size(max = 50, message = "作物名称不能超过50个字符") String cropName,
            @RequestParam(required = false) @Size(max = 50, message = "品种名称不能超过50个字符") String varietyName,
            @RequestParam(required = false)
            @Pattern(regexp = "\\s*(?:粮食|蔬菜|水果|油料)?\\s*", message = "作物类型只能是粮食、蔬菜、水果或油料")
            String cropType,
            @RequestParam(required = false) @Min(value = 0, message = "推广状态只能是0或1")
            @Max(value = 1, message = "推广状态只能是0或1") Integer plantStatus) {
        log.info("列表请求 pageNum={} pageSize={} cropName={} varietyName={} cropType={} plantStatus={}",
                pageNum, pageSize, cropName, varietyName, cropType, plantStatus);
        PageResult<CropVO> result = cropService.list(pageNum, pageSize, cropName, varietyName, cropType, plantStatus);
        log.info("列表响应 total={} pages={} returned={}", result.total(), result.pages(), result.records().size());
        return Result.success(result);
    }
}
