const vscode = require("vscode");
const path = require("path");
const fs = require("fs");

async function activate(context) {
  console.log('Extensão "Create React Liferay" ativada com sucesso!');

  let disposable = vscode.commands.registerCommand(
    "create-react-liferay.generate",
    async function () {
      try {
        // Importando dinamicamente as funções do seu próprio pacote Liferay-React!
        const {
          processProject,
        } = require("@liferay-react/create-react-liferay/bin/actions.js");
        const {
          normalizeDirName,
          formatDisplayName,
          getJavaVersion,
        } = require("@liferay-react/create-react-liferay/bin/utils.js");

        // 1. Pergunta o Tipo de Módulo
        const typeOptions = [
          {
            label: "$(symbol-class) Widget React",
            description: "Módulo Padrão",
            value: "widget",
          },
          {
            label: "$(package) Shared Bundle",
            description: "Dependências Compartilhadas",
            value: "shared",
          },
        ];

        const selectedTypeObj = await vscode.window.showQuickPick(typeOptions, {
          placeHolder: "Qual o tipo do módulo que você quer criar?",
        });

        if (!selectedTypeObj) return; // Se o usuário cancelar, interrompemos.
        const selectedType = selectedTypeObj.value;

        let safeProjectName = "";
        let displayName = "";
        let selectedTemplate;
        let portletCategory = "";
        let hasSharedBundle = false;
        let finalSharedBundle = "";

        // 2. Fluxo: Shared
        if (selectedType === "shared") {
          selectedTemplate = {
            name: "Shared Bundle",
            dir: "templates/shared-bundle",
            hasShared: false,
          };

          let projectNameInput = await vscode.window.showInputBox({
            prompt: "Qual o nome do shared bundle?",
            value: "novo-shared-bundle",
            placeHolder: "Ex: liferay-library-shared",
          });
          if (!projectNameInput) return; // cancelou

          safeProjectName = normalizeDirName(projectNameInput);
          displayName = formatDisplayName(projectNameInput);
        }
        // 3. Fluxo: Widget React
        else {
          const templateOptions = [
            {
              label: "Módulo simples",
              value: {
                name: "Simples",
                dir: "templates/modules/src-simple",
                hasShared: false,
              },
            },
            {
              label: "Módulo simples com template pré-determinado",
              value: {
                name: "Com Template Pré-determinado",
                dir: "templates/modules/src-simple-with-template",
                hasShared: false,
              },
            },
            {
              label: "Módulo com Shared (básico)",
              value: {
                name: "Com Shared",
                dir: "templates/modules/src-shared",
                hasShared: true,
              },
            },
            {
              label: "Módulo com Shared CLI",
              value: {
                name: "Com Shared CLI",
                dir: "templates/modules/src-shared-cli",
                hasShared: true,
              },
            },
          ];

          const selectedTemplateObj = await vscode.window.showQuickPick(
            templateOptions,
            {
              placeHolder: "Qual o template (modo) do módulo?",
            },
          );
          if (!selectedTemplateObj) return;
          selectedTemplate = selectedTemplateObj.value;

          let projectNameInput = await vscode.window.showInputBox({
            prompt: "Qual o nome do módulo?",
            value: "novo-modulo",
          });
          if (!projectNameInput) return;

          safeProjectName = normalizeDirName(projectNameInput);
          displayName = formatDisplayName(projectNameInput);

          portletCategory = await vscode.window.showInputBox({
            prompt:
              "Qual a categoria do portlet? (ex: category.simple, category.hidden)",
            value: "category.simple",
          });
          if (!portletCategory) return;

          if (selectedTemplate.hasShared) {
            hasSharedBundle = true;
            finalSharedBundle = await vscode.window.showInputBox({
              prompt: "Qual o nome do diretório do seu shared bundle?",
              placeHolder: "Ex: shared-bundle",
              validateInput: (text) =>
                text.trim()
                  ? null
                  : "O nome do diretório do shared bundle é obrigatório.",
            });
            if (!finalSharedBundle) return;
          }
        }

        // 5. Definir o local de destino
        let defaultUri = undefined;
        const workspaceFolders = vscode.workspace.workspaceFolders;

        if (workspaceFolders) {
          let defaultCwd = workspaceFolders[0].uri.fsPath;
          // Sugere o diretório "modules" por padrão se ele existir
          const modulesPath = path.join(defaultCwd, "modules");
          if (
            fs.existsSync(modulesPath) &&
            fs.lstatSync(modulesPath).isDirectory()
          ) {
            defaultCwd = modulesPath;
          }
          defaultUri = vscode.Uri.file(defaultCwd);
        }

        // Abre uma janela para o usuário informar/confirmar onde o módulo será criado
        const destUri = await vscode.window.showOpenDialog({
          canSelectFiles: false,
          canSelectFolders: true,
          canSelectMany: false,
          openLabel: "Criar Módulo Aqui",
          defaultUri: defaultUri,
          title: `Onde você deseja criar o ${selectedType === "shared" ? "Shared Bundle" : "Widget"}?`,
        });

        if (!destUri || destUri.length === 0) {
          return; // O usuário cancelou a escolha da pasta
        }

        let baseDir = destUri[0].fsPath;

        // Resolve duplicidades de nome (Cópia, Cópia 1)
        let finalProjectName = safeProjectName;
        let finalDisplayName = displayName;
        let targetDir = path.join(baseDir, finalProjectName);
        let counter = 0;

        while (fs.existsSync(targetDir)) {
          counter++;
          const suffix = counter === 1 ? "-copia" : `-copia-${counter}`;
          finalProjectName = `${safeProjectName}${suffix}`;
          finalDisplayName = `${displayName} Copia${counter > 1 ? ` ${counter}` : ""}`;
          targetDir = path.join(baseDir, finalProjectName);
        }

        // 6. Liferay Path (Validação considerando caminhos relativos ao arquivo gerado)
        const liferayPath = await vscode.window.showInputBox({
          prompt:
            "Qual o caminho (relativo ou absoluto) para o bundle do Liferay (liferayDir)?",
          placeHolder: "Ex: ../../bundles",
          validateInput: (text) => {
            if (!text.trim()) return "Campo obrigatório";
            try {
              // resolve junta targetDir com text. Se text for absoluto, ele apenas retorna o texto.
              const resolvedPath = path.resolve(targetDir, text);
              return fs.existsSync(resolvedPath) &&
                fs.lstatSync(resolvedPath).isDirectory()
                ? null
                : "A pasta do Liferay não foi encontrada. Verifique o caminho digitado.";
            } catch (e) {
              return "Caminho inválido";
            }
          },
        });
        if (!liferayPath) return;

        // 7. Chamando sua Engine!
        // Pega o caminho original da biblioteca pai
        const templateDirObj =
          require.resolve("@liferay-react/create-react-liferay/package.json");
        const templateDir = path.dirname(templateDirObj);
        const isJava21Plus = getJavaVersion();

        // Mensagem de loading
        vscode.window.withProgress(
          {
            location: vscode.ProgressLocation.Notification,
            title: `Criando módulo: ${finalProjectName}`,
            cancellable: false,
          },
          async (progress) => {
            // Processando o projeto
            processProject({
              targetDir,
              templateDir,
              safeProjectName: finalProjectName,
              displayName: finalDisplayName,
              finalLiferayDir: liferayPath,
              finalCategory: portletCategory,
              hasSharedBundle,
              finalSharedBundle,
              isJava21Plus,
              selectedTemplate,
            });

            vscode.window.showInformationMessage(
              `✅ Módulo ${finalProjectName} criado com sucesso!`,
            );
          },
        );
      } catch (err) {
        console.error(err);
        vscode.window.showErrorMessage("Erro inesperado: " + err.message);
      }
    },
  );

  context.subscriptions.push(disposable);
}

function deactivate() {}

module.exports = {
  activate,
  deactivate,
};
