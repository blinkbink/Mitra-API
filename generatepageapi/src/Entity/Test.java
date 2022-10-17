package Entity;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;

import id.co.keriss.consolidate.util.FileProcessor;

public class Test {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		FileImageInputStream fim=new FileImageInputStream(new File("/home/dev19/Downloads/Telegram Desktop/DS20171206102407.jpg"));
		 BufferedImage source =  ImageIO.read(fim);
//         int color = source.getRGB(0, 0);
         Image data=FileProcessor.makeColorTransparent(source, Color.WHITE, 0.5f);
         BufferedImage newBufferedImage = new BufferedImage(source.getWidth(),
       		  source.getHeight(), BufferedImage.TYPE_INT_RGB);
         newBufferedImage.createGraphics().drawImage(data, 0, 0, Color.OPAQUE,0, null);
         ImageIO.write(newBufferedImage, "png",new File("/home/dev19/Downloads/Telegram Desktop/DS20171206102407-OK.png"));
	}

}
