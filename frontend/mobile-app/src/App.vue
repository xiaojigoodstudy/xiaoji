<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('xiaoji mobile-app launched')
})

onShow(() => {
  console.log('xiaoji mobile-app shown')
})

onHide(() => {
  console.log('xiaoji mobile-app hidden')
})
</script>

<style lang="scss">
page {
  background: #f5f7fa;
  color: #222;
}
</style>
