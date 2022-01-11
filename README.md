# shanjupay
pay project
## 项目简介
闪聚支付项目主要是解决目前市面上第三方支付碎片化的问题，将第三方支付方式聚合到一起，提升用户支付体验。
## 项目环境
JDK-1.8；Maven-3.6.0；Nacos-2.0.3；Redis-2.8.9；RocketMQ-4.5.0；Dubbo-2.7
## 项目代码结构
* shanjupay：父工程，此项目最外层文件即是
* shanjupay-common：通用模块，存放工具类、结果类
* shanjupay-gateway：网关
* shanjupay-uaa：认证授权
* shanjupay-user：认证授权
* shanjupay-merchant-application：商户应用
* shanjupay-merchant：Dubbo服务类，商户服务相关
* shanjupay-transaction：Dubbo服务类，商户交易配置
* shanjupay-payment-agent：Dubbo服务类，支付业务相关
## 其他
* nacos_config.zip：存放Nacos配置中心相关配置
* test-freemarker：freemarker测试
* test-rocketmq：RocketMQ测试
