/* Static server for the built Melviz Editor (dist/).
 *
 * Provides the same virtual-dashboard endpoint the editor relies on for the
 * live preview:
 *
 *   GET  /dashboards/editor/<name>.dash.yaml  →  current editor content
 *   POST /_editor/dashboards                  →  register/refresh content
 *
 * Usage: yarn serve   (from editor/, after `yarn build`).
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT ? Number(process.env.PORT) : 8082;
const HOST = process.env.HOST || 'localhost';
const DIST_DIR = path.join(__dirname, '../dist');

const virtualDashboards = new Map(); // name -> { yaml, fileName, mtime }

const MIME = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.yaml': 'text/yaml; charset=utf-8',
    '.yml': 'text/yaml; charset=utf-8',
    '.map': 'application/json; charset=utf-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
    '.woff': 'font/woff',
    '.woff2': 'font/woff2',
    '.ttf': 'font/ttf',
};

function sendJson(res, status, obj) {
    const body = JSON.stringify(obj);
    res.writeHead(status, {
        'Content-Type': 'application/json; charset=utf-8',
        'Content-Length': Buffer.byteLength(body),
        'Cache-Control': 'no-store',
    });
    res.end(body);
}

function readBody(req) {
    return new Promise((resolve, reject) => {
        let data = '';
        req.on('data', (chunk) => {
            data += chunk;
            if (data.length > 10 * 1024 * 1024) {
                reject(new Error('Payload too large'));
                req.destroy();
            }
        });
        req.on('end', () => resolve(data));
        req.on('error', reject);
    });
}

function resolveVirtualDashboardUrl(url) {
    const m = url.match(/^\/dashboards\/editor\/([^\/]+)\.dash\.yaml$/);
    if (!m) return null;
    return decodeURIComponent(m[1]);
}

function serveStatic(req, res, urlPath) {
    let filePath = path.join(DIST_DIR, urlPath);
    if (urlPath === '/' || urlPath === '') {
        filePath = path.join(DIST_DIR, 'index.html');
    }
    fs.stat(filePath, (err, stat) => {
        if (err || !stat || !stat.isFile()) {
            fs.readFile(path.join(DIST_DIR, 'index.html'), (e2, buf) => {
                if (e2) {
                    res.writeHead(404);
                    res.end('Not found');
                    return;
                }
                res.writeHead(200, { 'Content-Type': MIME['.html'] });
                res.end(buf);
            });
            return;
        }
        const ext = path.extname(filePath).toLowerCase();
        res.writeHead(200, {
            'Content-Type': MIME[ext] || 'application/octet-stream',
            'Cache-Control': 'no-cache',
        });
        fs.createReadStream(filePath).pipe(res);
    });
}

const server = http.createServer(async (req, res) => {
    const url = new URL(req.url, `http://${HOST}`);
    const pathname = url.pathname;

    if (req.method === 'GET' && resolveVirtualDashboardUrl(pathname)) {
        const name = resolveVirtualDashboardUrl(pathname);
        const dash = virtualDashboards.get(name);
        if (!dash) {
            sendJson(res, 404, { error: `No virtual dashboard named "${name}"` });
            return;
        }
        res.writeHead(200, {
            'Content-Type': 'text/yaml; charset=utf-8',
            'Cache-Control': 'no-store',
        });
        res.end(dash.yaml || '');
        return;
    }

    if (req.method === 'POST' && pathname === '/_editor/dashboards') {
        try {
            const body = JSON.parse((await readBody(req)) || '{}');
            const name = String(body.name || 'dashboard').replace(/\.(ya?ml)$/i, '');
            virtualDashboards.set(name, {
                yaml: typeof body.yaml === 'string' ? body.yaml : '',
                fileName: body.fileName || name + '.yaml',
                mtime: Date.now(),
            });
            sendJson(res, 200, { ok: true, name });
        } catch (e) {
            sendJson(res, 400, { error: String(e) });
        }
        return;
    }

    if (req.method === 'GET' && pathname === '/_editor/dashboards') {
        sendJson(res, 200, Array.from(virtualDashboards.keys()));
        return;
    }

    serveStatic(req, res, pathname);
});

server.listen(PORT, HOST, () => {
    console.log(`Melviz Editor (built):  http://${HOST}:${PORT}`);
    console.log(`  dist: ${DIST_DIR}`);
});
