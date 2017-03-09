"use strict"
const path = require("path")
module.exports = {
  devServer: {
    contentBase: "./src/main/resources/static/",
    port: 3000,
    proxy: {
      '/': {
        target: 'http://localhost:8080',
      }
    },
  },
  devtool: "source-map",
  entry: "./src/main/front/test.ts",
  output: {
    path: path.resolve("./src/main/resources/static/"),
    filename: "bundle.js",
  },
  resolve: {
    extensions: [".ts", ".tsx", ".js"]
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: "ts-loader"
          },
        ]
      },
    ],
  },
};