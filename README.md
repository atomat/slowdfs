# slowdfs
一个Java版的分布式文件服务，可运行在Tomcat等Web应用服务器中。

## 目标
支持集群部署。   
集群中各个节点为对等节点，支持节点间文件自动同步，支持文件内容的MD5数字摘要。   
支持浏览器上传下载文件。   
提供Java版的文件上传下载SDK。   

## 设计原则
* 简单、轻量。采用简单直接的实现逻辑。
* 尽量使用成熟开源的组件，避免重复造轮子。
* 尽量让slowdfs持有文件，因此没有设计保证节点间文件删除一致性的机制。

## 目录&特殊文件说明
* webapp/files 文件存储路径
* webapp/html 文件singlefile.html、multifile.html用浏览器上传文件的例子
* webapp/tmpfiles 文件上传时，临时存放文件的目录
* webapp/WEB-INF/logs 日志目录。*my.log*是通用日志输出文件，*health.log*是集群各个节点健康检查的日志。
* webapp/WEB-INF/conf 配置文件目录。*slowdfs.xml*是系统运行参数配置文件，通常**不用修改**。*slowdfshost.xml*是集群中所有节点（包括本节点自身）的配置，请根据实际情况修改。

