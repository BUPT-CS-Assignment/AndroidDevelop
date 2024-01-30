[音乐播放器]
实现功能:
- 1. 可视化进度条
-  2. 可视化播放时间及歌曲长度
- 3.  可拖动/可点击进度条
-  4.  播放结束自动触发“停止”

依赖模块:
- 1. 主模块向播放器发送指令通过android.content.Intent完成
- 2. 播放器服务通过android.os.Binder进行绑定，主模块后续可以通过接口调用播放器端服务函数
-  3.  进度条通过android.widget.SeekBar构建
-  4. 进度条拖动和点击通过SeekBar.setOnSeekBarChangeListener实现回调
-  5.  实时播放时间更新通过java.util.Timer及java.util.TimerTask实现定时任务