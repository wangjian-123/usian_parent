package com.usian.controller;

import com.usian.ContentServiceFeign;
import com.usian.pojo.TbContent;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/backend/content")
@RestController
public class ContentController {

    @Autowired
    private ContentServiceFeign contentServiceFeign;

    /**
     * 内容管理查询
     * @param page
     * @param rows
     * @param categoryId
     * @return
     */
    @RequestMapping("/selectTbContentAllByCategoryId")
    public Result selectTbContentAllByCategoryId(@RequestParam(defaultValue = "1")Integer page,
                                                 @RequestParam(defaultValue = "20")Integer rows,Long categoryId){
        PageResult pageResult = contentServiceFeign.selectTbContentAllByCategoryId(page,rows,categoryId);
        if(pageResult.getResult()!=null && pageResult.getResult().size()>0){
            return Result.ok(pageResult);
        }
        return Result.error("无结果");
    }

    /**
     * 内容管理添加
     * @param tbContent
     * @return
     */
    @RequestMapping("/insertTbContent")
    public Result insertTbContent(TbContent tbContent){
        Integer contentNum = contentServiceFeign.insertTbContent(tbContent);
        if(contentNum == 1){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 内容管理删除
     * @param ids
     * @return
     */
    @RequestMapping("/deleteContentByIds")
    public Result deleteContentByIds(Long ids){
        Integer contentNum = contentServiceFeign.deleteContentByIds(ids);
        if(contentNum == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }
}
