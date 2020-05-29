package com.usian.controller;

import com.usian.pojo.TbItemParam;
import com.usian.service.ItemParamService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/service/itemParam")
@RestController
public class ItemParamController {

    @Autowired
    private ItemParamService itemParamService;

    /**
     * 查询商品规格参数模板
     * @param itemCatId
     * @return
     */
    @RequestMapping("/selectItemParamByItemCatId/{itemCatId}")
    public TbItemParam selectItemParamByItemCatId(@PathVariable Long itemCatId){
        return itemParamService.selectItemParamByItemCatId(itemCatId);
    }

    /**
     * 规格参数查询
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectItemParamAll")
    public PageResult selectItemParamAll(Integer page,Integer rows){
        return itemParamService.selectItemParamAll(page,rows);
    }

    /**
     *商品规格模板删除
     * @param id
     * @return
     */
    @RequestMapping("/deleteItemParamById")
    public Integer deleteItemParamById(Integer id){
        return itemParamService.deleteItemParamById(id);
    }

    /**
     * 商品规格模板添加
     * @param itemCatId
     * @param paramData
     * @return
     */
    @RequestMapping("/insertItemParam")
    public Integer insertItemParam(Long itemCatId,String paramData){
        return itemParamService.insertItemParam(itemCatId,paramData);
    }
}
