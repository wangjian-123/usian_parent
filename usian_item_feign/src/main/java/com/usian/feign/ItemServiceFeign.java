package com.usian.feign;

import com.usian.pojo.TbItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value="usian-item-service")
@RequestMapping("/service/item")
public interface ItemServiceFeign {

    @RequestMapping("/selectItemInfo")
    public TbItem selectItemInfo(@RequestParam Long itemId);
}
