const path = require("path");
const CopyPlugin = require("copy-webpack-plugin");
const { merge } = require("webpack-merge");
const common = require("webpack-base/webpack.common.config");

module.exports = async (webpackEnv) => {
  const components = ["echarts", "llm-prompter", "svg-heatmap", "mermaid"];
  const copyResources = [];
  // Melviz Core
  //
  // We deliberately use a `filter` callback (instead of `globOptions.ignore`) to
  // exclude WEB-INF from the core webapp copy. In copy-webpack-plugin v14 the
  // glob `ignore` patterns are resolved by tinyglobby relative to the webpack
  // context directory, which is the webapp directory and not the `from`
  // directory. As a result, a relative pattern such as "WEB-INF/**" never
  // matches and the WEB-INF folder would end up copied into the build output.
  // The filter callback receives the absolute file name, so we can reliably
  // exclude any path that contains a WEB-INF segment.
  copyResources.push({
    from: `../core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp`,
    to: `./`,
    filter: (absoluteFilename) => {
      const segments = path
        .normalize(absoluteFilename)
        .split(path.sep)
        .filter(Boolean);
      return !segments.includes("WEB-INF");
    },
  });

  components.forEach((component) => {
    copyResources.push({
      from: `../components/melviz-component-${component}/dist/`,
      to: `./melviz/component/${component}/`,
    });
  });

  return merge(common(webpackEnv), {
    entry: {},
    plugins: [
      new CopyPlugin({
        patterns: [...copyResources],
      }),
    ]
  });
};
