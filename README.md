# JVM Custom Memory Arena

This project is a from-scratch, educational implementation of a manual memory arena in Java, built to understand how low-level memory, pointers, and data structures actually work beneath high-level language abstractions.

 

## Motivation

After ~4 years of programming in Java, a persistent and sizable chunk of its functionality as a language still feels opaque and abstract because of how it manages things under the hood:

- Objects appear without visible allocation
- Memory is automatically managed
- References feel disconnected from actual memory

This project aims to remove that abstraction.

Everything here is built on top of a single `byte[]`, and all structures, safety measures, and meaning are implemented manually.

The design emphasizes explicit control and fail-fast behavior so that incorrect assumptions about memory usage surface immediately.

 

## Project Overview

At its core, this project implements a **manual memory arena**:

- Memory is allocated explicitly
- All reads and writes are bounds-checked
- Data structures are constructed by defining layouts over raw bytes
- Pointers are represented as integer offsets
- Invalid memory access fails immediately

The project incrementally builds higher-level behavior on top of this foundation in a controlled and well-defined manner.

 

## Core Concepts Implemented

### 1. Memory Arena

The arena is a contiguous block of memory represented by a `byte[]` and a single allocation pointer (`offset`).

```
[ allocated memory | unallocated memory ]
                 ^
               offset
```

Rules:
- Memory must be allocated before use
- Reads and writes are only allowed inside the allocated region
- Resetting the arena invalidates all previously allocated addresses

This mirrors modern arena allocators used in professional systems programming.

 

### 2. Strict Allocation Model

This project uses a strict safety model:

- All memory access is validated against the current allocation boundary
- Reading or writing outside allocated memory throws immediately
- There is no silent memory corruption

This forces correct reasoning and makes bugs obvious.

 

### 3. Primitive Storage (Big Endian)

Primitive values are stored manually.

Example: storing an `int`

- An `int` occupies 4 bytes
- Values are stored in big-endian order
- Bit shifting and masking are used explicitly
- Reconstruction is performed byte-by-byte

This makes integer representation and endianness explicit instead of implicit.

 

### 4. Struct-like Layouts (Nodes)

On top of raw memory, the project defines structured layouts.

A Node is defined as:

```
Node (8 bytes total):
+------------------+
| value (int)      |  offset + 0
+------------------+
| next (int)       |  offset + 4
+------------------+
```

Nodes are not Java objects.

A node is simply:
- an integer address
- a fixed memory layout
- helper methods that interpret bytes at that address

This mirrors C-style structs and pointers.

 

### 5. Pointers and Sentinel Values

Pointers are represented as integer offsets into the arena.

Rules:
- A pointer is either:
  - a valid address inside allocated memory
  - `-1`, used as a null sentinel
- All pointers are validated before use
- Invalid pointers throw immediately

This makes pointer semantics explicit and visible.

 

### 6. Data Structures Built from Raw Memory

Using the node layout, the project implements a linked list:

- Nodes allocated from the arena
- `next` pointers stored as raw addresses
- Traversal implemented manually
- No Java collections or object allocation involved

This demonstrates how high-level data structures emerge from low-level memory rules.

 

## Safety Mechanisms

Two layers of validation are implemented.

### `checkAddr(addr, bytesNeeded)`

Ensures:
- The address is non-negative
- The requested memory range fits entirely inside allocated memory

Used for primitive reads and writes.

 

### `checkNodePtr(ptr)`

Ensures:
- The pointer is `-1` (null), or
- The pointer references a full node inside allocated memory

Used for all node-based operations.

 

## Scope and Design Constraints

- Not a production allocator
- Not optimized for performance
- Not concurrent
- Not using JVM internals or Unsafe
- Not a garbage collector replacement

The implementation prioritizes clarity, correctness, and explicit control over performance or feature completeness.

 

## What This Project Teaches

- How memory is laid out at the byte level
- Why bounds checking matters
- How pointers actually work
- How struct layout is a design decision
- How data structures are built from raw memory
- Why high-level languages feel safe

 

## Current Capabilities

- Manual memory allocation
- Big-endian integer storage
- Strict bounds checking
- Struct-like node layouts
- Pointer validation
- Linked list traversal

 

## Planned Next Steps

- Separate node logic into a dedicated NodeStore
- Add additional struct types (arrays, strings)
- Improve diagnostics and error reporting
- Optional alignment guarantees
- Unit tests for failure cases
- Memory layout diagrams

 

 