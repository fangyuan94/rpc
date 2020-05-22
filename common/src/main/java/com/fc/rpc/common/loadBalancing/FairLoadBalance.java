package com.fc.rpc.common.loadBalancing;

import com.fc.rpc.common.handler.RPCClientHandler;
import com.fc.rpc.common.mapper.RPCMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author fangyuan
 */
@Slf4j
public class FairLoadBalance implements LoadBalance {

    private RPCMapper rpcMapper;

    public final static String FAIL_ZK_PATH_PREFIX = "/fail_zk_path/servers/";

    //产生随机数
    private final Random random = new Random();

    public FairLoadBalance(RPCMapper rpcMapper) {
        this.rpcMapper = rpcMapper;
    }

    @Override
    public RPCClientHandler choose() {


        if(rpcMapper.getMinResTimeSet().size() == 0 ){

            if(rpcMapper.getClientInstanceInfos().size()==0){
                throw  new RuntimeException(rpcMapper.getName()+"服务数据未准备完成==============>");
            }
            //从目前服务集合中 随机选取一个
            Integer wz = random.nextInt(rpcMapper.getRpcNettyClients().size());
            log.info("当前服务中无随机选取一个指定服务,服务集合为:",rpcMapper.getClientInstanceInfos());
            return rpcMapper.getRpcNettyClients().values().stream()
                    .collect(Collectors.toList()).get(wz).getRpcClientHandler();

        }else {
            Long firstScore = rpcMapper.getMinResTimeSet().first();
            //获取failServerStat
            Map<String, FairServerStat> map = rpcMapper.getMinResTimeServer().get(firstScore);
            String path;
            if(map.size() == 1 ){
                path = map.values().stream().findFirst().get().getPath();
                log.info("获取最短响应时间服务：{}",map);
            }else if(map.size() > 1){
                //对应的服务器中
                Integer wz = random.nextInt(map.size());
                path = map.values().stream().collect(Collectors.toList()).get(wz).getPath();
                log.info("存在相同最短响应时间{}服务随机选取：{}",map,path);
            }else {
                throw  new RuntimeException(rpcMapper.getName()+"服务数据未准备完成==============>");
            }
            return rpcMapper.getRpcNettyClients().get(path).getRpcClientHandler();
        }

    }
}
