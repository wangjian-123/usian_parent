package com.usian.controller;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.usian.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/file")
@RestController
public class FileUploadController {

    @Autowired
    private FastFileStorageClient storageClient;

    private static final List<String> contentTypeList = Arrays.asList("image/jpeg", "image/gif");

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {

        //校验文件类型
        if(!contentTypeList.contains(file.getContentType())){
            return Result.error("文件类型不符合");
        }
        //校验文件内容
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        if(bufferedImage == null){
            return Result.error("文件内容不合法");
        }
        //上传文件
        String filename = file.getOriginalFilename();
        String afterLast = StringUtils.substringAfterLast(filename,".");
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), afterLast, null);
        //返回图片url:http://image.usian.com/group1/M00/00/00/wKj1hF7D31mARMtvAAJCArXZ1yM180.jpg
        return Result.ok("http://image.usian.com/"+storePath.getFullPath());
    }
}
