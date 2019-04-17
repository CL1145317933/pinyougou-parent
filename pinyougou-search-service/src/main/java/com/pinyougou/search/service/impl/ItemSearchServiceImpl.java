package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Service(timeout=5000)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map<String,Object> search(Map searchMap) {
		//关键字空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords",keywords.replace(" ",""));
		Map<String,Object> map=new HashMap();
		//按关键字查询
		map.putAll(searchList(searchMap));
		//根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList",categoryList);

		String categoryName = (String) searchMap.get("category");//获取分类名称
		//根据分类查询品牌和规格列表
		if (!"".equals(categoryName)){//分类有名称
			map.putAll(searchBrandAndSpecList(categoryName));
		}else{ //没有分类名称,第一个先查
			if (categoryList.size()>0){
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}

		return map;
	}

	@Override
	public void importList(List<TbItem> itemList) {
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsId(Long[] goodsIds) {
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);

		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

	private Map searchList(Map searchMap) {
		Map map = new HashMap();

		//关键字高亮查询
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置需要高亮查询的域

		highlightOptions.setSimplePrefix("<em style='color:red'>");//设置高亮前缀
		highlightOptions.setSimplePostfix("</em>");//设置高亮后缀

		query.setHighlightOptions(highlightOptions);//设置高亮选项
		//根据关键字查询
		Criteria criteria =  new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		//按分类查询
		if (!"".equals(searchMap.get("category"))){
			Criteria categoryCri = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(categoryCri);
			query.addFilterQuery(filterQuery);
		}

		//按品牌查询
		if (!"".equals(searchMap.get("brand"))){
			Criteria brandCri = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(brandCri);
			query.addFilterQuery(filterQuery);
		}

		//按规格查询
		if (searchMap.get("spec")!=null){
			Map <String,String> specMap = (Map) searchMap.get("spec");

			for (String key : specMap.keySet()) {
				Criteria specCri = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(specCri);
				query.addFilterQuery(filterQuery);
			}
		}

		//按价格查询
		if (!"".equals(searchMap.get("price"))){
			String price = (String) searchMap.get("price");
			String[] strs = price.split("-");

			if (!strs[0].equals("0")){//如果起点不等于0
				Criteria criteriaTop = new Criteria("item_price").greaterThanEqual(strs[0]);
				FilterQuery filterQuery = new SimpleFilterQuery(criteriaTop);
				query.addFilterQuery(filterQuery);
			}

			if (!strs[1].equals("*")) {//如果末尾不为*
				Criteria criteriaEnd = new Criteria("item_price").lessThanEqual(strs[1]);
				FilterQuery filterQuery = new SimpleFilterQuery(criteriaEnd);
				query.addFilterQuery(filterQuery);
			}
		}

		//分页查询
		Integer pageNum = (Integer) searchMap.get("pageNum");//获取当前页码
		if (pageNum==null) {
			pageNum=0;//默认从第一页
		}

		Integer pageSize = (Integer) searchMap.get("pageSize");//获取每页记录数
		if (pageSize==null){
			pageSize=20;//默认记录数为 20
		}
		query.setOffset((pageNum-1)*pageSize);//从第几条记录开始查询
		query.setRows(pageSize);//设置每页的记录数

		//排序
		String sortValue = (String) searchMap.get("sortValue");//获取前台的排序数据 DESC/ASC
		String sortField = (String) searchMap.get("sortField");//获取排序字段
		if (sortValue!=null&&!sortValue.equals("")) {
			if (sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}

			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}

		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		for (HighlightEntry<TbItem> entry : page.getHighlighted()) {
			//获取原实体类
			TbItem item = entry.getEntity();

			if (entry.getHighlights().size()>0 && entry.getHighlights().get(0).getSnipplets().size()>0) {
				item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));//设置高亮效果
			}
		}
		map.put("rows",page.getContent());
		map.put("totalPages",page .getTotalPages());
		map.put("total",page.getTotalElements());
		return map;
	}

	/**
	 * 查询分类列表
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap){
		List<String> list = new ArrayList<>();

		Query query = new SimpleQuery();
		//根据关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		//设置分组选项
		GroupOptions options = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(options);

		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

		//根据字段名得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();

		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());
		}
		return list;
	}

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询品牌和规格信息
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap();
		//获取模板Id
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (typeId!=null) {
			//根据模板Id查询品牌
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList",brandList);

			//根据模板Id查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList",specList);
		}

		return map;

	}

}
