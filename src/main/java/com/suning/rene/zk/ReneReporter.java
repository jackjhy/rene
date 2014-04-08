package com.suning.rene.zk;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Created by tiger on 14-4-8.
 */
public class ReneReporter {

    ZooKeeper zk;

    private static final String ROOT_NODE = "/rene/nodes";

    public ReneReporter(String connectionString) throws IOException {
        zk = new ZooKeeper(connectionString,1000,new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {

            }
        });
    }

    public void createNode(String nodeName,String value) throws KeeperException, InterruptedException {
        zk.create(ROOT_NODE+"/"+nodeName,value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}
