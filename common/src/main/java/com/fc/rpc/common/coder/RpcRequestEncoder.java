package com.fc.rpc.common.coder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fc.rpc.common.pojo.req.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * 编码器
 * @author fangyuan
 */
public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        //转化为byte[]
        byte[] bytes = JSON.toJSONBytes(rpcRequest);
         byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
