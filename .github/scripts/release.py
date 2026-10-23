"""Validate a version-specific release and stage only its verified artifacts."""
import argparse
import json
from pathlib import Path
import re
import shutil
import struct
import zipfile


def validate_tag(tag, minecraft):
    pattern = r"v[0-9]+\.[0-9]+\.[0-9]+\+mc" + re.escape(minecraft)
    if not re.fullmatch(pattern, tag):
        raise ValueError(f"Tag must be vMAJOR.MINOR.PATCH+mc{minecraft}, got {tag!r}")
    properties = dict(
        line.split("=", 1) for line in Path("gradle.properties").read_text().splitlines()
        if "=" in line and not line.lstrip().startswith("#")
    )
    if properties["minecraft_version"] != minecraft:
        raise ValueError("Tag target does not match gradle.properties")
    return tag.removeprefix("v")


def stage(version, minecraft, java):
    artifact = Path(f"build/libs/aedans-unicorn-{version}.jar")
    sources = artifact.with_name(f"aedans-unicorn-{version}-sources.jar")
    with zipfile.ZipFile(artifact) as jar:
        metadata = json.loads(jar.read("fabric.mod.json"))
        if metadata["id"] != "unicorn" or metadata["version"] != version:
            raise ValueError("Jar mod ID/version does not match the release")
        if metadata["depends"]["minecraft"] != minecraft:
            raise ValueError("Jar targets the wrong Minecraft version")
        if metadata["depends"]["java"] != f">={java}":
            raise ValueError("Jar declares the wrong Java requirement")
        classes = [name for name in jar.namelist() if name.endswith(".class")]
        if not classes or any("UnicornGameTests" in name or "review/" in name for name in classes):
            raise ValueError("Jar is empty or contains test/diagnostic classes")
        for name in classes:
            magic, minor, major = struct.unpack(">IHH", jar.read(name)[:8])
            if magic != 0xCAFEBABE or major != java + 44:
                raise ValueError(f"Wrong Java bytecode in {name}: {major}")
    with zipfile.ZipFile(sources) as jar:
        if jar.testzip() is not None:
            raise ValueError("Corrupt sources jar")
    output = Path("build/release")
    if output.exists():
        shutil.rmtree(output)
    output.mkdir()
    for source in (artifact, sources):
        shutil.copy2(source, output / source.name)
    print(f"Verified and staged {version} for Minecraft {minecraft}, Java {java}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("validate-tag", "stage"))
    parser.add_argument("--tag", required=True)
    parser.add_argument("--minecraft", required=True)
    parser.add_argument("--java", type=int, required=True)
    args = parser.parse_args()
    version = validate_tag(args.tag, args.minecraft)
    if args.command == "stage":
        stage(version, args.minecraft, args.java)
