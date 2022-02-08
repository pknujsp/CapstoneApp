package com.lifedawn.capstoneapp.retrofits.parameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LocalApiPlaceParameter implements Serializable {
	private String query;
	private String categoryGroupCode;
	private String x;
	private String y;
	private String radius;
	private String page;
	private String size;
	private String sort;
	private String rect;
	
	public static final String DEFAULT_PAGE = "1";
	public static final String DEFAULT_SIZE = "15";
	public static final String SORT_ACCURACY = "accuracy";
	public static final String SORT_DISTANCE = "distance";
	
	public static final int SEARCH_CRITERIA_MAP_POINT_MAP_CENTER = 0;
	public static final int SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION = 1;
	public static final int SEARCH_CRITERIA_SORT_TYPE_DISTANCE = 2;
	public static final int SEARCH_CRITERIA_SORT_TYPE_ACCURACY = 3;
	
	public String getQuery() {
		return query;
	}
	
	public LocalApiPlaceParameter setQuery(String query) {
		this.query = query;
		return this;
	}
	
	public String getCategoryGroupCode() {
		return categoryGroupCode;
	}
	
	public LocalApiPlaceParameter setCategoryGroupCode(String categoryGroupCode) {
		this.categoryGroupCode = categoryGroupCode;
		return this;
	}
	
	public String getX() {
		return x;
	}
	
	public LocalApiPlaceParameter setX(String x) {
		this.x = x;
		return this;
	}
	
	public String getY() {
		return y;
	}
	
	public LocalApiPlaceParameter setY(String y) {
		this.y = y;
		return this;
	}
	
	public String getRadius() {
		return radius;
	}
	
	public LocalApiPlaceParameter setRadius(String radius) {
		this.radius = radius;
		return this;
	}
	
	public String getPage() {
		return page;
	}
	
	public LocalApiPlaceParameter setPage(String page) {
		this.page = page;
		return this;
	}
	
	public String getSize() {
		return size;
	}
	
	public LocalApiPlaceParameter setSize(String size) {
		this.size = size;
		return this;
	}
	
	public String getSort() {
		return sort;
	}
	
	public LocalApiPlaceParameter setSort(String sort) {
		this.sort = sort;
		return this;
	}
	
	public String getRect() {
		return rect;
	}
	
	public LocalApiPlaceParameter setRect(String rect) {
		this.rect = rect;
		return this;
	}
	
	public Map<String, String> getParameterMap()
	{
		Map<String, String> map = new HashMap<>();
		
		if (query != null)
		{
			map.put("query", query);
		}
		if (categoryGroupCode != null)
		{
			map.put("category_group_code", categoryGroupCode);
		}
		if (x != null)
		{
			map.put("x", x);
		}
		if (y != null)
		{
			map.put("y", y);
		}
		if (radius != null)
		{
			map.put("radius", radius);
		}
		if (page != null)
		{
			map.put("page", page);
		}
		if (size != null)
		{
			map.put("size", size);
		}
		if (sort != null)
		{
			map.put("sort", sort);
		}
		if (rect != null)
		{
			map.put("rect", rect);
		}
		return map;
	}
}