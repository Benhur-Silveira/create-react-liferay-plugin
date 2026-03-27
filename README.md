# Create React Liferay (VS Code Extension)

Uma extensão essencial e amigável desenhada para otimizar 100% a experiência de desenvolvimento (DX) de **Widgets React no ecossistema Liferay DXP**, nativamente do seu VS Code.

## 🚀 O que essa extensão faz?
Ela agiliza radicalmente a geração do *boilerplate* inicial (pastas, pacotes, tradução e código-fonte) de módulos Portlet React e *Shared Bundles* para o Liferay. 
Toda a criação funciona orientada por um passo a passo totalmente visual no VS Code, sem precisar usar terminais nem scripts complexos de geração!

## ✨ Funcionalidades
- **Assistente Visual Interativo do VS Code:** Utilize o menu limpo para escolher categorias de Liferay, templates e regras.
- **Escolha de Template:** Construa baseado no que precisar!
  - ⚛️ Módulos Simples
  - 📦 Módulo com Shared Bundle Nativo ou CLI
  - 🔗 Geração livre e separada de Shared Bundles
- **Destino Inteligente:** Se detectar uma pasta `modules/` no seu Workspace, ela entende rapidamente o destino lógico, mas ainda permite que você use a janela do Explorer para salvar o módulo onde você desejar.
- **Caminhos de Liferay Simplificados:** Aceita links absolutos (`C:\Liferay`) e **relativos** (`../../bundles`) para vincular seus ambientes perfeitamente às variáveis de deploy automático.
- Adequação automática de imports (`javax` ou `jakarta`) de acordo com o Java reconhecido em projeto.

## 🛠 Como Usar
1. Abra um projeto qualquer ou a pasta principal do seu Workspace no VS Code.
2. Aperte <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>P</kbd> *(ou Cmd+Shift+P no Mac)* para abrir a Paleta de Comandos do editor.
3. Digite e selecione: **`React Liferay: Criar Módulo`**
4. Siga as perguntas solicitadas! (Tipo, template, nome do widget e local do Liferay).
5. Tudo pronto. Uma nova pasta aparecerá nos seus arquivos, configurada, estruturada, lincada e prontinha para receber os comandos regulares de build e deploy de Front-End no Liferay.

## 🧑‍💻 Requisitos & Dependências
Construído a partir do gerador original [create-react-liferay](https://www.npmjs.com/package/@liferay-react/create-react-liferay).

---
**Criado para acelerar desenvolvedores Liferay.** 
Licença MIT.
