package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.*;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private TbItemDescMapper tbItemDescMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisClient redisClient;

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${BASE}")
    private String BASE;

    @Value("${DESC}")
    private String DESC;

    @Value("${PARAM}")
    private String PARAM;

    @Value("${ITEM_INFO_EXPIRE}")
    private Long ITEM_INFO_EXPIRE;

    @Value("${SETNX_BASC_LOCK_KEY}")
    private String SETNX_BASC_LOCK_KEY;

    @Value(("${SETNX_DESC_LOCK_KEY}"))
    private String SETNX_DESC_LOCK_KEY;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;


    /**
     * 查询商品
     * @param itemId
     * @return Tbitem
     */
    @Override
    public TbItem selectItemInfo(Long itemId) {
        TbItem tbItem = (TbItem) redisClient.get(ITEM_INFO+":"+itemId+":"+BASE);
        if(tbItem!=null){
            return tbItem;
        }
        /***解决缓存击穿（分布式锁）***/
        if(redisClient.setnx(SETNX_BASC_LOCK_KEY+":"+itemId,itemId,30L)){
            tbItem = tbItemMapper.selectByPrimaryKey(itemId);
            /***解决缓存穿透（缓存空数据）***/
            if(tbItem==null){
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO+":"+itemId+":"+BASE,null);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO+":"+itemId+":"+BASE,30L);

            }else{
                //把数据保存到缓存
                redisClient.set(ITEM_INFO+":"+itemId+":"+BASE,tbItem);
                //把数据保存到缓存
                redisClient.expire(ITEM_INFO+":"+itemId+":"+BASE,ITEM_INFO_EXPIRE);
            }
            //删除分布式锁
            redisClient.del(SETNX_BASC_LOCK_KEY+":"+itemId);
            return tbItem;
        }else{
            try {
                //获取锁失败
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemInfo(itemId);
        }

    }

    /**
     * 查询所有商品，并分页。
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {

        PageHelper.startPage(page,rows);
        TbItemExample tbItemExample = new TbItemExample();
        tbItemExample.setOrderByClause("updated DESC");
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo((byte)1);
        List<TbItem> itemList = tbItemMapper.selectByExample(tbItemExample);
        for (TbItem tbItem : itemList) {
            tbItem.setPrice(tbItem.getPrice()/100);
        }
        PageInfo<TbItem> pageInfo = new PageInfo<>(itemList);
        PageResult pageResult = new PageResult();
        pageResult.setPageIndex(page);
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));
        pageResult.setResult(itemList);
        return pageResult;
    }

    /**
     * 商品添加
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer insertItem(TbItem tbItem, String desc, String itemParams) {
        //补齐 Tbitem 数据
        Long itemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        tbItem.setCreated(date);
        tbItem.setPrice(tbItem.getPrice()*100);
        Integer tbItemNum = tbItemMapper.insertSelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        Integer tbItemDescNum = tbItemDescMapper.insertSelective(tbItemDesc);

        //补齐商品规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setUpdated(date);
        Integer tbItemParamItemNum = tbItemParamItemMapper.insertSelective(tbItemParamItem);
        //发送消息到mq,同步索引库
        amqpTemplate.convertAndSend("item_exchage","item.add",itemId);
        return tbItemNum+tbItemDescNum+tbItemParamItemNum;
    }

    /**
     * 商品删除
     * @param itemId
     * @return
     */
    @Override
    public Integer deleteItemById(Long itemId) {
        Integer itemNum = tbItemMapper.deleteByPrimaryKey(itemId);
        Integer itemDescNum = tbItemDescMapper.deleteByPrimaryKey(itemId);
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        Integer itemParamItemNum = tbItemParamItemMapper.deleteByExample(tbItemParamItemExample);
        redisClient.del(ITEM_INFO+":"+itemId+":"+BASE);
        redisClient.del(ITEM_INFO+":"+itemId+":"+DESC);
        redisClient.del(ITEM_INFO+":"+itemId+":"+PARAM);
        return itemDescNum+itemNum+itemParamItemNum;
    }

    /**
     * 修改查询
     * @param itemId
     * @return
     */
    @Override
    public Map<String,Object> preUpdateItem(Long itemId) {
        Map<String, Object> map = new HashMap<>();
        //根据商品 ID 查询商品
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        tbItem.setPrice(tbItem.getPrice()/100);
        map.put("item",tbItem);
        //根据商品 ID 查询商品描述
        TbItemDesc tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc",tbItemDesc.getItemDesc());
        //根据商品 ID 查询商品类目
        TbItemCat tbItemCat = tbItemCatMapper.selectByPrimaryKey(tbItem.getCid());
        map.put("itemCat",tbItemCat.getName());
        //根据商品 ID 查询商品规格参数
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> paramItemList = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        if(paramItemList!=null && paramItemList.size()>0){
            map.put("itemParam",paramItemList.get(0).getParamData());
        }
        return map;
    }

    /**
     * 修改商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer updateTbItem(TbItem tbItem, String desc, String itemParams) {
        //补齐 Tbitem 数据
        Date date = new Date();
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        tbItem.setPrice(tbItem.getPrice()*100);
        Integer itemNum = tbItemMapper.updateByPrimaryKeySelective(tbItem);
        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setUpdated(date);
        Integer itemDescNum = tbItemDescMapper.updateByPrimaryKeySelective(tbItemDesc);
        //补齐商品规格参数
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(tbItem.getId());
        List<TbItemParamItem> paramItemList = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(paramItemList.get(0).getId());
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(date);
        Integer itemParamNum = tbItemParamItemMapper.updateByPrimaryKeyWithBLOBs(tbItemParamItem);
        redisClient.del(ITEM_INFO+":"+tbItem.getId()+":"+BASE);
        redisClient.del(ITEM_INFO+":"+tbItem.getId()+":"+DESC);
        redisClient.del(ITEM_INFO+":"+tbItem.getId()+":"+PARAM);
        return itemNum+itemDescNum+itemParamNum;
    }

    /**
     * 商品描述搜索
     * @param itemId
     * @return
     */
    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId) {
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(ITEM_INFO + ":" + itemId + ":" + DESC);
        if(tbItemDesc!=null){
            return tbItemDesc;
        }
        /***解决缓存击穿（分布式锁）***/
        if(redisClient.setnx(SETNX_DESC_LOCK_KEY+":"+itemId,itemId,30L)){
            tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
            /***解决缓存穿透（缓存空数据）***/
            if(tbItemDesc==null){
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,null);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,30L);
            }else{
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,tbItemDesc);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,ITEM_INFO_EXPIRE);
            }
            //删除分布式锁
            redisClient.del(SETNX_DESC_LOCK_KEY+":"+itemId);
            return tbItemDesc;
        }else{
            try {
                //获取锁失败
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemDescByItemId(itemId);
        }

    }

    /**
     * 扣减库存
     * @param orderId
     * @return
     */
    @Override
    public Integer updateTbItemByOrderId(String orderId) {
        TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = tbOrderItemExample.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        int result = 0;
        List<TbOrderItem> tbOrderItems = tbOrderItemMapper.selectByExample(tbOrderItemExample);
        for (TbOrderItem tbOrderItem : tbOrderItems) {
            TbItem tbItem = tbItemMapper.selectByPrimaryKey(Long.valueOf(tbOrderItem.getItemId()));
            tbItem.setNum(tbItem.getNum()-tbOrderItem.getNum());
            result += tbItemMapper.updateByPrimaryKeySelective(tbItem);
        }

        return result;
    }
}
