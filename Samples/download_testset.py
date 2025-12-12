#!/usr/bin/env python3
"""
SpatialLM3D Dataset Downloader
Downloads sample PLY files from HuggingFace SpatialLM-Testset dataset.

Usage:
    python download_testset.py [--count N] [--output-dir DIR]

Requirements:
    pip install huggingface-hub requests
"""

import argparse
import os
import sys
from pathlib import Path

try:
    from huggingface_hub import hf_hub_download, list_repo_files
    import requests
except ImportError:
    print("ERROR: Required packages not installed.")
    print("Please run: pip install huggingface-hub requests")
    sys.exit(1)


REPO_ID = "manycore-research/SpatialLM-Testset"
DEFAULT_COUNT = 5
DEFAULT_OUTPUT_DIR = Path(__file__).parent


def list_available_scenes(repo_id: str) -> list[str]:
    """List all PLY files available in the HuggingFace repository."""
    try:
        all_files = list_repo_files(repo_id, repo_type="dataset")
        ply_files = [f for f in all_files if f.endswith('.ply')]
        return sorted(ply_files)
    except Exception as e:
        print(f"ERROR: Failed to list repository files: {e}")
        return []


def download_ply_file(repo_id: str, filename: str, output_dir: Path) -> bool:
    """Download a single PLY file from HuggingFace."""
    try:
        print(f"Downloading: {filename}...")
        local_path = hf_hub_download(
            repo_id=repo_id,
            filename=filename,
            repo_type="dataset",
            local_dir=output_dir,
            local_dir_use_symlinks=False
        )

        # Move file to root of output_dir if it's in subdirectories
        downloaded_file = Path(local_path)
        target_file = output_dir / downloaded_file.name

        if downloaded_file != target_file:
            downloaded_file.rename(target_file)
            # Clean up empty subdirectories
            try:
                downloaded_file.parent.rmdir()
            except:
                pass

        file_size_mb = target_file.stat().st_size / (1024 * 1024)
        print(f"  SUCCESS: {target_file.name} ({file_size_mb:.2f} MB)")
        return True

    except Exception as e:
        print(f"  ERROR: Failed to download {filename}: {e}")
        return False


def create_readme(output_dir: Path, downloaded_files: list[str]):
    """Create a README.md file describing the downloaded samples."""
    readme_path = output_dir / "README.md"

    content = f"""# SpatialLM3D Sample Scenes

This directory contains {len(downloaded_files)} sample PLY files from the SpatialLM-Testset dataset.

## Dataset Source

- Repository: HuggingFace `manycore-research/SpatialLM-Testset`
- License: CC-BY-NC-4.0 (Non-commercial use, academic/educational projects allowed)
- URL: https://huggingface.co/datasets/manycore-research/SpatialLM-Testset

## Files

"""

    for i, filename in enumerate(downloaded_files, 1):
        file_path = output_dir / Path(filename).name
        if file_path.exists():
            size_mb = file_path.stat().st_size / (1024 * 1024)
            content += f"{i}. `{file_path.name}` ({size_mb:.2f} MB)\n"

    content += """
## Usage

1. Launch SpatialLM3D Desktop application
2. Click "Select PLY File" button
3. Navigate to this `Samples/` directory
4. Select any `.ply` file to analyze

## Notes

- These files are for demonstration and testing purposes
- Ensure you have sufficient memory to load large point clouds
- Processing time varies based on scene complexity
"""

    readme_path.write_text(content)
    print(f"\nCreated: {readme_path}")


def main():
    parser = argparse.ArgumentParser(
        description="Download sample PLY files from SpatialLM-Testset dataset"
    )
    parser.add_argument(
        "--count",
        type=int,
        default=DEFAULT_COUNT,
        help=f"Number of files to download (default: {DEFAULT_COUNT})"
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=DEFAULT_OUTPUT_DIR,
        help=f"Output directory (default: {DEFAULT_OUTPUT_DIR})"
    )
    parser.add_argument(
        "--list",
        action="store_true",
        help="List all available PLY files and exit"
    )

    args = parser.parse_args()

    print("=" * 70)
    print("SpatialLM3D Dataset Downloader")
    print("=" * 70)

    # List available files
    print(f"\nFetching available files from {REPO_ID}...")
    available_files = list_available_scenes(REPO_ID)

    if not available_files:
        print("ERROR: No PLY files found in repository.")
        sys.exit(1)

    print(f"Found {len(available_files)} PLY files in repository.")

    if args.list:
        print("\nAvailable PLY files:")
        for i, filename in enumerate(available_files, 1):
            print(f"  {i}. {filename}")
        sys.exit(0)

    # Prepare output directory
    output_dir = args.output_dir.resolve()
    output_dir.mkdir(parents=True, exist_ok=True)
    print(f"\nOutput directory: {output_dir}")

    # Download files
    count = min(args.count, len(available_files))
    print(f"\nDownloading {count} sample files...")
    print("-" * 70)

    downloaded = []
    for filename in available_files[:count]:
        if download_ply_file(REPO_ID, filename, output_dir):
            downloaded.append(filename)

    # Summary
    print("-" * 70)
    print(f"\nDownload complete: {len(downloaded)}/{count} files successful")

    if downloaded:
        create_readme(output_dir, downloaded)
        print("\nSample files ready for use in SpatialLM3D application!")
    else:
        print("\nWARNING: No files were downloaded successfully.")
        sys.exit(1)


if __name__ == "__main__":
    main()
