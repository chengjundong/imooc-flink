# Flink Window API

# Time
- processing time: Time of Flink executing event processing; the lowest latency; no guarantee on data consistency
- event time: Time of event creation; more latency than using processing time; provide some data consistency

# Window
## Category
- Keyed or Non-key:
    - Keyed: `keyBay` & `window`
    - Non-key: `windowAll`
- Window size
    - Time driven: eg, size = 10 mins; [start, end)
    - Data driven: eg, size = 100 elements
- Assigner
    - Tumbling: no overlap, offset = window size
    - Silding: has overlap, offset != window size
    - Session(Punctuate): no overlap; no input after XXX, then close a window
    - Global: using custom trigger to let Flink when to open/close a window

# Reference
[Timely Stream Processing](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/concepts/time/)