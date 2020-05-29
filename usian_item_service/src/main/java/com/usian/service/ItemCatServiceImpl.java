package com.usian.service;

import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    /**
     * 查询商品类目
     * @param id
     * @return
     */
    @Override
    public List<TbItemCat> selectItemCategoryByParentId(long id) {
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(tbItemCatExample);
        return tbItemCatList;
    }

    /**
     * 首页左侧商品分类查询
     * @return
     */
    @Override
    public CatResult selectItemCategoryAll() {
        List<?> list = getCatNode(0L);
        CatResult catResult = new CatResult();
        catResult.setData(list);
        return catResult;
    }

    public List<?> getCatNode(Long parentId){
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List CatNodeList = new ArrayList();
        int count = 0;
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(tbItemCatExample);
        for (TbItemCat tbItemCat : tbItemCatList) {
            if(tbItemCat.getIsParent()){
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getCatNode(tbItemCat.getId()));
                CatNodeList.add(catNode);
                count++;
                if(count == 18){
                    break;
                }
            }else{
                CatNodeList.add(tbItemCat.getName());
            }
        }
        return CatNodeList;
    }
}
