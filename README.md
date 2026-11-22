# 农作物品种信息管理

本项目对应「基于 Trae 的 SpringBoot 综合开发实训作业」选项 A。系统提供农作物品种的新增、修改、详情、逻辑删除和分页筛选接口。示例品种和亩产数据仅用于接口测试。

[GitHub 开源仓库](https://github.com/uhhgxg/agri-crop-management) · MIT 许可证

## 技术栈与环境

| 组件 | 版本或用途 |
| --- | --- |
| Java | JDK 17+，实测 JDK 24.0.2 |
| Maven | 3.6.3+，实测 3.9.11 |
| Spring Boot | 4.2.0，提供 Web MVC 与 Jakarta Bean Validation |
| MyBatis-Plus | 3.5.18.1，使用 Spring Boot 4 Starter 与 JSqlParser 分页模块 |
| H2 | 2.5.252，使用本地文件保存业务数据 |
| SLF4J + Logback | 2.0.20 / 1.6.6，输出至控制台和 `logs/agri.log` |

[Spring Boot 4.2.0 发布说明](https://spring.io/blog/2026/11/19/spring-boot-4-2-0-available-now/) · [MyBatis-Plus 3.5.18.1 发布说明](https://github.com/baomidou/mybatis-plus/releases/tag/v3.5.18.1)

## 用 Trae 启动

在 Trae 里打开包含 `pom.xml` 的项目目录，再打开终端。用 `mvn -v` 确认 Maven 正在使用 JDK 17 或更新的版本。Windows 上如果显示 JDK 8，可把 `JAVA_HOME` 指向较新的 JDK 安装目录，再重新打开终端。

**依赖前置知识清单** · PowerShell、Maven 项目构建、JDK 的 `JAVA_HOME`。

```powershell
mvn -v
mvn spring-boot:run
```

当终端出现 `Started AgriApplication` 后，请求 `http://127.0.0.1:8080/crop/list`。首次启动会执行 `src/main/resources/schema.sql`，并在 `data/` 下创建数据库。列表初始为空。服务重启后，先前录入的品种仍能查询。如果 8080 端口被占用，可以在启动前执行 `$env:SERVER_PORT = '8081'`，并同步更新 Postman 的 `baseUrl`。

## 接口说明

新增和修改请求使用 `Content-Type: application/json`。成功响应统一包含 `code`、`msg`、`data`，HTTP 状态与 `code` 一致。

| 功能 | 请求 | 关键输入 | 成功返回的 `data` |
| --- | --- | --- | --- |
| 新增品种 | `POST /crop/add` | `CropDTO`，由数据库生成 ID | 新品种的 `CropVO` |
| 修改品种 | `PUT /crop/update` | 完整的 `CropDTO`，含正整数 `id` | 修改后的 `CropVO` |
| 品种详情 | `GET /crop/{id}` | 正整数 `id` | 对应的 `CropVO` |
| 逻辑删除 | `DELETE /crop/{id}` | 正整数 `id` | `null` |
| 分页筛选 | `GET /crop/list` | 下表的可选参数 | `PageResult<CropVO>` |

| 列表参数 | 规则 |
| --- | --- |
| `pageNum` | 正整数，从 1 开始，默认 1 |
| `pageSize` | 1–100，默认 10 |
| `cropName`、`varietyName` | 模糊匹配，最多 50 个字符 |
| `cropType` | 精确匹配，支持粮食、蔬菜、水果、油料 |
| `plantStatus` | 精确匹配，0 停推，1 推广 |

不填筛选参数时按 ID 倒序列出可见品种。分页数据包含 `total` 总条数、`pages` 总页数、`pageNum` 当前页、`pageSize` 每页条数和 `records` 本页记录。超出末页时 `records` 为空。

### 新增请求示例

**依赖前置知识清单** · JSON 对象、数字与字符串、HTTP 请求体。

```json
{
  "cropName": "水稻",
  "varietyName": "试验稻一号",
  "cropType": "粮食",
  "suitArea": "长江中下游",
  "growthCycle": 120,
  "yieldLevel": 650.50,
  "plantStatus": 1
}
```

新增成功后，从 `data.id` 取得 ID。修改时在同样的请求体中加入这个 ID，并提交所有业务字段。`growthCycle` 使用天数，范围 1–3650。`yieldLevel` 使用千克每亩，范围 0–100000，最多保留两位小数。名称和地区必填，地区最多 120 字符。

### 响应示例

**依赖前置知识清单** · HTTP 状态码、JSON 的 `null`。

```json
{"code": 200, "msg": "操作成功", "data": null}
```

上面的响应对应删除成功。将新增请求中的生长周期改成 `0` 会得到 HTTP 400。

**依赖前置知识清单** · JSON 响应、表单字段校验。

```json
{"code": 400, "msg": "生长周期不能小于1天", "data": null}
```

记录不存在或已删除时返回 HTTP 404。未预期的异常返回 HTTP 500，服务端 `error` 日志保留异常堆栈。

## 工程结构与实现说明

| 包 | 职责 |
| --- | --- |
| `entity` | `Crop` 对应 `crop` 表，保存主键、业务字段、时间和内部删除标记。 |
| `dto` | `CropDTO` 接收新增或修改请求，用 Jakarta Validation 校验必填字段、长度、类型和数值。校验分组处理两类请求的 ID 规则。 |
| `vo` | `CropVO` 定义对外响应字段。详情、编辑和列表都转换为 VO，内部 `deleted` 列保留在 Entity。 |
| `mapper` | `CropMapper` 继承 MyBatis-Plus 的 `BaseMapper`，提供常用数据库操作。 |
| `service` | `CropServiceImpl` 组织数据库操作、动态筛选、Entity 与 VO 转换。 |
| `controller` | `CropController` 接收 HTTP 请求并封装响应。 |
| `config` | 注册分页插件与创建、修改时间的自动填充器。 |
| `exception` | `GlobalExceptionHandler` 统一处理参数校验、业务异常、HTTP 异常和未知异常。 |
| `common` | `Result` 封装响应，`PageResult` 封装分页数据。 |

- **动态查询**。Service 用 `LambdaQueryWrapper<Crop>` 逐项添加条件。文本参数含有非空白字符才参与查询，状态值为 `0` 时仍会按停推筛选。
- **分页**。`PaginationInnerInterceptor` 执行计数和分页，在数据库里只取请求页的记录。稳定的 ID 倒序让相同数据集的分页顺序一致。
- **时间与删除**。MyBatis-Plus 在新增时填充两个时间字段，在修改时更新 `updateTime`。删除把 `deleted` 置为 1，详情和列表自动过滤这些记录。
- **日志与异常**。Controller 记录通过校验的请求参数与响应摘要，Service 记录数据写入和列表查询结果。校验失败等异常由全局处理器统一记录堆栈并返回 JSON。

## 测试复现

在 Postman 中导入 [测试集合](postman/agri-crop.postman_collection.json)，按编号运行。集合会先创建两条测试数据，并把返回的 ID 保存为集合变量。最后删除测试记录。建议用空的演示数据库运行分页截图用例。

也可在另一个终端用官方 Postman CLI 运行本地集合。

**依赖前置知识清单** · Postman CLI、PowerShell、本地 HTTP 服务。

```powershell
postman collection run .\postman\agri-crop.postman_collection.json
```

验证暂时无法查询数据库所产生的 HTTP 500 时，Maven 集成测试会用独立内存库模拟表不可用，随后恢复测试表。测试同时覆盖校验失败、组合筛选、时间填充和逻辑删除标记。

**依赖前置知识清单** · Maven 测试生命周期、集成测试。

```powershell
mvn test
```

[查看测试证据](evidence/README.md)
