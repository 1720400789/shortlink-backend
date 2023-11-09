package org.zj.shortlink.admin.common.serialize;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 手机号脱敏反序列化
 * Springboot默认通过Json的机制将响应结果序列化为Jackson字符串
 */
public class PhoneDesensitizationSerializer extends JsonSerializer<String> {

    /**
     * @param phone 对象中需要处理的值
     * @param jsonGenerator 序列化器
     * @param serializerProvider
     * @throws IOException
     */
    @Override
    public void serialize(String phone, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // mobilePhone时hutool工具类中封装好的专门给手机号脱敏的方法，只展示前3位和后四位
        String phoneDesensitization = DesensitizedUtil.mobilePhone(phone);
        jsonGenerator.writeString(phoneDesensitization);
    }
}