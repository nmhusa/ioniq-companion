from pathlib import Path
from zipfile import ZipFile
for module in ("app","wear"):
    mapping=Path(module+"/build/outputs/mapping/release/mapping.txt").read_text()
    assert " -> " in mapping, module
    apk=Path(module+"/build/outputs/apk/release/"+module+"-release-unsigned.apk")
    with ZipFile(apk) as z:
        for name in z.namelist():
            assert b"CI_SYNTHETIC_CREDENTIAL_DO_NOT_USE_081" not in z.read(name), "Plaintext test credential found"
    if module=="app":
        assert any(line.startswith("com.ioniq.widget.HyundaiApi -> ") and not line.endswith("HyundaiApi:") for line in mapping.splitlines()), "API class not renamed"
print("Release mapping and plaintext sentinel checks passed")
