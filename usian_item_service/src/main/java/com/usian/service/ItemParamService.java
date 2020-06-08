package com.usian.service;

import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamItem;
import com.usian.utils.PageResult;

public interface ItemParamService {
    TbItemParam selectItemParamByItemCatId(Long itemCatId);

    PageResult selectItemParamAll(Integer page, Integer rows);

    Integer deleteItemParamById(Integer id);

    Integer insertItemParam(Long itemCatId, String paramData);

    TbItemParamItem selectTbItemParamItemByItemId(Long itemId);
}
