package com.sina.data.rest;

import com.sina.data.rest.api.AllMetrics;
import com.sina.data.rest.api.ClustersWithHostsMap;
import com.sina.data.rest.api.CompareAmongHosts;
import com.sina.data.rest.api.CompareWithLast;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Created by lile1 on 2015/12/1.
 */
public class RestApplication extends Application {


    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        //2.1	获取集群名和下属机器的对应关系
        router.attach("/clustersAndHosts",ClustersWithHostsMap.class);
        //2.2	获取集群所有的metric
        router.attach("/allMetrics",AllMetrics.class);
        //2.3	查看某一时间段内所选指标在不同机器之间的的对比值
        router.attach("/view_compareAmongHost",CompareAmongHosts.class);
        //2.4	查看所选机器的指标，与过去某个时间段的环比值
        router.attach("/view_compareWithLast",CompareWithLast.class);

        return router;
    }
}
