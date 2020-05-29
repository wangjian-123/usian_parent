package com.usian.controller;

import com.usian.ContentServiceFeign;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/content")
public class ContentCategoryController {

    @Autowired
    private ContentServiceFeign contentServiceFeign;

    /**
     * 内容分类管理查询
     * @param id
     * @return
     */
    @RequestMapping("/selectContentCategoryByParentId")
    public Result selectContentCategoryByParentId(@RequestParam(defaultValue = "0") Long id){
        List<TbContentCategory> tbContentCategoryList = contentServiceFeign.selectContentCategoryByParentId(id);
        if(tbContentCategoryList!=null && tbContentCategoryList.size()>0){
            return Result.ok(tbContentCategoryList);
        }
        return Result.error("查无结果");
    }

    /**
     * 内容分类管理添加
     * @param contentCategory
     * @return
     */
    @RequestMapping("/insertContentCategory")
    public Result insertContentCategory(TbContentCategory contentCategory){
        Integer contentCategoryNum = contentServiceFeign.insertContentCategory(contentCategory);
        if(contentCategoryNum == 1){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 内容分类管理删除
     * @param categoryId
     * @return
     */
    @RequestMapping("/deleteContentCategoryById")
    public Result deleteContentCategoryById(Long categoryId){
        Integer contentCategoryNum = contentServiceFeign.deleteContentCategoryById(categoryId);
        if(contentCategoryNum == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

    @RequestMapping("/updateContentCategory")
    public Result updateContentCategory(TbContentCategory tbContentCategory){
        Integer contentCategoryNum = contentServiceFeign.updateContentCategory(tbContentCategory);
        if(contentCategoryNum == 1){
            return Result.ok();
        }
        return Result.error("修改失败");
    }

    
}
