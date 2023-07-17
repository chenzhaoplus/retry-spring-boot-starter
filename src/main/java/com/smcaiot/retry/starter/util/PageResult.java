package com.smcaiot.retry.starter.util;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 列表结果
 * @author yanghs
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/********方式一********/
	//列数据
//	private List<T> rows;
	//总条数
//	private long total;

	/********方式二********/
	//列数据
	private List<T> content;
	//总条数
	private long totalElements;

	/**
	 * 配合 pageHelper 分页
	 *
	 * @param list （PageInfo）
	 * @param <T>  泛型
	 * @return 拼装分页后的PageResult
	 */
	public static <T> PageResult<T> of(List<T> list) {
		PageInfo<T> pageInfo = new PageInfo<>(list);
		PageResult<T> result = new PageResult<>();
		result.setContent(pageInfo.getList());
		result.setTotalElements(pageInfo.getTotal());
		return result;
	}

}
