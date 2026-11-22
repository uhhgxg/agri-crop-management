package com.xxx.agri.common;

import java.util.List;

public record PageResult<T>(long total, long pages, long pageNum, long pageSize, List<T> records) {}
