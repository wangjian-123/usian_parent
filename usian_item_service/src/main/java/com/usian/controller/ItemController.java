package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.ItemService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/service/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 查询商品
     * @param itemId
     * @return Tbitem
     */
    @RequestMapping("/selectItemInfo")
    public TbItem selectItemInfo(Long itemId){
        return itemService.selectItemInfo(itemId);
    }

    /**
     * 查询所有商品，并分页。
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectTbItemAllByPage")
    public PageResult selectTbItemAllByPage(Integer page,Integer rows){
        return itemService.selectTbItemAllByPage(page,rows);
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/insertTbItem")
    public Integer insertItem(@RequestBody TbItem tbItem, String desc, String itemParams){
        return itemService.insertItem(tbItem,desc,itemParams);
    }

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/deleteItemById")
    public Integer deleteItemById(Long itemId){
        return itemService.deleteItemById(itemId);
    }

    /**
     * 修改查询
     * @param itemId
     * @return
     */
    @RequestMapping("/preUpdateItem")
    public Map<String,Object> preUpdateItem(Long itemId){
        return itemService.preUpdateItem(itemId);
    }

    /**
     * 商品修改
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/updateTbItem")
    public Integer updateTbItem(@RequestBody TbItem tbItem, String desc,String itemParams){
        return itemService.updateTbItem(tbItem,desc,itemParams);
    }
}
