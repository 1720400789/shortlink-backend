package org.zj.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求入参
     * @return 短链接创建响应
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {});
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页短链接请求响应
     * @return 分页集合
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
        // 因为最后调用的是GET，不能跟上面一样直接传一整个对象RequestBody
        // 用Map集合装参数，调用hutool的HttpUtil.get时就会帮我们把Map序列化为Json字符串了
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);

        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
    }
}
