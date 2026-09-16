import { createApp } from 'vue'
import App from '@/App.vue'

import router from '@/router'
import {createPinia} from 'pinia'
const pinia = createPinia()
//引入持久化插件
import piniaPluginPersistedstate from "pinia-plugin-persistedstate"
//使用持久化插件
pinia.use(piniaPluginPersistedstate)
//引入vant组件库
import Vant from 'vant'
import 'vant/lib/index.css'
//主题和基础样式
import '@/assets/base.css'
import '@/assets/main.css'

const app = createApp(App)

app.use(router).use(pinia).use(Vant).mount('#app')
