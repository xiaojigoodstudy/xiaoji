<template>
  <view class="page">
    <view class="hero">
      <text class="title">小记工具集</text>
      <text class="subtitle">统一移动端入口（uni-app）</text>
    </view>
    <view class="card">
      <text>已接入模块入口：打卡、计划、我的</text>
    </view>
  </view>
</template>

<style lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
}

.hero {
  padding: 32rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #0ea5e9, #14b8a6);
}

.title {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #fff;
}

.subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.92);
}

.card {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #fff;
  color: #334155;
}
</style>
