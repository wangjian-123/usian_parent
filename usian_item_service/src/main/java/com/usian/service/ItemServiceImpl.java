package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemDescMapper;
import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemDesc;
import com.usian.pojo.TbItemExample;
import com.usian.pojo.TbItemParamItem;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private TbItemDescMapper tbItemDescMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

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
        pageResult.setPage(page);
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
        return tbItemNum+tbItemDescNum+tbItemParamItemNum;
    }
}
