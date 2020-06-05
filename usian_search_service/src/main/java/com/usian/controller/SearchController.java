package com.usian.controller;

import com.usian.pojo.SearchItem;
import com.usian.service.SearchItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/searchItem")
public class SearchController {

    @Autowired
    private SearchItemService searchItemService;

    @RequestMapping("/importAll")
    public Boolean importAll(){
        return searchItemService.importAll();
    }

    @RequestMapping("/list")
    public List<SearchItem> list(String q,Long page,Integer rows){
        return searchItemService.list(q,page,rows);
    }
}
