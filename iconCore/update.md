# IconCore更新日志

### 1.0.6（2020.11.30）
* 增加`AppFilterMergeHelper`，用于`appfilter.xml`合并的工具，帮助开发者快速整理和合并适配清单
* 调整AppFilter文件生成的属性顺序

### 1.0.3（2020.11.10）
* 增加`Icon`信息的差异对比工具，简化新增图标信息的整理与展示
* 优化线程同步造成的安全问题
* 优化图标信息序列化时产生的字符转义问题
* 增加`IconHelper.MultipleXmlMap`混合图标字典，可以为图标检索与图标遍历分别提供字典
* 增加`SharedPreferencesUtils`持久化储存工具，简化本地参数存储过程
* 修复内部细节问题，优化性能

### 1.0.2（2020.11.07）
* 增加`CrashHandler`，增加对于崩溃信息的收集能力
* 修复内部细节问题，优化性能

### 1.0.1（2020.11.05）
* 调整`IconHelper`的`Drawable`缓存策略，修复因为包名重复导致的`Icon`重复问题
* 修复内部细节问题，优化性能

### 1.0.0 (2020.11.02)
* 完成基础功能的开发
* 增加`UpdateInfoManager`的全局缓存，加速版本更新内容的读取
* 增加`ExternalLinkManager`的全局缓存，加速外部链接信息的读取
* 增加`ExternalLinkManager`的扩充字段，增加接入者的扩展性
* 调整适配文件的图标名称生成算法，减少重复性
* 修复`ZipHelper`算法中的崩溃中断问题
* 修复`IconSaveHelper`保存icon时导致的界面图标显示异常问题
* `Fragment`增加浅色状态栏控制开关
* 优化`IconHelper`数据加载效率（加速到10ms内）
* 增加`MakerInfoManager`中的应用图标字段