package com.usian.controller;

import com.usian.pojo.TbItemCat;
import com.usian.service.ItemCatService;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/service/itemCat")
@RestController
public class ItemCatController {

    @Autowired
    private ItemCatService itemCatService;

    /**
     * 商品类目查询
     * @param id
     * @return
     */
    @RequestMapping("/selectItemCategoryByParentId")
    public List<TbItemCat> selectItemCategoryByParentId(long id){
        return itemCatService.selectItemCategoryByParentId(id);
    }

    /**
     * 首页左侧商品分类查询
     * @return
     */
    @RequestMapping("/selectItemCategoryAll")
    public CatResult selectItemCategoryAll(){
        return itemCatService.selectItemCategoryAll();
    }
}
