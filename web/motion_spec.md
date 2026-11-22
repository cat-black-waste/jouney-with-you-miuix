# jouney with you — motion spec

Method: pixel2motion v2（`nolangz/pixel2motion`）。先定人格 → 再定时间线 → 再定参数；所有动画参数必须能回溯到人格词。
本文件是网页预览与 APK 的唯一动效基准，Android 端逐项映射（见文末表）。

## 1. 人格（Personality）

- 三个词：**quiet / precise / premium**（安静、精确、有质感）
- 坐标：Energy 低，Tone 严肃偏中性 → 取 `Elegant / Premium` 预设
- 依据：字标是几何无衬线、留白大、单色为主，动效应“克制”而不是“活泼”
- 反证：如果换成 bounce/squash，字标会像消费类 App 的开屏，不像作品署名

## 2. 令牌（Tokens）

```
--duration: 1600ms        # 一次完整揭示
--ease-enter: cubic-bezier(0.16, 1, 0.3, 1)     # 落位，长尾
--ease-settle: cubic-bezier(0.4, 0, 0.2, 1)     # 收束
--overshoot: 1.02         # 只允许“耳语级”过冲
--squash: 0               # 不变形
```

同理心检查：成品用三个词描述应是「安静地亮起来 / 逐字落位 / 不留痕迹」——与人格一致。

## 3. 时间线（20 : 50 : 30）

| 阶段 | 占比 | 时长 | 内容 |
|---|---|---|---|
| Anticipation | 18% | 0–190ms | 字标以灰阶待在场，仅极轻 blur 存在感 |
| Action | 52% | 190–1020ms | 逐字揭示：每字 430ms，步进 34ms（12 字总跨度约 374ms，整串 ≤ 700ms 的级联上限） |
| Follow-through | 30% | 1020–1600ms | 每字独立弹簧收尾，颜色从珊瑚色 crest 沉到墨色，blur 归零 |

级联而不是齐动：每字起点相差 34ms（≈ 每字时长的 8%），落在 10–20% 的推荐区间内。

## 4. 拖影（Drag trail，第二动作）

- 手势：在字标上横向拖动，指尖位置映射为“揭示进度”，每字阈值 = `smoothstep((px - (x-26)) / (w+58))`，再取 `^1.45` 做非线性
- 跟随：拖动时 stiffness 260 / damping 26；松手 420ms 后自动补完（decay，不硬切）
- 颜色：灰 `#C4C4CA` → 珊瑚 `#FF5B37`（crest）→ 墨 `#0C0C0D`；crest 用 `sin^3.2` 包络，只在字的中段出现
- 拖影：每字 3 层残影，位移 `(5 + 16·energy)·j·(1-p/2)`，透明度 `energy·(1-p)/(1.5j)`，1.5px 模糊
- 归位：过冲上限 1.04，落点必须精确等于静态字标（Final Frame Contract）

## 5. 单实例转场

字标是**同一棵树**，三页只换槽位（y / scale / opacity），用指数趋近的弹簧在页面之间连续移动，不做淡入淡出的替身：

| 页 | y | scale | opacity |
|---|---|---|---|
| 1 arrival | 0.40h | 1.00 | 1.0 |
| 2 clearance | 0.155h | 0.40 | 0.62 |
| 3 signature | 0.355h | 0.94 | 1.0 |

页面内容 460ms 淡出 / 620ms 位移入场，与字标位移错开，形成“字标领着内容走”的层次。

## 6. 第二页（假检测）

- 旋转弧 1.25s 线性循环（旋转是线性唯一正当用法）
- 三条假检测：0.76s + 0.44s 步进，逐条落绿
- 完成：环收缩 → 绿色实心圆 scale 0.55→1（0.55s，`cubic-bezier(.2,1.5,.35,1)`）→ 勾 0.38s 描出
- CTA 同步从灰变绿，才可点

## 7. 可访问性 / 性能

- `prefers-reduced-motion` 下直接呈现终态
- 只动 transform / opacity / filter / color，不动布局
- 触摸目标 ≥ 48dp，CTA 56dp

## 8. 网页 ↔ APK 映射

| 网页 | Compose |
|---|---|
| 逐字 span + 弹簧 | `Animatable`/`animateFloatAsState` per glyph + `Text` 绘制 |
| 拖影 em 副本 | `drawText` 三层残影（Canvas） |
| 槽位弹簧 | `animateFloatAsState` 驱动 `graphicsLayer.translationY/scale` |
| 旋转弧 | Miuix `InfiniteProgressIndicator` / `CircularProgressIndicator(null)` |
| 完成圆 + 勾 | `animateFloatAsState` 画勾 `PathMeasure` 截取 |
| 抽屉面板 | Miuix `SuperDialog` |
| 列表行 | Miuix `BasicComponent` / `Card` |
| 按钮 | Miuix `Button` / `TextButton` |
