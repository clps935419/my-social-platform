export default {
  input: 'http://localhost:8080/api/api-docs',
  output: {
    path: './src/api/generated',
    format: 'prettier',
    lint: 'biome',
  },
  plugins: [
    '@tanstack/vue-query',
    {
      name: '@hey-api/client-axios',
      runtimeConfigPath: './src/api/hey-api.runtime.ts',
    },
  ],
};
