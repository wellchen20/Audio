package com.rokid.appmonitor;

import com.rokid.appmonitor.IAppMonitorListener;

interface IAppMonitor {
  // 获取id指定的应用活动状态
  // 如获取视频通话应用活动状态: getActiveStatus(IAppMonitor.APPID_VIDEOCALL)
  int getActiveStatus(int id);

  // 通过id设置调用者是否是特殊应用，目前支持的特殊应用如下
  // id: APPID_VIDEOCALL
  // binder用于侦测调用者是否断开
  void setBinder(int id, IBinder binder);

  // 设置调用者当前活动状态
  // 只有特殊应用才需要设置
  // 视频通话应用状态(status):
  //   0: 未启动
  //   1: 在前台但未接通视频
  //   2: 视频通话中
  void active(int status);

  // 监听特殊应用的状态变化
  void setListener(int id, IAppMonitorListener cb);
  const int APPID_VIDEOCALL = 0;
  const int MAX_APPID = 1;
}
