package com.usian.service;

import com.usian.mapper.TbContentCategoryMapper;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;

    /**
     * 内容分类管理查询
     * @param id
     * @return
     */
    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbContentCategory> tbContentCategoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);

        return tbContentCategoryList;
    }

    /**
     * 内容分类管理添加
     * @param contentCategory
     * @return
     */
    @Override
    public Integer insertContentCategory(TbContentCategory contentCategory) {
        Date date = new Date();
        contentCategory.setUpdated(date);
        contentCategory.setCreated(date);
        contentCategory.setIsParent(false);
        contentCategory.setStatus(1);
        contentCategory.setSortOrder(1);
        Integer contentCategoryNum = tbContentCategoryMapper.insertSelective(contentCategory);
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(contentCategory.getParentId());
        if(!tbContentCategory.getIsParent()){
           tbContentCategory.setIsParent(true);
           tbContentCategory.setUpdated(date);
           tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
        }
        return contentCategoryNum;
    }

    /**
     * 内容分类管理删除
     * @param categoryId
     * @return
     */
    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        if(tbContentCategory.getIsParent()){
           return 0;
        }

        Integer contentCategoryNum = tbContentCategoryMapper.deleteByPrimaryKey(categoryId);

        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(tbContentCategory.getParentId());
        List<TbContentCategory> tbContentCategoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        if(tbContentCategoryList.size() == 0){
            TbContentCategory parentContentCategory = new TbContentCategory();
            parentContentCategory.setId(tbContentCategory.getParentId());
            parentContentCategory.setIsParent(false);
            parentContentCategory.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKeySelective(parentContentCategory);
        }

        return contentCategoryNum;
    }

    /**
     * 内容分类管理修改
     * @param tbContentCategory
     * @return
     */
    @Override
    public Integer updateContentCategory(TbContentCategory tbContentCategory) {
        tbContentCategory.setUpdated(new Date());
        Integer contentCategoryNum = tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
        return contentCategoryNum;
    }
}
