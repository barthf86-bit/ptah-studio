const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const projectRoot = path.resolve(__dirname, '..');
const distDir = path.join(projectRoot, 'dist');
const zipPath = path.join(distDir, 'chrome-extension.zip');

// Lista de archivos y carpetas requeridos para la extensión / PWA
const requiredFiles = ['manifest.json', 'sw.js', 'index.html'];

console.log('📦 Iniciando empaquetado de la Extensión de Chrome / PWA...\n');

// 1. Validar presencia de archivos mínimos esenciales
const missingFiles = requiredFiles.filter(file => !fs.existsSync(path.join(projectRoot, file)));
if (missingFiles.length > 0) {
  console.warn(`⚠️ Advertencia: Faltan los siguientes archivos en la raíz: ${missingFiles.join(', ')}`);
}

// 2. Crear carpeta dist/ si no existe
if (!fs.existsSync(distDir)) {
  fs.mkdirSync(distDir, { recursive: true });
}

// 3. Eliminar archivo zip anterior si existe
if (fs.existsSync(zipPath)) {
  fs.unlinkSync(zipPath);
}

// 4. Comprimir archivos excluyendo archivos de configuración privada y node_modules
try {
  const excludePattern = '-x "*.env*" "*scripts/*" "*node_modules/*" "*.git*" "*dist/*" "*.keystore" "*.jks" "play-store-key.json" "build/*" "app/*"';
  execSync(`zip -r "${zipPath}" . ${excludePattern}`, { cwd: projectRoot, stdio: 'pipe' });

  const stats = fs.statSync(zipPath);
  const sizeInMB = (stats.size / (1024 * 1024)).toFixed(2);

  console.log(`✅ ¡Empaquetado exitoso!`);
  console.log(`📁 Ubicación: ${zipPath}`);
  console.log(`📊 Tamaño final: ${stats.size} bytes (${sizeInMB} MB)\n`);
  process.exit(0);
} catch (error) {
  console.error('❌ Error al comprimir el paquete ZIP:', error.message);
  process.exit(1);
}
