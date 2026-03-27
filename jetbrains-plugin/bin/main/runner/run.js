#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

function parseArg(name) {
  const idx = process.argv.indexOf(name);
  if (idx === -1) return null;
  return process.argv[idx + 1] || null;
}

function readPayload() {
  const payloadFile = parseArg('--payloadFile');
  if (!payloadFile) {
    throw new Error('Argumento obrigatório ausente: --payloadFile');
  }
  const raw = fs.readFileSync(payloadFile, 'utf8');
  return JSON.parse(raw);
}

function resolveEngineBase() {
  return path.resolve(__dirname, 'node_modules', '@liferay-react', 'create-react-liferay');
}

function resolveTemplateConfig(payload) {
  const moduleBase = path.resolve(__dirname, 'node_modules', '@liferay-react', 'boilerplate-modulo');
  const sharedBase = path.resolve(__dirname, 'node_modules', '@liferay-react', 'boilerplate-shared');
  const originalDir = (payload.selectedTemplate && payload.selectedTemplate.dir) || '';

  if (payload.type === 'shared') {
    return {
      templateDir: sharedBase,
      selectedTemplate: { ...payload.selectedTemplate, dir: 'src' },
    };
  }

  const dirMap = {
    'templates/modules/src-simple': 'src-sem-shared',
    'templates/modules/src-simple-with-template': 'src',
    'templates/modules/src-shared': 'src',
    'templates/modules/src-shared-cli': 'src',
  };

  return {
    templateDir: moduleBase,
    selectedTemplate: { ...payload.selectedTemplate, dir: dirMap[originalDir] || 'src' },
  };
}

function run() {
  const payload = readPayload();
  const engineBase = resolveEngineBase();
  const templateConfig = resolveTemplateConfig(payload);

  const actionsPath = path.join(engineBase, 'bin', 'actions.js');
  const utilsPath = path.join(engineBase, 'bin', 'utils.js');

  if (!fs.existsSync(actionsPath) || !fs.existsSync(utilsPath)) {
    throw new Error(`Engine não encontrada em: ${engineBase}`);
  }

  const { processProject } = require(actionsPath);
  const { getJavaVersion } = require(utilsPath);

  processProject({
    targetDir: payload.targetDir,
    templateDir: templateConfig.templateDir,
    safeProjectName: payload.safeProjectName,
    displayName: payload.displayName,
    finalLiferayDir: payload.liferayDir,
    finalCategory: payload.category || '',
    hasSharedBundle: !!payload.hasSharedBundle,
    finalSharedBundle: payload.sharedBundleDirName || '',
    isJava21Plus: getJavaVersion(),
    selectedTemplate: templateConfig.selectedTemplate,
  });

  console.log(`✅ Módulo ${payload.safeProjectName} criado com sucesso!`);
}

try {
  run();
} catch (err) {
  console.error(err && err.stack ? err.stack : String(err));
  process.exit(1);
}

