module.exports = {
  presets: [
    ['@babel/preset-env', { targets: 'defaults' }],
    ['@babel/preset-react', { runtime: 'automatic' }],
  ],
  overrides: [
    {
      test: /\.ts$/,
      presets: [['@babel/preset-typescript', { allExtensions: true, isTSX: false }]],
    },
    {
      test: /\.tsx$/,
      presets: [['@babel/preset-typescript', { allExtensions: true, isTSX: true }]],
    },
  ],
};
