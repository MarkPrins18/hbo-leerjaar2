#!/usr/bin/env bash
# Bisection script to find which test class creates unwanted files/state.
# Runs every test class in the matching files one by one and stops at the
# first one after which the file/dir exists. Run from the project root; the
# path is relative to it, while tests run in server/ (so check server/<file>).
# Usage: bash .claude/skills/systematic-debugging/find-polluter.sh <file_or_dir_to_check> <test_file_pattern>
# Example: bash .claude/skills/systematic-debugging/find-polluter.sh 'server/.git' 'server/test/*.kt'

set -u

if [ $# -ne 2 ]; then
  echo "Usage: $0 <file_or_dir_to_check> <test_file_pattern>"
  echo "Example: $0 'server/.git' 'server/test/*.kt'"
  exit 1
fi

if [ ! -f ./kotlin ]; then
  echo "Run this script from the project root (./kotlin not found in $PWD)" >&2
  exit 2
fi

POLLUTION_CHECK="$1"
TEST_PATTERN="${2#./}"

echo "🔍 Searching for test that creates: $POLLUTION_CHECK"
echo "Test pattern: $TEST_PATTERN"
echo ""

# Test classes, not files: one file can hold several classes
# (MapTest.kt contains LocationServiceTest and MapRoutesTest).
TEST_CLASSES=$(find . -path "./$TEST_PATTERN" -name '*.kt' -print0 \
  | xargs -0 -r grep -hoE '^class [A-Za-z0-9_]+' \
  | cut -d' ' -f2 | sort -u)

if [ -z "$TEST_CLASSES" ]; then
  echo "No test classes found for pattern: $TEST_PATTERN" >&2
  exit 2
fi
TOTAL=$(printf '%s\n' "$TEST_CLASSES" | wc -l | tr -d ' ')

echo "Found $TOTAL test classes"
echo ""

LOG=$(mktemp)
trap 'rm -f "$LOG"' EXIT

COUNT=0
for TEST_CLASS in $TEST_CLASSES; do
  COUNT=$((COUNT + 1))

  if [ -e "$POLLUTION_CHECK" ]; then
    echo "⚠️  Pollution already exists before test $COUNT/$TOTAL"
    echo "   Remove it and rerun. Skipping: $TEST_CLASS"
    continue
  fi

  echo "[$COUNT/$TOTAL] Testing: $TEST_CLASS"

  # A failing test is fine here; a filter that runs nothing is not.
  ./kotlin test -m server --include-classes="$TEST_CLASS" > "$LOG" 2>&1 || true
  if grep -q "no tests were discovered" "$LOG"; then
    echo "   ERROR: no tests discovered for $TEST_CLASS. Output:" >&2
    tail -n 5 "$LOG" >&2
    exit 2
  fi

  if [ -e "$POLLUTION_CHECK" ]; then
    echo ""
    echo "🎯 FOUND POLLUTER!"
    echo "   Test class: $TEST_CLASS"
    echo "   Created: $POLLUTION_CHECK"
    echo ""
    echo "Pollution details:"
    ls -la "$POLLUTION_CHECK"
    echo ""
    echo "To investigate:"
    echo "  ./kotlin test -m server --include-classes=$TEST_CLASS    # Run just this class"
    echo "  grep -rn 'class $TEST_CLASS' server/test/                # Find its code"
    exit 1
  fi
done

echo ""
echo "✅ No polluter found - all tests clean!"
exit 0
