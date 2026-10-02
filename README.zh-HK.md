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

## 合成表、成就與設定

預設恢復舊版合成表：普通蘋果放中央，周圍放 **8 個金塊**，合成 **1 個附魔金蘋果**。

| 金塊 | 金塊 | 金塊 |
| --- | --- | --- |
| 金塊 | 蘋果 | 金塊 |
| 金塊 | 金塊 | 金塊 |

取得金塊後會在配方書解鎖本模組的配方。

### 成就自動切換

成就按遊戲實際載入的配方切換，不僅取決於本模組的開關：

| 實際配方狀態 | 啟用的成就 | 觸發條件 | 停用的成就 |
| --- | --- | --- | --- |
| 存在附魔金蘋果配方 | 君臨天下（`"frame": "challenge"`） | 合成附魔金蘋果 | 再度君臨 |
| 不存在附魔金蘋果配方 | 再度君臨 | 食下附魔金蘋果 | 君臨天下 |

普通取出和 Shift 合成都按實際產物觸發，不限制配方 ID；僅取得或食下蘋果不會解鎖合成成就。

### 配方相容與去重

合成表開關只控制本模組的配方，不會移除其他模組的配方。
若其他模組或資料包已有相同材料、排列和產物的配方，本模組自動不載入自己的配方及配方書解鎖，
僅保留另一來源的配方，避免重複顯示。
移除另一來源後，若本模組的合成表開關仍啟用，下次載入配方時會恢復本模組的配方。

### Mod Menu 設定與舊版紋理

安裝可選的 Mod Menu（20.0.2 或更新）後，設定介面有兩個選項：

1. 啟用/停用附魔金蘋果舊版合成表。
2. 選擇附魔金蘋果舊版紋理/新版紋理。

兩個舊版選項預設啟用。紋理切換即時生效，保留附魔閃光，不改變普通金蘋果。
舊紋理取自 Mojang 原版內置 Programmer Art 資源包，模組封面亦使用這一紋理，詳見[紋理來源](docs/legacy-texture.md)。
設定儲存在 `config/overpowered_again.json`，欄位為 `legacyRecipe` 和 `legacyTexture`。
單人遊戲關閉設定介面後會自動重新載入合成表和進度；多人遊戲的客戶端不能改伺服器配方，
需在伺服器設定中修改並重新啟動伺服器。紋理選項由客戶端獨立控制。

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
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationExternalRecipe=true
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationLegacyRecipe=false
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationLegacyRecipe=false -PintegrationExternalRecipe=true
```

以上四條指令分別涵蓋：僅本模組配方、兩個模組提供相同配方、無配方、僅其他模組配方。
測試在真實開發伺服端中檢查配方去重、成就切換、普通及 Shift 合成觸發、食用效果刷新、
獨立效果計時，以及新世界資源載入階段的安全配方偵測。其他模組配方由測試資料包模擬；這些測試不涵蓋客戶端設定介面與紋理顯示。
每次執行都會使用 Gradle 列印出的全新目錄，結果寫入
`run-test/run-<時間戳>/test-result.txt`，舊結果會保留。

檢查結束後，測試透過 `halt(false)` 要求正常關閉伺服端，回到伺服端主迴圈，而不是在伺服端執行緒中呼叫
`System.exit`。

## 授權

本模組以 [Unlicense](https://unlicense.org) 釋出至公共領域，Fabric 中介資料即以它作為首選授權；
如你更願意，也可以改用 [MIT 授權](https://opensource.org/license/mit)。

見 [LICENSE](LICENSE) 與 [LICENSE-MIT](LICENSE-MIT)。

內嵌的 Mojang 紋理不屬於模組程式碼授權範圍，詳見[紋理來源](docs/legacy-texture.md)。
