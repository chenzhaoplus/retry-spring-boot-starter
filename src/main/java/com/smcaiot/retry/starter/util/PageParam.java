package com.smcaiot.retry.starter.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 列表查询条件
 * @author yanghs
 */
@Data
@Accessors(chain = true)
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    //排序字段
    private String sort="ID";
    //排序方式 asc、desc
    private String order="DESC";

    /********方式一********/
    //偏移值
    private int offset;
    //每页大小
    private int limit=10;

    /********方式二********/
    //页码
    private int pageNum=1;
    //每页大小
    private int pageSize=10;

    /********方式三********/
    //页码
    private int page;
    //每页大小
    private int rows=10;

    //private boolean exportExcelFlag = false;
}
