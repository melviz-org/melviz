
const CopyPlugin = require("copy-webpack-plugin");
const { merge } = require("webpack-merge");
const common = require("webpack-base/webpack.common.config");

module.exports = async (webpackEnv) => {
  const components = ["echarts", "llm-prompter", "svg-heatmap"];
  const copyResources = [];
  // Melviz Core
  copyResources.push({
    from: `../core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp`,
    to: `./`,
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
