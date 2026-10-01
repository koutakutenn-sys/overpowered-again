# Overpowered Again

[English](README.md) · [简体中文](README.zh-CN.md) · **繁體中文**

##### 再度君臨

一個面向 **Minecraft 26.2** 的小型 Fabric 模組，重新定義附魔金蘋果。

食下附魔金蘋果後會獲得：

| 效果 | 等級 | 持續時間 |
| --- | --- | --- |
| 生命回復 | IV | 30 秒 |
| 傷害吸收 | IV | 2 分鐘 |
| 抗性 | I | 5 分鐘 |
| 抗火 | I | 5 分鐘 |

在原版 26.2 中，後三項已經是這些數值；本模組改的是生命回復——由 II 級 20 秒改為 IV 級 30 秒。
這裡把四項完整列出，是為了在原版數值變動時，行為不會悄悄偏移。

## 進度（成就）

模組新增一個進度：

- **名稱：** Overpowered Again
- **描述：** 食下一個附魔金蘋果
- 食下附魔金蘋果時解鎖

## 實作方式

自 1.21.2 起，可食用物品的效果存放在 `minecraft:consumable` 物品組件上，類型是一組
consume effect，由 `Consumable#onConsume` 透過 `onConsumeEffects.forEach(...)` 套用。
`ConsumableMixin` 只重定向了這一處呼叫點（而且只針對附魔金蘋果），因此食用的其他環節完全保持原版行為：
粒子、音效、統計、`consume_item` 進度觸發器，以及物品消耗本身。

目標數值只寫在一個地方：
`src/main/java/com/koutakutenn/overpoweredagain/OverpoweredEffects.java`。

## 環境要求

- Minecraft 26.2
- Fabric Loader 0.19.5 或更新
- Java 25 或更新
- 無需另外安裝 Fabric API：jar 內已內嵌 Fabric Resource Loader v1 與 Fabric API Base，
  用於載入本模組的資料與語言資源。

## 安裝

把 jar 放進實例的 `mods` 資料夾後重新啟動遊戲。模組不支援熱載入，必須完全重新啟動。

## 建置

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
./gradlew build
```

成品位於 `build/libs/`。

## 測試

```sh
./gradlew runIntegrationTest -PacceptMinecraftEula=true
```

這會啟動一個真實的伺服端，檢查四種效果、被替換掉的原版數值、自訂進度及其物品謂詞。
每次執行都會使用 Gradle 列印出的全新目錄，結果寫入
`run-test/run-<時間戳>/test-result.txt`，舊結果會保留。

檢查結束後，測試透過 `halt(false)` 要求正常關閉伺服端，回到伺服端主迴圈，而不是在伺服端執行緒中呼叫
`System.exit`。

## 授權

本模組以 [Unlicense](https://unlicense.org) 釋出至公共領域，Fabric 中介資料即以它作為首選授權；
如你更願意，也可以改用 [MIT 授權](https://opensource.org/license/mit)。

見 [LICENSE](LICENSE) 與 [LICENSE-MIT](LICENSE-MIT)。
