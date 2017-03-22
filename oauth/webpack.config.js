"use strict"
const path = require("path")
module.exports = {
  devServer: {
    contentBase: "./src/main/resources/static/",
    port: 3000,
    proxy: {
      '/**/*': {
        target: 'http://localhost:8080',
        bypass: function(req) {
          if (req.url === "/" || req.url.indexOf('.') !== -1) {
            return req.url
          }
        }
      }
    },
  },
  devtool: "source-map",
  entry: "./src/main/front/index.tsx",
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
        loader: "ts-loader",
        options: {
        }
      },
    ],
  },
};