package cn.itcast.account.handler;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Description: VideoHttpRequestHandler
 * <br></br>
 * className: VideoHttpRequestHandler
 * <br></br>
 * packageName: cn.itcast.account.handler
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/9/29 10:01
 */
@Component
public class VideoHttpRequestHandler extends ResourceHttpRequestHandler {
    public final static String ATTR_FILE = "NON-STATIC-FILE";

    @Override
    protected Resource getResource(HttpServletRequest request) throws IOException {
        Path path = (Path) request.getAttribute(ATTR_FILE);
        return new FileSystemResource(path);
    }
}
