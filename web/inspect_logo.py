from PIL import Image, ImageDraw

input_path = "public/assets/logo/ExoticStamp_logo2.png"
output_path = "public/favicon-round.png"

im = Image.open(input_path).convert("RGBA")
size = (48, 48)
im = im.resize(size, Image.LANCZOS)
mask = Image.new("L", size, 0)
ImageDraw.Draw(mask).ellipse((0, 0, size[0], size[1]), fill=255)
output = Image.new("RGBA", size, (255, 255, 255, 0))
output.paste(im, (0, 0), im)
output.putalpha(mask)
output.save(output_path)
print("generated", output_path)
print("mode", im.mode, "size", im.size, "bands", im.getbands())
