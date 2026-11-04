# Evaluation change log

Dev set: trials/ run 20261104T173951Z (36 trials). Every change below was made
after looking at dev-set results. Final numbers come ONLY from a fresh test run
that is not inspected before evaluation.

| # | Date | Change | Why | Found on |
|---|------|--------|-----|----------|
| 1 | 2026-11-04 | llm: replaced per-service log-volume table with "services completely silent" | Volume swings with random traffic; caused a false alarm on a no-fault trial. Restores parity with rules (which only use silence) | dev trial 001-none (pre-run check) |
| 2 | 2026-11-04 | rules: rpc_unreachable also matches `UnknownHostException: chain` | Stopped container loses its DNS name; third form of "chain down" (after timeout, refused). Rules scored 0% on chain_stop | chain_stop x3 |
| 3 | 2026-11-04 | incident: `restarted()` signal from startup markers; rules + llm both get it | wrong_contract and chain_reset have identical error lines; only the restarted service differs ("what changed?") | wrong_contract / chain_reset |
| 4 | 2026-11-04 | rules: chain_restarted_state_lost, backend_restarted_config_change | Use change #3 | same |

## Known metric limitations
- evidence_valid checks a quote EXISTS in the window, not that it is RELEVANT
  (llama3.2 quoted real but unrelated Hibernate startup warnings). Relevance -> LLM judge (Phase 5).
- n=3 per fault at temperature 0: repeats are near-identical; effective sample ~12 fault types.

## Dev results log
| version | rules | qwen3:8b (no think) | llama3.2 | note |
|---|---|---|---|---|
| v1 | 67% | 58% | 81% (3/3 false alarms) | before changes 2-4 |
| v2 | 83% | 50% | 75% (3/3 false alarms) | after 2-4. One added prompt line moved qwen3 -8pts (backend_crash -> wallet 3/3): LLM prompt fragility |

Rules improved using the dev set; LLMs were not tuned -> dev comparison is NOT fair. Final claims only from a fresh test run.
