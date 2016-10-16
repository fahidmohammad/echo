var fs = require("fs");
var browserify = require("browserify");
var babelify = require("babelify");
browserify("./src/app.jsx")
  .transform(babelify, {presets: ["es2015", "react"]})
  .bundle()
  .pipe(fs.createWriteStream("./javascripts/bundle.js"));