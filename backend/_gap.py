import xml.etree.ElementTree as ET
root = ET.parse("target/site/jacoco/jacoco.xml").getroot()

def pkg_classes(prefix):
    rows = []
    for pkg in root.findall("package"):
        name = pkg.get("name", "")
        if not name.startswith(prefix):
            continue
        for cls in pkg.findall("class"):
            cname = cls.get("name", "").split("/")[-1]
            if "$" in cname:
                continue
            low = cname.lower()
            if any(x in low for x in ["request", "response", "dto", "view", "command"]):
                continue
            missed_l = covered_l = missed_b = covered_b = 0
            for c in cls.findall("counter"):
                if c.get("type") == "LINE":
                    missed_l = int(c.get("missed"))
                    covered_l = int(c.get("covered"))
                if c.get("type") == "BRANCH":
                    missed_b = int(c.get("missed"))
                    covered_b = int(c.get("covered"))
            total = missed_l + covered_l
            if total == 0 or missed_l < 5:
                continue
            rows.append((missed_l, missed_b, covered_l, total, name, cname))
    rows.sort(reverse=True)
    return rows[:30]

for label, pref in [
    ("REWARD", "metro/ExoticStamp/modules/reward"),
    ("AUTH", "metro/ExoticStamp/modules/auth"),
    ("COLLECTION", "metro/ExoticStamp/modules/collection"),
]:
    print("===", label, "===")
    for ml, mb, cl, tot, pkg, cn in pkg_classes(pref):
        short = pkg.split("/")[-1]
        print(f"{ml:4d}L {mb:3d}B miss | {cl:4d}/{tot:4d} | {short}/{cn}")