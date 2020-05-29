package com.usian.controller;

import com.usian.pojo.TbContentCategory;
import com.usian.service.ContentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/contentCategory")
public class ContentCategoryController {

    @Autowired
    private ContentCategoryService contentCategoryService;

    /**
     * 内容分类管理查询
     * @param id
     * @return
     */
    @RequestMapping("/selectContentCategoryByParentId")
    public List<TbContentCategory> selectContentCategoryByParentId(Long id){
        return contentCategoryService.selectContentCategoryByParentId(id);
    }

    /**
     * 内容分类管理添加
     * @param contentCategory
     * @return
     */
    @RequestMapping("/insertContentCategory")
    public Integer insertContentCategory(@RequestBody TbContentCategory contentCategory){
        return contentCategoryService.insertContentCategory(contentCategory);
    }

    /**
     * 内容分类管理删除
     * @param categoryId
     * @return
     */
    @RequestMapping("/deleteContentCategoryById")
    public Integer deleteContentCategoryById(Long categoryId){
        return contentCategoryService.deleteContentCategoryById(categoryId);
    }

    /**
     * 内容分类管理修改
     * @param tbContentCategory
     * @return
     */
    @RequestMapping("/updateContentCategory")
    public Integer updateContentCategory(@RequestBody TbContentCategory tbContentCategory){
        return contentCategoryService.updateContentCategory(tbContentCategory);
    }
}
