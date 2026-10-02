#!/usr/bin/env python3
"""Print the Maven -pl list for the modules touched since a given commit.

Modules are resolved against the reactor graph declared by the root pom, so the
output only ever contains projects Maven will actually have in its reactor.
Prints nothing when no reactor module was touched.
"""

import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

POM_NS = "{http://maven.apache.org/POM/4.0.0}"


def _strip_ns(tag):
    return tag.split("}", 1)[-1] if "}" in tag else tag


def read_modules(pom):
    """Declared <module> entries of a pom, including those inside <profiles>."""
    try:
        root = ET.parse(pom).getroot()
    except ET.ParseError as exc:
        print(f"warning: could not parse {pom}: {exc}", file=sys.stderr)
        return []
    modules = []
    for modules_el in root.iter():
        if _strip_ns(modules_el.tag) != "modules":
            continue
        for module_el in modules_el:
            if _strip_ns(module_el.tag) == "module" and module_el.text:
                modules.append(module_el.text.strip())
    return modules


def build_reactor(repo_root):
    """Map every reactor module directory to its child module directories."""
    children = {}
    queue = [Path(".")]
    while queue:
        rel_dir = queue.pop()
        if rel_dir in children:
            continue
        pom = repo_root / rel_dir / "pom.xml"
        if not pom.is_file():
            continue
        kids = []
        for name in read_modules(pom):
            kid = Path(*(rel_dir / name).parts)
            if (repo_root / kid / "pom.xml").is_file():
                kids.append(kid)
                queue.append(kid)
        children[rel_dir] = kids
    return children


def descendants(children, start):
    out, queue = set(), [start]
    while queue:
        node = queue.pop()
        for kid in children.get(node, []):
            if kid not in out:
                out.add(kid)
                queue.append(kid)
    return out


def owning_module(children, path):
    """Deepest reactor module directory containing `path`, or None."""
    candidate = None
    for parent in [path.parent, *path.parent.parents]:
        if parent in children:
            if candidate is None or len(parent.parts) > len(candidate.parts):
                candidate = parent
    return candidate


def main():
    base_sha = sys.argv[1]
    repo_root = Path(
        subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            check=True, capture_output=True, text=True,
        ).stdout.strip()
    )

    # -z keeps paths NUL-delimited, so names containing spaces survive intact.
    raw = subprocess.run(
        ["git", "diff", "--name-only", "-z", base_sha, "HEAD"],
        check=True, capture_output=True, text=True,
    ).stdout
    changed = [Path(p) for p in raw.split("\0") if p]

    children = build_reactor(repo_root)
    selected = set()
    for path in changed:
        module = owning_module(children, path)
        if module is None or module == Path("."):
            continue
        if children.get(module):
            # Aggregator: -pl on it would build only that pom, so take its
            # declared descendants instead.
            selected |= descendants(children, module)
        else:
            selected.add(module)

    leaves = sorted(str(m) for m in selected if not children.get(m))
    print(",".join(leaves))


if __name__ == "__main__":
    main()
