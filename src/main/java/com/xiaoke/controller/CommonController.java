package com.xiaoke.controller;

import com.xiaoke.common.CustomException;
import com.xiaoke.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //参数名必须与表单名相同
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除

        //原始文件名
        String originalFilename=file.getOriginalFilename();
        String suffix=null;
        if(originalFilename!=null){
            suffix=originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        //使用uuid重新生成文件名，防止文件名称重复而导致覆盖
        String fileName= UUID.randomUUID().toString().replace("-","")+suffix;

        //创建一个目录对象

        File dir=new File(basePath);
        if(!dir.exists()) {
            boolean tmp=dir.mkdirs();
            if(!tmp){
                throw new CustomException("创建目录失败");
            }else {
                log.info("创建目录成功");
            }
        }

        try {
            file.transferTo(new File(basePath + fileName));
        }catch (IOException e){
            log.error(e.getMessage());
        }
        return  R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            ServletOutputStream outputStream = response.getOutputStream();

            String suffix=name.substring(name.lastIndexOf("."));

            response.setContentType("image/"+suffix.substring(1));

            int len=0;
            byte[] bytes = new byte[1024];
            while(len!=-1){
                len=fileInputStream.read(bytes);
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            outputStream.close();
            fileInputStream.close();
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
