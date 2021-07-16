package com.rokid.appmonitor;

interface IAppMonitorListener {
  // 特殊应用状态变化通知
  void onStatusChange(int id, int status);
}
