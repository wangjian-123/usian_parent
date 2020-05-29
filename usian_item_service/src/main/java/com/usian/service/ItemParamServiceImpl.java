package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemParamMapper;
import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamExample;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {

    @Autowired
    private TbItemParamMapper tbItemParamMapper;

    /**
     * 查询商品规格参数模板
     * @param itemCatId
     * @return
     */
    @Override
    public TbItemParam selectItemParamByItemCatId(Long itemCatId) {
        TbItemParamExample tbItemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = tbItemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> list = tbItemParamMapper.selectByExampleWithBLOBs(tbItemParamExample);
        if(list!=null && list.size()>0){

            return list.get(0);
        }
        return null;
    }

    /**
     * 规格模板参数查询
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectItemParamAll(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        TbItemParamExample tbItemParamExample = new TbItemParamExample();
        tbItemParamExample.setOrderByClause("updated DESC");
        List<TbItemParam> paramList = tbItemParamMapper.selectByExampleWithBLOBs(tbItemParamExample);
        PageInfo<TbItemParam> pageInfo = new PageInfo<>(paramList);
        PageResult pageResult = new PageResult();
        pageResult.setResult(pageInfo.getList());
        pageResult.setPageIndex(pageInfo.getPageNum());
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));
        return pageResult;
    }

    /**
     * 商品规格模板删除
     * @param id
     * @return
     */
    @Override
    public Integer deleteItemParamById(Integer id) {

        Integer itemParamNum = tbItemParamMapper.deleteByPrimaryKey(id.longValue());
        if(itemParamNum == 1){
            return itemParamNum;
        }
        return null;
    }

    /**
     * 商品规格模板添加
     * @param itemCatId
     * @param paramData
     * @return
     */
    @Override
    public Integer insertItemParam(Long itemCatId, String paramData) {
        TbItemParamExample tbItemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = tbItemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> itemParamList = tbItemParamMapper.selectByExampleWithBLOBs(tbItemParamExample);
        if(itemParamList.size()>0){
            return 0;
        }
        TbItemParam tbItemParam = new TbItemParam();
        Date date = new Date();
        tbItemParam.setUpdated(date);
        tbItemParam.setCreated(date);
        tbItemParam.setParamData(paramData);
        tbItemParam.setItemCatId(itemCatId);
        return tbItemParamMapper.insertSelective(tbItemParam);
    }
}
