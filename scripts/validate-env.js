const fs = require('fs');
const path = require('path');

// Carga .env manualmente si existe
const envPath = path.resolve(__dirname, '../.env');
if (fs.existsSync(envPath)) {
  const envContent = fs.readFileSync(envPath, 'utf8');
  envContent.split('\n').forEach(line => {
    const trimmed = line.trim();
    if (trimmed && !trimmed.startsWith('#') && trimmed.includes('=')) {
      const [key, ...values] = trimmed.split('=');
      const val = values.join('=').replace(/^["']|["']$/g, '');
      if (!process.env[key.trim()]) {
        process.env[key.trim()] = val;
      }
    }
  });
}

// Reglas de validación para las variables de entorno
const schema = [
  {
    key: 'APP_ID',
    required: true,
    pattern: /^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)+$/,
    message: 'Debe tener un formato de paquete válido (ej. com.ptahstudio.demo).'
  },
  {
    key: 'EXTENSION_ID',
    required: true,
    pattern: /^[a-z]{32}$/,
    message: 'Debe ser un ID de Chrome Web Store válido (32 letras minúsculas).'
  },
  {
    key: 'CWS_CLIENT_ID',
    required: true,
    pattern: /\.apps\.googleusercontent\.com$/,
    message: 'Debe terminar en .apps.googleusercontent.com.'
  },
  {
    key: 'CWS_CLIENT_SECRET',
    required: true,
    pattern: /^GOCSPX-.+/,
    message: 'Debe iniciar con el prefijo GOCSPX-.'
  },
  {
    key: 'CWS_REFRESH_TOKEN',
    required: true,
    pattern: /^1\/\/.+/,
    message: 'Debe ser un Refresh Token válido de Google (inicia con 1//).'
  },
  {
    key: 'GEMINI_API_KEY',
    required: true,
    pattern: /^AIzaSy.+/,
    message: 'Debe iniciar con el prefijo oficial AIzaSy.'
  },
  {
    key: 'ANDROID_STORE_PASSWORD',
    required: true,
    pattern: /^.{6,}$/,
    message: 'La contraseña debe contener al menos 6 caracteres.'
  }
];

const errors = [];

schema.forEach(({ key, required, pattern, message }) => {
  const value = process.env[key];

  if (!value) {
    if (required) {
      errors.push(`❌ FALTA VARIABLE [${key}]: No definida en .env ni en el entorno.`);
    }
    return;
  }

  if (pattern && !pattern.test(value)) {
    errors.push(`⚠️ FORMATO INVÁLIDO [${key}]: ${message}`);
  }
});

if (errors.length > 0) {
  console.error('\n🔴 Falló la validación del entorno de Ptah Studio:\n');
  errors.forEach(err => console.error(`  ${err}`));
  console.error('\nPor favor corrige los valores en tu archivo .env antes de continuar.\n');
  process.exit(1);
}

console.log('✅ Entorno validado correctamente. Todas las variables están presentes y con formato correcto.');
process.exit(0);
