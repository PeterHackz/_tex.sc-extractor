import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;

class Stream {

	public byte[] stream;
	public int offset = 0;

	public Stream(FileInputStream a1) {
		try {
			// FileInputStream is slow AF, so let's make our own reader.
			this.stream = new byte[a1.available()];
			a1.read(this.stream);
		} catch (IOException e) {}
	}

	public int read() {
		this.offset += 1;
		return this.stream[this.offset - 1];
	}

	public int readInt() {
		int v1 = this.read();
		int v2 = this.read();
		int v3 = this.read();
		int v4 = this.read();
		return (((v1 & 0xFF) << 24) | ((v2 & 0xFF) << 16) | ((v3 & 0xFF) << 8) | (v4 & 0xFF));
	}

	public int readByte() {
		return (byte)this.read() & 0xFF;
	}

	public int readUInt32() {
		int v1, v2, v3, v4;
		v1 = this.readByte();
		v2 = this.readByte();
		v3 = this.readByte();
		v4 = this.readByte();
		return ((v4 << 24) >>> 0) + ((v3 << 16) | (v2  << 8) | v1);
	}

	public int readU16() {
		int v1 = 0;
		int v2, v3;
		v2 = this.readByte();
		v3 = this.readByte();
		v1 = (v3 << 8) | v2;
		return v1;
	}

	public int parseColor(int a1, int a2, int a3, int a4) {
		return (a4 << 24) | (a1 << 16) | (a2 << 8) | a3;
	}

	public int readPixel(int a1) {
		int v1 = 0;
		int v2, v3, v4, v5;
		if (a1 == 0 || a1 == 1) {
			v2 = this.readByte();
			v3 = this.readByte();
			v4 = this.readByte();
			v5 = this.readByte();
			return this.parseColor(v2, v3, v4, v5);
		} else if (a1 == 2) {
			v2 = this.readU16();
			return this.parseColor((((v2 >> 12) & 0xF) << 4), (((v2 >> 8) & 0xF) << 4), (((v2 >> 4) & 0xF) << 4), ((v2 & 0xF) << 4));
		} else if (a1 == 3) {
			v2 = this.readU16();
			return this.parseColor((((v2 >> 11) & 0x1F) << 3), (((v2 >> 6) & 0x1F) << 3), (((v2 >> 1) & 0x1F) << 3), ((v2 & 0xFF) << 7));
		} else if (a1 == 4) {
			v2 = this.readU16();
			return this.parseColor((((v2 >> 11) & 0x1F) << 3), (((v2 >> 5) & 0x3F) << 2), ((v2 & 0x1F) << 3), 255);
		} else if (a1 == 6) {
			v2 = this.readU16();
			return this.parseColor((v2 >> 8), (v2 >> 8), (v2 >> 8), (v2 & 0xFF));
		} else if (a1 == 10) {
			v2 = this.readByte();
			return this.parseColor(v2, v2, v2, v2);
		}
		return v1;
	}

}

public class main {
	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			FileInputStream v1 = new FileInputStream("tex.sc");
			Stream stream = new Stream(v1);
			String sub = "";
			while (true) {
				int tag = stream.readByte();
				if (tag == 0) {
					break;
				}
				int length = stream.readUInt32();
				int pt = stream.readByte();
				long width = stream.readU16();
				long height = stream.readU16();
				BufferedImage image = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
				if (tag != 28 && tag != 27) {
					for (int w = 0; w < width; w++) {
						for (int h = 0; h < height; h++) {
							image.setRGB(w, h, stream.readPixel(pt));
						}
					}
				} else {
					int z, c;
					double hl = Math.ceil((float)height / 32);
					double wl = Math.ceil((float)width / 32);
					for (int x = 0; x < hl; x++) {
						for (int y = 0; y < wl; y++) {
							z = x * 32;
							while (z != ((x + 1) * 32) && z < height) {
								c = y * 32;
								while (c != ((y + 1) * 32) && c < width) {
									image.setRGB(c, z, stream.readPixel(pt));
									c += 1;
								}
								z += 1;
							}
						}
					}
				}
				File output = new File("result" + sub + ".png");
				ImageIO.write(image, "png", output);
				sub += "_";
			}
			long end = System.currentTimeMillis();
			System.out.println("completed in: " + ((end - start) / 1000) + "seconds.");
		} catch (IOException e) {}
	}
}
