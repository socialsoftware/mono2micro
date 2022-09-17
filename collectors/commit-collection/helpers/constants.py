from dataclasses import dataclass
from pathlib import Path


@dataclass
class Constants:
    group_commits_interval: int = 3600
    project_root: Path = Path(__file__).parent.parent
    codebases_data_output_directory: str = str(project_root) + "/resources/codebases_collection"
    resources_directory: str = str(project_root) + "/resources"
    codebases_root_directory: str = str(project_root) + "/codebases_cloned"
    mono2micro_codebases_root: str = str(project_root.parent) + "/mono2micro-mine/codebases"
