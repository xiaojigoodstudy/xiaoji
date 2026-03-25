Page({
  data: {
    result: '???'
  },

  onTapHealth() {
    const url = 'http://127.0.0.1:8080/api/health'
    wx.request({
      url,
      method: 'GET',
      success: (res) => {
        this.setData({ result: JSON.stringify(res.data) })
      },
      fail: (err) => {
        this.setData({ result: '????: ' + JSON.stringify(err) })
      }
    })
  }
})
