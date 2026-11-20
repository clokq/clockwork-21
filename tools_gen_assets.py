"""Generates icon.png, sun.png and moon_phases.png (mechanical-clock themed)."""
import math
from PIL import Image, ImageDraw

SS = 4  # supersampling

def rot_rect(d, cx, cy, ang, x1, y1, x2, y2, fill):
    c, s = math.cos(ang), math.sin(ang)
    pts = [(x1, y1), (x2, y1), (x2, y2), (x1, y2)]
    d.polygon([(cx + x * c - y * s, cy + x * s + y * c) for x, y in pts], fill=fill)

def gear(d, cx, cy, r, teeth, rot, fill, hole=None):
    tw, tl = r * 0.28, r * 0.3
    for i in range(teeth):
        rot_rect(d, cx, cy, rot + i * 2 * math.pi / teeth, r - 2, -tw / 2, r + tl, tw / 2, fill)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=fill)
    if hole:
        h = r * 0.35
        d.ellipse([cx - h, cy - h, cx + h, cy + h], fill=hole)

def clock(size, brass, dark, steel, red, hands=(10, 10, 30), small_gear=True):
    S = size * SS
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx = cy = S / 2
    r = S * 0.34
    if small_gear:
        gear(d, S * 0.2, S * 0.8, r * 0.42, 8, 0.2, tuple(int(c * 0.72) for c in brass[:3]) + (255,), dark)
    gear(d, cx, cy, r, 14, 0.1, brass)
    d.ellipse([cx - r * .8, cy - r * .8, cx + r * .8, cy + r * .8], fill=dark)
    for i in range(12):
        ln = r * (0.2 if i % 3 == 0 else 0.1)
        w = r * (0.05 if i % 3 == 0 else 0.03)
        rot_rect(d, cx, cy, i * math.pi / 6, r * .78 - ln, -w, r * .74, w, steel)
    h, m, s = hands
    def hand(a, ln, w, col):
        rot_rect(d, cx, cy, -math.pi / 2 + a, -ln * .15, -w, ln, w, col)
    hand((h % 12 + m / 60) * math.pi / 6, r * .45, r * .07, steel)
    hand(m * math.pi / 30, r * .65, r * .045, steel)
    hand(s * math.pi / 30, r * .7, r * .02, red)
    d.ellipse([cx - r * .1, cy - r * .1, cx + r * .1, cy + r * .1], fill=brass)
    return img.resize((size, size), Image.LANCZOS)

BRASS, DARK, STEEL, RED = (217, 164, 65, 255), (22, 22, 27, 255), (232, 228, 218, 255), (229, 72, 62, 255)

# mod icon
ic = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
bg = ImageDraw.Draw(ic)
bg.rounded_rectangle([0, 0, 127, 127], 22, fill=(18, 18, 22, 255))
ic.alpha_composite(clock(128, BRASS, DARK, STEEL, RED))
ic.save("src/main/resources/assets/clockwork/icon.png")

ENV = "src/main/resources/resourcepacks/clock_sky/assets/minecraft/textures/environment/"

# sun: warm golden clock (no small gear so it reads as a sun)
sun = clock(128, (255, 205, 80, 255), (70, 38, 8, 255), (255, 244, 200, 255), (255, 90, 40, 255), (10, 10, 30), small_gear=False)
sun.save(ENV + "sun.png")

# moon phases atlas: 4x2 cells, full -> waning -> new -> waxing
CELL = 128
atlas = Image.new("RGBA", (CELL * 4, CELL * 2), (0, 0, 0, 0))
base = clock(CELL, (205, 212, 228, 255), (28, 32, 48, 255), (245, 247, 255, 255), (150, 170, 255, 255), (3, 25, 50), small_gear=False)
for i in range(8):
    cell = base.copy()
    p = i / 8 * 2 * math.pi  # 0 = full
    px = cell.load()
    cx = cy = CELL / 2
    R = CELL * 0.34 * 1.3  # covers gear teeth
    for y in range(CELL):
        for x in range(CELL):
            r_, g_, b_, a_ = px[x, y]
            if a_ == 0:
                continue
            nx, ny = (x - cx) / R, (y - cy) / R
            edge = math.sqrt(max(0.0, 1 - ny * ny))
            term = edge * math.cos(p)
            lit = (nx < term) if i < 4 else (nx > -term)
            if i == 0:
                lit = True
            if not lit:
                px[x, y] = (r_ // 4, g_ // 4, b_ // 3, int(a_ * 0.55))
    atlas.alpha_composite(cell, ((i % 4) * CELL, (i // 4) * CELL))
atlas.save(ENV + "moon_phases.png")
print("assets ok")
