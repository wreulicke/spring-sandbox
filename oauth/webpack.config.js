"use strict"
const path = require("path")
module.exports = {
  devServer: {
    contentBase: "./",
    port: 3000,
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