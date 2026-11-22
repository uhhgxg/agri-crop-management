package com.xxx.agri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.agri.common.PageResult;
import com.xxx.agri.dto.CropDTO;
import com.xxx.agri.entity.Crop;
import com.xxx.agri.exception.BusinessException;
import com.xxx.agri.mapper.CropMapper;
import com.xxx.agri.service.CropService;
import com.xxx.agri.vo.CropVO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CropServiceImpl implements CropService {

    private static final Logger log = LoggerFactory.getLogger(CropServiceImpl.class);
    private final CropMapper cropMapper;

    public CropServiceImpl(CropMapper cropMapper) {
        this.cropMapper = cropMapper;
    }

    @Override
    @Transactional
    public CropVO add(CropDTO input) {
        Crop crop = toEntity(input);
        cropMapper.insert(crop);
        log.info("新增农作物品种 id={}", crop.getId());
        return getById(crop.getId());
    }

    @Override
    @Transactional
    public CropVO update(CropDTO input) {
        Crop crop = toEntity(input);
        if (cropMapper.updateById(crop) == 0) {
            throw notFound(input.id());
        }
        log.info("修改农作物品种 id={}", input.id());
        return getById(input.id());
    }

    @Override
    public CropVO getById(long id) {
        Crop crop = cropMapper.selectById(id);
        if (crop == null) {
            throw notFound(id);
        }
        return toView(crop);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (cropMapper.deleteById(id) == 0) {
            throw notFound(id);
        }
        log.info("逻辑删除农作物品种 id={}", id);
    }

    @Override
    public PageResult<CropVO> list(int pageNum, int pageSize, String cropName, String varietyName,
                                   String cropType, Integer plantStatus) {
        LambdaQueryWrapper<Crop> query = Wrappers.lambdaQuery(Crop.class);
        if (StringUtils.hasText(cropName)) {
            query.like(Crop::getCropName, cropName.strip());
        }
        if (StringUtils.hasText(varietyName)) {
            query.like(Crop::getVarietyName, varietyName.strip());
        }
        if (StringUtils.hasText(cropType)) {
            query.eq(Crop::getCropType, cropType.strip());
        }
        if (plantStatus != null) {
            query.eq(Crop::getPlantStatus, plantStatus);
        }
        query.orderByDesc(Crop::getId);

        Page<Crop> page = cropMapper.selectPage(Page.of(pageNum, pageSize), query);
        List<CropVO> records = page.getRecords().stream().map(CropServiceImpl::toView).toList();
        log.info("分页查询完成 pageNum={} pageSize={} total={} returned={}",
                page.getCurrent(), page.getSize(), page.getTotal(), records.size());
        return new PageResult<>(page.getTotal(), page.getPages(), page.getCurrent(), page.getSize(), records);
    }

    private static Crop toEntity(CropDTO input) {
        Crop crop = new Crop();
        crop.setId(input.id());
        crop.setCropName(input.cropName().strip());
        crop.setVarietyName(input.varietyName().strip());
        crop.setCropType(input.cropType());
        crop.setSuitArea(input.suitArea().strip());
        crop.setGrowthCycle(input.growthCycle());
        crop.setYieldLevel(input.yieldLevel());
        crop.setPlantStatus(input.plantStatus());
        return crop;
    }

    private static CropVO toView(Crop crop) {
        return new CropVO(crop.getId(), crop.getCropName(), crop.getVarietyName(), crop.getCropType(),
                crop.getSuitArea(), crop.getGrowthCycle(), crop.getYieldLevel(), crop.getPlantStatus(),
                crop.getCreateTime(), crop.getUpdateTime());
    }

    private static BusinessException notFound(long id) {
        return new BusinessException(HttpStatus.NOT_FOUND, "未找到该农作物品种，id=" + id);
    }
}
