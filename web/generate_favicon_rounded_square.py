from PIL import Image, ImageDraw

input_path = 'public/assets/logo/ExoticStamp_logo2.png'
output_path = 'public/favicon-rounded-square.png'

im = Image.open(input_path).convert('RGBA')
size = (48, 48)
im = im.resize(size, Image.LANCZOS)
mask = Image.new('L', size, 0)
corner_radius = 9

# Rounded rectangle mask
for y in range(size[1]):
    for x in range(size[0]):
        rx = min(x, size[0] - 1 - x)
        ry = min(y, size[1] - 1 - y)
        if rx >= corner_radius or ry >= corner_radius:
            mask.putpixel((x, y), 255)
        else:
            dx = corner_radius - rx
            dy = corner_radius - ry
            if dx * dx + dy * dy <= corner_radius * corner_radius:
                mask.putpixel((x, y), 255)

output = Image.new('RGBA', size, (255, 255, 255, 0))
output.paste(im, (0, 0), im)
output.putalpha(mask)
output.save(output_path)
print('generated', output_path)
