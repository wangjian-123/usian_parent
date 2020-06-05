package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemCatMapper;
import com.usian.mapper.TbItemDescMapper;
import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.pojo.*;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private TbItemDescMapper tbItemDescMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 查询商品
     * @param itemId
     * @return Tbitem
     */
    @Override
    public TbItem selectItemInfo(Long itemId) {
        return tbItemMapper.selectByPrimaryKey(itemId);
    }

    /**
     * 查询所有商品，并分页。
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {

        PageHelper.startPage(page,rows);
        TbItemExample tbItemExample = new TbItemExample();
        tbItemExample.setOrderByClause("updated DESC");
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo((byte)1);
        List<TbItem> itemList = tbItemMapper.selectByExample(tbItemExample);
        for (TbItem tbItem : itemList) {
            tbItem.setPrice(tbItem.getPrice()/100);
        }
        PageInfo<TbItem> pageInfo = new PageInfo<>(itemList);
        PageResult pageResult = new PageResult();
        pageResult.setPageIndex(page);
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));
        pageResult.setResult(itemList);
        return pageResult;
    }

    /**
     * 商品添加
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer insertItem(TbItem tbItem, String desc, String itemParams) {
        //补齐 Tbitem 数据
        Long itemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        tbItem.setCreated(date);
        tbItem.setPrice(tbItem.getPrice()*100);
        Integer tbItemNum = tbItemMapper.insertSelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        Integer tbItemDescNum = tbItemDescMapper.insertSelective(tbItemDesc);

        //补齐商品规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setUpdated(date);
        Integer tbItemParamItemNum = tbItemParamItemMapper.insertSelective(tbItemParamItem);
        //发送消息到mq,同步索引库
        amqpTemplate.convertAndSend("item_exchage","item.add",itemId);
        return tbItemNum+tbItemDescNum+tbItemParamItemNum;
    }

    /**
     * 商品删除
     * @param itemId
     * @return
     */
    @Override
    public Integer deleteItemById(Long itemId) {
        Integer itemNum = tbItemMapper.deleteByPrimaryKey(itemId);
        Integer itemDescNum = tbItemDescMapper.deleteByPrimaryKey(itemId);
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        Integer itemParamItemNum = tbItemParamItemMapper.deleteByExample(tbItemParamItemExample);
        return itemDescNum+itemNum+itemParamItemNum;
    }

    /**
     * 修改查询
     * @param itemId
     * @return
     */
    @Override
    public Map<String,Object> preUpdateItem(Long itemId) {
        Map<String, Object> map = new HashMap<>();
        //根据商品 ID 查询商品
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        tbItem.setPrice(tbItem.getPrice()/100);
        map.put("item",tbItem);
        //根据商品 ID 查询商品描述
        TbItemDesc tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc",tbItemDesc.getItemDesc());
        //根据商品 ID 查询商品类目
        TbItemCat tbItemCat = tbItemCatMapper.selectByPrimaryKey(tbItem.getCid());
        map.put("itemCat",tbItemCat.getName());
        //根据商品 ID 查询商品规格参数
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> paramItemList = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        if(paramItemList!=null && paramItemList.size()>0){
            map.put("itemParam",paramItemList.get(0).getParamData());
        }
        return map;
    }

    /**
     * 修改商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer updateTbItem(TbItem tbItem, String desc, String itemParams) {
        //补齐 Tbitem 数据
        Date date = new Date();
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        tbItem.setPrice(tbItem.getPrice()*100);
        Integer itemNum = tbItemMapper.updateByPrimaryKeySelective(tbItem);
        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setUpdated(date);
        Integer itemDescNum = tbItemDescMapper.updateByPrimaryKeySelective(tbItemDesc);
        //补齐商品规格参数
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(tbItem.getId());
        List<TbItemParamItem> paramItemList = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(paramItemList.get(0).getId());
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(date);
        Integer itemParamNum = tbItemParamItemMapper.updateByPrimaryKeyWithBLOBs(tbItemParamItem);
        return itemNum+itemDescNum+itemParamNum;
    }
}
