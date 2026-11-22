# 测试证据

Trae 集成终端启动 Spring Boot 的演示端口为 `18086`。逐项结果图来自同一构建产物在 `18085` 上的独立演示。源码默认端口为 `8080`。

[Trae 启动截图](01-trae-start.png)显示 Tomcat 启动与 `Started AgriApplication`。接口结果图由 Postman CLI 的真实本地运行输出生成，保留实际请求、HTTP 状态、JSON 响应和对应断言。两次验收均通过，每次执行 11 次请求、22 条断言，失败数为 0。[完整 Postman 运行记录](postman-run.txt) · [在 Trae 中复跑的终端截图](11-postman-cli-terminal.png)

| 用例 | 查看结果 | 核验点 |
| --- | --- | --- |
| 新增品种 | [02](02-postman-add.png) | HTTP 200，返回 ID 和两个时间字段 |
| 修改品种 | [03](03-postman-update.png) | HTTP 200，生长周期从 120 改为 135 天 |
| 按 ID 查询 | [04](04-postman-detail.png) | HTTP 200，读到修改后的品种 |
| 分页 | [05](05-postman-pagination.png) | 总计 2 条，查询第 2 页，每页 1 条 |
| 四条件动态查询 | [06](06-postman-multi-filter.png) | 名称和品种模糊匹配，类型和状态精确匹配 |
| 查询参数全部省略 | [07](07-postman-no-filter.png) | 两条品种均可查询，使用默认分页 |
| DTO 校验失败 | [08](08-postman-validation-400.png) | 生长周期为 0，返回 HTTP 400 |
| 逻辑删除 | [09](09-postman-delete.png) | HTTP 200，`data` 为 `null` |
| 已删除品种查询 | [10](10-postman-business-404.png) | 返回 HTTP 404 和友好业务提示 |

[Maven 集成测试摘要](maven-test-summary.txt)对应 `CropApiTest` 的两个测试。它们通过真实 HTTP 请求验证常规流程与错误流程，其中数据库表暂时不可用的用例断言 HTTP 500、统一错误文案及 `data: null`。
