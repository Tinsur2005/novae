package cn.tinsur.mall.controller;

import cn.tinsur.mall.util.AliOSSUtil;
import cn.tinsur.mall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/service")
public class UploadController {

    @Autowired
    private AliOSSUtil aliOSSUtil;

    /**
     * 文件上传到阿里云OSS
     * @param file 上传的文件
     * @param folder OSS里的目录前缀，不同功能传不同值：头像传avatar，商品图传product
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file, @RequestParam(required = false, defaultValue = "") String folder) {
        //7c45616c1e8740d987c41e95f33b9abe
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //a.png
        String filename = file.getOriginalFilename();
        System.out.println(filename);
        //.png
        String extension = filename.substring(filename.lastIndexOf("."));
        //avatar/7c45616c1e8740d987c41e95f33b9abe.png
        String newFilename = (folder.isEmpty() ? "" : folder + "/") + uuid + extension;
        String url = "";
        try {
            url = aliOSSUtil.uploadFile(newFilename, file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.ok("上传成功", url);
    }
}
