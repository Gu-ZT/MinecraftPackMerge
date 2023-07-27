const {defineConfig} = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        port: 6688
    },
    publicPath:'./',
    outputDir:'dist',
    assetsDir:'static'
})
