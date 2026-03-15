const fs = require('fs');
const nacl = require('tweetnacl');

const inPath = process.argv[2];
const outPath = process.argv[3];

if (!inPath || !outPath) {
  console.error('Usage: node sign-manifest.js in.json out.signed.json');
  process.exit(2);
}

const privBase64 = process.env.MANIFEST_PRIVATE_KEY;
if (!privBase64) {
  console.error('MANIFEST_PRIVATE_KEY env required');
  process.exit(2);
}

const priv = Buffer.from(privBase64, 'base64');
if (priv.length !== 64) {
  console.error(`Invalid MANIFEST_PRIVATE_KEY length: expected 64 bytes, got ${priv.length}`);
  process.exit(2);
}

const manifest = JSON.parse(fs.readFileSync(inPath, 'utf8'));

// Каноничная строка для подписи
const payloadString = JSON.stringify(manifest);
const sig = nacl.sign.detached(Buffer.from(payloadString, 'utf8'), new Uint8Array(priv));

const signed = {
  payload: manifest,
  signature: Buffer.from(sig).toString('base64'),
  algorithm: 'ed25519'
};

fs.writeFileSync(outPath, JSON.stringify(signed, null, 2));
console.log('Manifest signed ->', outPath);