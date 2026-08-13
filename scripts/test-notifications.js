const fs = require('fs');
const path = require('path');

// Cargar variables de .env
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

const { TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID, SLACK_WEBHOOK_URL } = process.env;

async function testNotifications() {
  console.log('🔔 Probando envíos de notificación desde Ptah Studio...\n');

  // --- PRUEBA TELEGRAM ---
  if (TELEGRAM_BOT_TOKEN && TELEGRAM_CHAT_ID) {
    try {
      const res = await fetch(`https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          chat_id: TELEGRAM_CHAT_ID,
          text: '🧪 *Ptah Studio CI/CD*\n\n¡Prueba de integración local completada con éxito desde la terminal!',
          parse_mode: 'Markdown'
        })
      });
      const data = await res.json();
      if (data.ok) {
        console.log('✅ TELEGRAM: Notificación enviada con éxito.');
      } else {
        console.error('❌ TELEGRAM ERROR:', data.description);
      }
    } catch (err) {
      console.error('❌ TELEGRAM ERROR DE RED:', err.message);
    }
  } else {
    console.warn('⚠️ TELEGRAM OMITIDO: Faltan TELEGRAM_BOT_TOKEN o TELEGRAM_CHAT_ID en .env');
  }

  // --- PRUEBA SLACK ---
  if (SLACK_WEBHOOK_URL) {
    try {
      const res = await fetch(SLACK_WEBHOOK_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          text: '🧪 *Ptah Studio CI/CD*\n¡Prueba de integración local completada con éxito desde la terminal!'
        })
      });
      if (res.ok) {
        console.log('✅ SLACK: Notificación enviada con éxito.');
      } else {
        console.error('❌ SLACK ERROR:', res.statusText);
      }
    } catch (err) {
      console.error('❌ SLACK ERROR DE RED:', err.message);
    }
  } else {
    console.warn('⚠️ SLACK OMITIDO: Falta SLACK_WEBHOOK_URL en .env');
  }
}

testNotifications();
