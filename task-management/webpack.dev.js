const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'task-management';

// add the server path to your server location path
// const exoServerPath = "/exo-server";
const exoServerPath = "/home/exo/Desktop/platform-6.2.x-SNAPSHOT";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`)
  },
  devtool: 'inline-source-map'
});

module.exports = config;
