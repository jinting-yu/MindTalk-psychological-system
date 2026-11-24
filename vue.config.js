const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 8080,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  },
  chainWebpack: config => {
    // 移除 babel-loader 处理 ts/tsx
    config.module.rule('ts').uses.delete('babel-loader');
    config.module.rule('tsx').uses.delete('babel-loader');
    
    // 禁用 fork-ts-checker
    config.plugins.delete('fork-ts-checker');
  }
});