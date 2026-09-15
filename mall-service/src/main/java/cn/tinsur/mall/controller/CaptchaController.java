package cn.tinsur.mall.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import cn.tinsur.mall.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/service")
public class CaptchaController {
    @Autowired
    private DefaultKaptcha defaultKaptcha;
    @Autowired
    private RedisTemplate redisTemplate;


    //图片以json形式返回
    @GetMapping("/captcha")
    public Result captcha(HttpServletResponse response) throws IOException {
        System.out.println("KaptchaController.kaptcha");
        String captcha = defaultKaptcha.createText();
        //redis存储
        String uuid = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("captcha:" + uuid, captcha, 30, TimeUnit.SECONDS);

        BufferedImage image = defaultKaptcha.createImage(captcha);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        String base64Code = Base64.encodeBase64String(out.toByteArray());
        Map<String, Object> map = new HashMap<>();
        map.put("captcha", "data:image/png;base64," + base64Code);
        map.put("uuid", uuid);
        return Result.ok("", map);
    }

    @GetMapping("/captcha1")
    public void captcha(HttpSession session, HttpServletResponse response) throws IOException {
        String captcha = defaultKaptcha.createText();
        session.setAttribute("captcha", captcha);
        BufferedImage image = defaultKaptcha.createImage(captcha);
        // 禁止浏览器缓存，保证每次刷新都能拿到新图
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        ImageIO.write(image, "jpg", response.getOutputStream());
    }
}