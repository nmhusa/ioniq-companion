import json,pathlib
root=pathlib.Path(__file__).resolve().parents[1]
base=root/'app/src/main/assets'
data=json.loads((base/'paint_catalog.json').read_text())
assert data['market']=='US'
groups=data['groups'];assert len(groups)==13
assert len({g['id'] for g in groups})==len(groups)
assets=set()
for g in groups:
 assert 2022<=int(g['year'])<=2026
 assert g['source'].startswith('https://')
 assert len({c['id'] for c in g['colors']})==len(g['colors'])
 for c in g['colors']:
  assert c['asset'].startswith('paint_icons/') and '..' not in c['asset']
  assert (base/c['asset']).is_file()
  assets.add(c['asset'])
assert len(assets)==50
assert not any(g['model']==6 and g['year']=='2026' for g in groups)
assert sum((base/a).stat().st_size for a in assets)<4000000
assert len(next(g for g in groups if g['id']=='i5_2022_standard')['colors'])==6
assert len(next(g for g in groups if g['id']=='i9_2026_standard')['colors'])==9
assert all(not c['code'] for g in groups if int(g['year'])<2025 for c in g['colors'])
print('Paint catalog: 13 palettes, 50 local images; year coverage and paths checked')
