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

## 合成表、成就与设置

默认恢复旧版合成表：普通苹果放中央，周围放 **8 个金块**，合成 **1 个附魔金苹果**。

| 金块 | 金块 | 金块 |
| --- | --- | --- |
| 金块 | 苹果 | 金块 |
| 金块 | 金块 | 金块 |

取得金块后会在配方书解锁本模组的配方。

### 成就自动切换

成就根据游戏实际加载的配方切换，不仅取决于本模组的开关：

| 实际配方状态 | 启用的成就 | 触发条件 | 禁用的成就 |
| --- | --- | --- | --- |
| 存在附魔金苹果配方 | 君临天下（`"frame": "challenge"`） | 合成附魔金苹果 | 再度君临 |
| 不存在附魔金苹果配方 | 再度君临 | 吃下附魔金苹果 | 君临天下 |

普通取出和 Shift 合成都按实际产物触发，不限制配方 ID；仅拿到或吃下苹果不会解锁合成成就。

### 配方兼容与去重

合成表开关只控制本模组的配方，不会移除其他模组的配方。
若其他模组或数据包已有相同材料、排列和产物的配方，本模组自动不加载自己的配方及配方书解锁，
仅保留另一来源的配方，避免重复显示。
移除另一来源后，若本模组的合成表开关仍开启，下次加载配方时会恢复本模组的配方。

### Mod Menu 设置与旧版纹理

安装可选的 Mod Menu（20.0.2 或更新）后，配置界面有两个选项：

1. 启用/禁用附魔金苹果旧版合成表。
2. 选择附魔金苹果旧版纹理/新版纹理。

两个旧版选项默认启用。纹理切换即时生效，保留附魔闪光，不改变普通金苹果。
旧纹理取自 Mojang 原版内置 Programmer Art 资源包，模组封面也使用这一纹理，详见[纹理来源](docs/legacy-texture.md)。
配置保存在 `config/overpowered_again.json`，字段为 `legacyRecipe` 和 `legacyTexture`。
单人游戏关闭设置界面后会自动重载合成表和进度；多人游戏的客户端不能改服务器配方，
需在服务器配置中修改并重启服务器。纹理选项由客户端独立控制。

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
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationExternalRecipe=true
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationLegacyRecipe=false
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationLegacyRecipe=false -PintegrationExternalRecipe=true
```

以上四条命令分别覆盖：仅本模组配方、两个模组提供相同配方、无配方、仅其他模组配方。
测试在真实开发服务端中检查配方去重、成就切换、普通及 Shift 合成触发、食用效果刷新、
独立效果计时，以及新世界资源加载阶段的安全配方检测。其他模组配方由测试数据包模拟；这些测试不覆盖客户端设置界面与纹理显示。
每次运行都会使用 Gradle 打印出的全新目录，结果写入
`run-test/run-<时间戳>/test-result.txt`，旧结果会保留。

检查结束后，测试通过 `halt(false)` 请求正常关闭服务端，回到服务端主循环，而不是在服务端线程里调用
`System.exit`。

## 许可

本模组以 [Unlicense](https://unlicense.org) 释放到公共领域，Fabric 元数据即以它作为首选许可；
如果你更愿意，也可以改用 [MIT 许可](https://opensource.org/license/mit)。

见 [LICENSE](LICENSE) 与 [LICENSE-MIT](LICENSE-MIT)。

内嵌的 Mojang 纹理不属于模组代码许可范围，详见[纹理来源](docs/legacy-texture.md)。
