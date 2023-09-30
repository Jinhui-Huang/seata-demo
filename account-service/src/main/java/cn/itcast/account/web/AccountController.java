package cn.itcast.account.web;

import cn.itcast.account.handler.VideoHttpRequestHandler;
import cn.itcast.account.service.AccountService;
import cn.itcast.account.service.AccountTCCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 虎哥
 */
@RestController

@RequestMapping("account")
public class AccountController {

    @Autowired
    private AccountTCCService accountService;

    @Autowired
    private VideoHttpRequestHandler videoHttpRequestHandler;

    @PutMapping("/{userId}/{money}")
    public ResponseEntity<Void> deduct(@PathVariable("userId") String userId, @PathVariable("money") Integer money){
        accountService.deduct(userId, money);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/play")
    public void getPlayResource(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Path path = Paths.get("/home/huian/Videos/test.mp4");
        if (Files.exists(path)) {
            String mimeType = Files.probeContentType(path);
            if (!StringUtils.isEmpty(mimeType)) {
                response.setContentType(mimeType);
            }
            request.setAttribute(VideoHttpRequestHandler.ATTR_FILE, path);
            videoHttpRequestHandler.handleRequest(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        }
    }

}
