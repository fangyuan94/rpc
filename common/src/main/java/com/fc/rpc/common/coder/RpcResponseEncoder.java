package com.fc.rpc.common.coder;

import com.alibaba.fastjson.JSON;
import com.fc.rpc.common.pojo.reps.RpcResponse;
import com.fc.rpc.common.pojo.req.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 * @author fangyuan
 */
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        //转化为byte[]
        byte[] bytes = JSON.toJSONBytes(rpcResponse);
         byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
