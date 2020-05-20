package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItemParam;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/backend/itemParam")
@RestController
public class ItemParamController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    /**
     * 查询商品规格参数
     * @param itemCatId
     * @return
     */
    @RequestMapping("/selectItemParamByItemCatId/{itemCatId}")
    public Result selectItemParamByItemCatId(@PathVariable Long itemCatId){
        TbItemParam tbItemParam = itemServiceFeign.selectItemParamByItemCatId(itemCatId);
        if(tbItemParam!=null){
            return Result.ok(tbItemParam);
        }
        return Result.error("无结果");
    }

    /**
     * 规格参数查询
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectItemParamAll")
    public Result selectItemParamAll(@RequestParam(defaultValue = "1")Integer page,
                                     @RequestParam(defaultValue = "100")Integer rows){
        PageResult pageResult = itemServiceFeign.selectItemParamAll(page,rows);
        if(pageResult.getResult() != null && pageResult.getResult().size()>0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     *商品规格模板删除
     * @param id
     * @return
     */
    @RequestMapping("/deleteItemParamById")
    public Result deleteItemParamById(Integer id){
        Integer itemParamNum = itemServiceFeign.deleteItemParamById(id);
        if(itemParamNum == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

    /**
     * 商品规格模板添加
     * @param itemCatId
     * @param paramData
     * @return
     */
    @RequestMapping("/insertItemParam")
    public Result insertItemParam(Long itemCatId,String paramData){
        Integer itemParamNum = itemServiceFeign.insertItemParam(itemCatId,paramData);
        if(itemParamNum == 1){
            return Result.ok();
        }
        return Result.error("添加失败");
    }
}
