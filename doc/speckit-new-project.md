# 新專案導入 Spec Kit（Specify CLI）快速指南

本文件說明在 Windows（亦適用 macOS/Linux）以 Spec-Driven Development 流程啟動新專案，透過 Specify CLI 初始化專案並在 AI 助手中使用 `speckit` 系列斜線指令。

---

## 前置需求
- Python 3.11 以上
- 套件管理工具 `uv`（用於安裝 Specify CLI）
- Git（初始化與版本控制）
- 支援斜線指令的 AI 程式設計助手（如 GitHub Copilot、Claude Code、Cursor、Windsurf、Qoder 等）

> 若尚未安裝 uv，請參考官方文件：https://docs.astral.sh/uv/

---

## 安裝 Specify CLI
推薦持久安裝（一次安裝，隨處可用）。

```bash
# 推薦方式：持久安裝
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git

# 升級（需要時）
uv tool install specify-cli --force --from git+https://github.com/github/spec-kit.git

# 一次性使用（不安裝到 PATH）：
uvx --from git+https://github.com/github/spec-kit.git specify init <PROJECT_NAME>
```

---

## 初始化新專案
以新目錄或現有目錄啟動專案骨架與流程整合。

```bash
# 在新目錄建立專案
specify init my-project

# 指定 AI 助手（範例：Copilot）
specify init my-project --ai copilot

# 在現有目錄初始化（兩種寫法）
specify init . --ai copilot
specify init --here --ai copilot

> 非空目錄提示：
> 當你在「現有目錄」初始化且該目錄不是空的，CLI 會顯示：
> `Warning: Current directory is not empty (...) Template files will be merged with existing content and may overwrite existing files Do you want to continue? [y/N]:`
> 這表示範本檔案將「合併」到目前目錄，並「可能覆蓋」既有檔案。
> - 輸入 `y` 代表同意繼續合併並可能覆蓋。
> - 直接按 Enter 或輸入 `N` 代表取消。
> - 若你確定要略過互動確認，可使用 `--force`：
>
```bash
specify init --here --force --ai copilot
```
>
> 建議先把現有內容備份或（若使用 Git）先 commit，再繼續。

# Windows/跨平台使用 PowerShell 版本腳本
specify init my-project --ai copilot --script ps

# 非空目錄強制合併（略過確認）
specify init . --force --ai copilot

# 跳過 git 初始化
specify init my-project --ai gemini --no-git

# 檢查環境工具（git、各 AI 工具等）
specify check
```

常用 `--ai` 選項值：`claude`, `gemini`, `copilot`, `cursor-agent`, `qwen`, `opencode`, `codex`, `windsurf`, `kilocode`, `auggie`, `roo`, `codebuddy`, `amp`, `shai`, `q`, `bob`, `qoder`。

---

## 常用選項速查
- `<project-name>`：新目錄名稱（或使用 `--here`/`.` 於當前目錄）
- `--ai <name>`：選擇整合的 AI 助手
- `--script <sh|ps>`：選擇 Shell 腳本類型（Bash/Zsh 或 PowerShell）
- `--ignore-agent-tools`：略過 AI 助手工具檢查
- `--no-git`：略過 git 初始化
- `--here`：於當前目錄初始化
- `--force`：強制合併至非空目錄，略過確認
- `--skip-tls`：略過 TLS 驗證（不建議）
- `--debug`：輸出除錯資訊
- `--github-token <token>`：用於 API 請求（亦可透過 `GH_TOKEN`/`GITHUB_TOKEN` 環境變數設定）

---

## 在 AI 助手中使用 `speckit` 流程
執行 `specify init` 後，AI 助手會注入以下斜線指令，以支援 Spec-Driven Development 的步驟：

核心指令（建議依序進行）：
- `/speckit.constitution`：建立/更新專案的「憲章」與開發準則（品質、測試、UX、效能等）
- `/speckit.specify`：撰寫需求與使用者故事（聚焦「做什麼、為什麼」，少談技術細節）
- `/speckit.plan`：制定技術落地計畫（技術棧與架構）
- `/speckit.tasks`：將計畫拆解為可執行任務清單
- `/speckit.implement`：依任務執行實作

可選加值指令：
- `/speckit.clarify`：釐清模糊不明之處（建議在 `/speckit.plan` 前使用）
- `/speckit.analyze`：交叉檢查規格、任務與產物的一致性與覆蓋度（在 `/speckit.tasks` 之後、`/speckit.implement` 之前）
- `/speckit.checklist`：產生自訂品質檢核清單（像英文版「單元測試」的概念）

> 心法重點：先有可執行規格，再進入實作；以規格導向的流程來確保可預期的結果與品質。

---

## 進階：環境變數
- `SPECIFY_FEATURE`：在非 Git 環境下覆寫特徵偵測（例如：`001-photo-albums`），可讓代理在無分支情境下聚焦特定功能。請於使用 `/speckit.plan` 或後續指令前，在代理的執行環境中設定。

```bash
# 例：鎖定到特定功能目錄（非 Git 情境）
export SPECIFY_FEATURE=001-photo-albums
```

---

## 疑難排解與建議
- 啟用除錯輸出：
```bash
specify init my-project --ai claude --debug
```
- 企業環境 API 需求：
```bash
specify init my-project --ai claude --github-token <your_token>
# 或使用環境變數：GH_TOKEN / GITHUB_TOKEN
```
- Git 認證問題（Linux）：可安裝 Git Credential Manager；詳見官方 README 的 Troubleshooting 段落。

---

## 參考連結
- 官方 README（含完整流程與範例）：https://github.com/github/spec-kit
- 詳細方法學（Spec-Driven Development）：https://github.com/github/spec-kit/blob/main/spec-driven.md
- 影片總覽（操作示範）：https://www.youtube.com/watch?v=a9eR1xsfvHg

---

## 快速開始範例（建議路徑）
1) 安裝工具：
```bash
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git
```
2) 初始化專案骨架：
```bash
specify init my-project --ai copilot
```
3) 開啟 AI 助手，在專案目錄依序執行：
```
/speckit.constitution
/speckit.specify
/speckit.plan
/speckit.tasks
/speckit.implement
```
4) 檢查環境或升級：
```bash
specify check
uv tool install specify-cli --force --from git+https://github.com/github/spec-kit.git
```

> 完成上述步驟後，即可以規格為中心，快速且可預期地推進新功能或整個專案的實作。