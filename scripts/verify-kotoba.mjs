import fs from "node:fs";
import path from "node:path";
import { pathToFileURL } from "node:url";

const [webPath, wasmPath, hostPath] = process.argv.slice(2);
if (!webPath || !wasmPath || !hostPath) throw new Error("missing conformance paths");

const cases = [
  [[0n, 5n, 1n, 4n, 2n, 7n, 3n, 6n], 4n, 4n, 1n, ":perfect-memory"],
  [[0n, 1n, 0n, 5n, 1n, 4n, 2n, 7n, 3n, 6n], 5n, 4n, 1n, ":a"],
  [[0n, 0n, 0n, 5n, 0n, 1n, 1n, 4n, 2n, 7n, 3n, 6n], 4n, 4n, 1n, ":perfect-memory"],
  [[0n, 5n], 1n, 1n, 0n, ":perfect-memory"],
  [[], 0n, 0n, 0n, ":perfect-memory"],
];
const rejected = [[0n], [-1n, 0n], [0n, 8n], Array(34).fill(0n)];

const web = await import(pathToFileURL(path.resolve(webPath)));
if (web.kotobaArtifact.requiredCapabilities.length !== 0)
  throw new Error("Memorize Web graph requested a capability");
if (web.instantiateKotoba().main() !== 42n) throw new Error("Memorize Web main mismatch");
for (const args of cases)
  if (web.instantiateKotoba()["summary-check"](...args) !== 42n)
    throw new Error("Memorize Web summary mismatch");
for (const picks of rejected)
  if (web.instantiateKotoba()["reject-check"](picks) !== 42n)
    throw new Error("Memorize Web accepted malformed picks");

const host = await import(pathToFileURL(path.resolve(hostPath)));
const wasmBytes = fs.readFileSync(path.resolve(wasmPath));
for (const [picks, attempts, pairs, completeFlag, grade] of cases) {
  const wasm = await host.instantiateKotoba(wasmBytes);
  if (wasm.instance.exports["summary-check"](
      wasm.typedValues.vectorI64(picks), attempts, pairs, completeFlag, grade) !== 42n)
    throw new Error("Memorize Wasm summary mismatch");
}
for (const picks of rejected) {
  const wasm = await host.instantiateKotoba(wasmBytes);
  if (wasm.instance.exports["reject-check"](wasm.typedValues.vectorI64(picks)) !== 42n)
    throw new Error("Memorize Wasm accepted malformed picks");
}
const wasmMain = await host.instantiateKotoba(wasmBytes);
if (wasmMain.instance.exports.main() !== 42n) throw new Error("Memorize Wasm main mismatch");

console.log("ghosthacker-memorize: bounded first-case-file Web/Wasm conformance passed");
