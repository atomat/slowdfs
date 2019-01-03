# slowdfs
一个Java版（JavaSE-1.7及以上）的分布式文件服务，可运行在Tomcat等Web应用服务器中。

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
* webapp/files 文件存储路径。
* webapp/html 文件singlefile.html、multifile.html用浏览器上传文件的例子。
* webapp/tmpfiles 文件上传时，临时存放文件的目录。
* webapp/WEB-INF/logs 日志目录。*my.log*是通用日志输出文件，*health.log*是集群各个节点健康检查的日志。
* webapp/WEB-INF/conf 配置文件目录。*slowdfs.xml*是系统运行参数配置文件，通常**不用修改**。*slowdfshost.xml*是集群中所有节点（包括本节点自身）的配置，请根据实际情况修改。

## 使用说明
1. Maven build；
2. 将target目录下slowdfs目录复制到Tomcat的webapps目录下；
3. 修改slowdfshost.xml文件，设置slowdfs的所有节点。

## api说明
字符集均为：UTF-8    
* 文件上传接口：/upload/文件所属组；支持的方法：POST。说明：上传单个、多个文件到指定文件组下。    
接口返回内容：
```
{
	"result": "succ",  // 文件上传请求的成功失败标志。succ-成功，err-失败
	"uploadfiles": [{  // 文件上传结果，数组形式，支持上传多个文件
		"groupId": "default",  // 文件所属组
		"originalFileName": "testa.zip",  // 原始文件名
		"prefix": "zip",  // 文件名后缀
		"fileSize": 1001014,  // 文件大小
		"fileMD5Value": "65b58ce5a3803e69f3d548c86d65bf35",  // 文件内容的MD5值
		"fileId": "a05b46178c89e988d902de9f7312fc20",  // 文件ID（全局唯一）
		"fileName": "a05b46178c89e988d902de9f7312fc20.zip", // 在服务器端的文件名
		"downloadUrl": "/download/default/a05b46178c89e988d902de9f7312fc20.zip",  // 文件下载URL
		"storePathFile": "/6/b5/65b58ce5a3803e69f3d548c86d65bf35.zip",  // 文件在服务器端的存储路径
		"dateTime": "20180707 23:58:13.445 +0800",  // 文件上传时间
		"uploadStatus": true,  // 文件上传结果，boolean类型。true-上传成功，false-上传失败
		"msg": ""  // 当文件上传失败时，msg中存放的是失败原因
	}]
}
```
* 文件上传接口：/upload；支持的方法：POST。说明：上传单个、多个文件到default文件组下。
* 文件下载接口：/download/文件所属组/文件名称；支持的方法：GET、POST。说明：下载指定文件组下的文件。    
正常情况下返回文件流。报错时返回值为：{"result":"err","msg":"错误原因"}。
* 文件下载接口：/download/文件名称；支持的方法：GET、POST。说明：下载default文件组下的文件。
* 文件删除接口：/deletefile/文件所属组/文件ID；支持的方法：GET、POST。说明：删除指定文件组下的文件。    
删除成功时返回：{"result":"succ","msg":"……"}，报错时返回：{"result":"err","msg":"错误信息"}

## 问题
* 服务启动时出现“你的主机中的软件中止了一个已建立的连接”异常。     
存在多个slowdfs节点时，节点间的健康检查会引起这个异常。原因是某个节点服务启动后，该节点会对集群中所有节点发送健康检查请求，此时，后启动的节点在启动过程中收到健康检查的请求会导致“你的主机中的软件中止了一个已建立的连接”异常。    
通常可以忽略该情形导致的异常。
* 如何对下载文件的请求验证是否合法，例如必须是已登录的用户才能下载文件？
目前没有提供验证处理。但是可以通过实现一个Filter或者Spring interceptor对下载请求拦截处理。实现的逻辑包括：一、对/download/拦截，建议根据不同的文件分组进行验证，例如public组下的文件无需验证、private组下的文件必须验证用户身份。二、对/syncfile/拦截，确保下载请求的来源IP是slowdfs集群中某个节点的IP。

