# kotoba-lang/rtl

Zero-dep portable `.cljc` — restored from the legacy `kami-engine/kami-rtl`
Rust crate (deleted in kotoba-lang/kami-engine PR #82 "Remove Rust workspace
from kami-engine") as part of the **clj-wgsl migration** (ADR-2607010930,
`com-junkawasaki/root`).

HDL editor (Verilog/VHDL parsing), event-driven simulation, waveform
viewer, and synthesis netlist.

| Namespace | Restored from | Purpose |
|---|---|---|
| `rtl.hdl` | `hdl` | Verilog/VHDL AST (plain EDN maps/keywords) + minimal Verilog module-header parser |
| `rtl.simulator` | `simulator` | Event-driven RTL simulator, four-valued IEEE 1164 logic, delta-cycle support |
| `rtl.waveform` | `waveform` | Waveform view + IEEE 1364 VCD (Value Change Dump) export |
| `rtl.synthesis` | `synthesis` | Gate-level netlist, memoized combinational-depth estimation, constant-folding optimization |

Depends on `kotoba-lang/{engineer,engineer-render}` for shared contracts.

## Status

Restored — all 4 modules ported from the original 891-line Rust `lib.rs`,
with all 5 original Rust unit tests mirrored 1:1 in `test/rtl_test.cljc`
(+1 smoke test). Pure data + pure functions/parsers throughout; no IO/GPU.
`rtl.simulator`'s event queue uses a sorted vector (kept sorted on insert)
rather than a literal binary heap — same externally observable ordering
as the original's `BinaryHeap<SimEvent>` min-heap.

## Develop

```bash
clojure -M:test
```
