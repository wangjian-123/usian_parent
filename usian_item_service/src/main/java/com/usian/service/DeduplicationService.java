package com.usian.service;

import com.usian.pojo.DeDuplication;

public interface DeduplicationService {
    DeDuplication selectDeDuplicationByTxNo(String txNo);

    void insertDeDuplication(String txNo);
}
