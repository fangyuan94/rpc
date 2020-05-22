package com.fc.configCentre;

import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 *包装类
 */
@Getter
@Setter
public class ZookeeperConfigurationCLoseWarp {

    private NodeCache nodeCache;
}
