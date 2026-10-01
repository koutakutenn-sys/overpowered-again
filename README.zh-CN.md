# Overpowered Again

[English](README.md) · **简体中文** · [繁體中文](README.zh-HK.md)

##### 再度君临

一个面向 **Minecraft 26.2** 的小型 Fabric 模组，重新定义了附魔金苹果。

吃下附魔金苹果后将获得：

| 效果 | 等级 | 持续时间 |
| --- | --- | --- |
| 生命恢复 | IV | 30 秒 |
| 伤害吸收 | IV | 2 分钟 |
| 抗性提升 | I | 5 分钟 |
| 抗火 | I | 5 分钟 |

在原版 26.2 中，后三项已经是这些数值；本模组改的是生命恢复——从 II 级 20 秒变为 IV 级 30 秒。
这里把四项完整列出，是为了在原版数值变动时行为不会悄悄偏移。

## 进度（成就）

模组新增一个进度：

- **名字：** Overpowered Again
- **描述：** 吃下一个附魔金苹果
- 吃下附魔金苹果时解锁

## 实现方式

自 1.21.2 起，可食用物品的效果存放在 `minecraft:consumable` 物品组件上，类型是一组
consume effect，由 `Consumable#onConsume` 通过 `onConsumeEffects.forEach(...)` 应用。
`ConsumableMixin` 只重定向了这一处调用点（且仅针对附魔金苹果），因此食用的其他环节完全保持原版行为：
粒子、音效、统计、`consume_item` 进度触发器以及物品消耗本身。

目标数值只写在一个地方：
`src/main/java/com/koutakutenn/overpoweredagain/OverpoweredEffects.java`。

## 环境要求

- Minecraft 26.2
- Fabric Loader 0.19.5 或更高
- Java 25 或更高
- 无需另外安装 Fabric API：jar 内已内嵌 Fabric Resource Loader v1 与 Fabric API Base，
  用于加载本模组的数据与语言资源。

## 安装

把 jar 放进实例的 `mods` 目录后重启游戏。模组不支持热加载，必须完全重启。

## 构建

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
./gradlew build
```

产物位于 `build/libs/`。

## 测试

```sh
./gradlew runIntegrationTest -PacceptMinecraftEula=true
```

这会启动一个真实的服务端，检查四种效果、被替换掉的原版数值、自定义进度及其物品谓词。
每次运行都会使用 Gradle 打印出的全新目录，结果写入
`run-test/run-<时间戳>/test-result.txt`，旧结果会保留。

检查结束后，测试通过 `halt(false)` 请求正常关闭服务端，回到服务端主循环，而不是在服务端线程里调用
`System.exit`。

## 许可

本模组以 [Unlicense](https://unlicense.org) 释放到公共领域，Fabric 元数据即以它作为首选许可；
如果你更愿意，也可以改用 [MIT 许可](https://opensource.org/license/mit)。

见 [LICENSE](LICENSE) 与 [LICENSE-MIT](LICENSE-MIT)。
