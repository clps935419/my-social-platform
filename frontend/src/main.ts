import { createApp } from "vue";
import { VueQueryPlugin } from "@tanstack/vue-query";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import App from "./App.vue";
import router from "./router";
import { configureApiClient } from "./api/client";

const app = createApp(App);

configureApiClient();

// Setup Vue Router
app.use(router);

// Setup TanStack Query (Vue Query)
app.use(VueQueryPlugin, {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        retry: 1,
        staleTime: 1000 * 60 * 5, // 5 minutes
      },
    },
  },
});

// Setup Element Plus
app.use(ElementPlus);
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.mount("#app");
