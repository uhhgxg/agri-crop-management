package com.xxx.agri.service;

import com.xxx.agri.common.PageResult;
import com.xxx.agri.dto.CropDTO;
import com.xxx.agri.vo.CropVO;

public interface CropService {

    CropVO add(CropDTO input);

    CropVO update(CropDTO input);

    CropVO getById(long id);

    void delete(long id);

    PageResult<CropVO> list(int pageNum, int pageSize, String cropName, String varietyName,
                            String cropType, Integer plantStatus);
}
