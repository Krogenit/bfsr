const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function walk(dir, root) {
  const res = [];
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name);
    const stat = fs.statSync(p);
    if (stat.isDirectory()) {
      res.push(...walk(p, root));
    } else if (stat.isFile()) {
      res.push({ p, rel: path.relative(root, p).replace(/\\/g,'/') , stat });
    }
  }
  return res;
}

function sha256File(filePath) {
  const h = crypto.createHash('sha256');
  const s = fs.readFileSync(filePath);
  h.update(s);
  return h.digest('hex');
}

const argv = require('minimist')(process.argv.slice(2));
const src = argv.src || 'run/client/build/output';
const out = argv.out || 'out';
const version = argv.version || new Date().toISOString();

if (!fs.existsSync(src)) {
  console.error('Source dir not found:', src);
  process.exit(2);
}

const files = walk(src, src).map(({p, rel, stat}) => {
  const sha = sha256File(p);
  return {
    path: rel,
    size: stat.size,
    sha256: sha,
    mode: '0' + (stat.mode & 0o777).toString(8)
  };
});

const manifest = {
  version,
  publishedAt: new Date().toISOString(),
  files
};

fs.mkdirSync(out, { recursive: true });
fs.writeFileSync(path.join(out, 'manifest.json'), JSON.stringify(manifest, null, 2));
console.log('Manifest created:', path.join(out, 'manifest.json'));