package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jack
 * @create 2018-11-29 10:21
 */
@Slf4j
@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private UploadProperties uploadProperties;//使用@EnableConfigurationProperties所以可以注入，否则需要使用@Component
    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {
        //校验文件的类型
        String contentType = file.getContentType();
        if(!uploadProperties.getAllowTypes().contains(contentType)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image==null){//文件不是image类型
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            log.error("校验文件出现异常！",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
        //进行上传
        String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
        try {
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            return uploadProperties.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            log.error("上传文件出现异常！",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
