package com.usian.controller;

import com.usian.service.SearchItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/searchItem")
public class SearchController {

    @Autowired
    private SearchItemService searchItemService;

    @RequestMapping("/importAll")
    public Boolean importAll(){
        return searchItemService.importAll();
    }
}
